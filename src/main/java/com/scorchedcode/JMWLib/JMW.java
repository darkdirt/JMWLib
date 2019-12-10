package com.scorchedcode.JMWLib;

import com.scorchedcode.JMWLib.event.MeWeEventListener;
import com.scorchedcode.JMWLib.model.Group;
import com.scorchedcode.JMWLib.model.User;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JMW {

    private static JMW instance;
    static ArrayList<MeWeEventListener> registeredListeners = new ArrayList<>();
    private String refreshToken;
    private String csrfToken;
    private String userID;

    protected JMW(JMWBuilder builder) {
        if (instance == null) {
            if (new File("cookies.json").exists()) {
                CookieManager cookies = new CookieManager();
                CookieHandler.setDefault(cookies);
                try {
                    File cookieFile = new File("cookies.json");
                    JSONObject obj = new JSONObject(String.join("", Files.readAllLines(cookieFile.toPath())));
                    for (String key : obj.keySet())
                        cookies.getCookieStore().add(new URI("https://mewe.com"), new HttpCookie(key, obj.getString(key)));
                    for (HttpCookie httpCookie : cookies.getCookieStore().get(new URI("https://mewe.com"))) {
                        if (httpCookie.getName().equalsIgnoreCase("refresh-token"))
                            refreshToken = httpCookie.getValue();
                        if (httpCookie.getName().equalsIgnoreCase("csrf-token"))
                            csrfToken = httpCookie.getValue();
                    }
                    instance = this;
                    new Query();
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            } else if (builder.refresh == null || builder.csrf == null) {
                TokenWebView.main(null);
                return;
            } else {
                refreshToken = builder.refresh;
                csrfToken = builder.csrf;
                instance = this;
                serializeCookies();
            }
            JSONObject info = submitQuery("https://mewe.com/api/v2/me/info");
            userID = info.getString("id");
            URI socket = null;
            try {
                socket = new URI("wss://ws.mewe.com/indexWS?userId="+userID);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Cookie", "refresh-token="+refreshToken + "; csrf-token="+csrfToken);
            MeWeSocket pipe = new MeWeSocket(socket, headers);
            try {
                pipe.connectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static JMW getInstance() {
        return instance;
    }

    public Group getGroup(String id) {
        Query groupQuery = new Query("https://mewe.com/api/v2/group/" + id);
        return new Group(groupQuery.getResponse());
    }

    public User getUser(String id) {
        Query userQuery = new Query("https://mewe.com/api/v2/mycontacts/user/" + id);
        return new User(userQuery.getResponse());

    }

    public User getSelfUser() {
        return getUser(userID);
    }

    public ArrayList<Group> getGroups() {
        ArrayList<Group> allGroups = new ArrayList<>();
        Query groups = new Query("https://mewe.com/api/v2/groups");
        try {
            for (Object group : groups.getResponse().getJSONArray("confirmedGroups"))
                allGroups.add(new Group((JSONObject) group));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return allGroups;
    }

    public JSONObject submitQuery(String endpoint, String... requests) {
        return new Query(endpoint, requests).getResponse();
    }

    public JSONObject submitPayload(String endpoint, Object payload) {
        return new Query(endpoint, payload).getResponse();
    }

    public void addListener(MeWeEventListener e) {
        registeredListeners.add(e);
    }

    class Query {
        JSONObject response;

        public Query() {
            CookieManager manager = (CookieManager) CookieHandler.getDefault();
            HashMap<String, String> cookies = new HashMap<>();
            Connection conn = buildConnection("https://mewe.com/api/v3/auth/identify");
            try {
                for (HttpCookie cookie : manager.getCookieStore().get(new URI("https://mewe.com")))
                    cookies.put(cookie.getName(), cookie.getValue());
                conn.cookies(cookies);
                System.out.println(conn.method(Connection.Method.GET).execute().body());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            updateTokens(conn);
        }

        public Query(String endpoint, Object payload) {
            Connection conn = buildConnection(endpoint);
            if(payload instanceof JSONObject) {
                conn.header("Content-Type", "application/json; charset=UTF-8");
                try {
                    response = new JSONObject(conn.method(Connection.Method.POST).requestBody(payload.toString()).execute().body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(payload instanceof InputStream) {
                InputStream ins = (InputStream)payload;
                try {
                    conn.data("files[]", "image.png", ins);
                    response = new JSONObject(conn.method(Connection.Method.POST).execute().body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            updateTokens(conn);
        }

        public Query(String endpoint, String... requests) {
            Connection conn = buildConnection(endpoint);
            conn.ignoreHttpErrors(true);
            if (requests.length != 0) {
                for (String request : requests)
                    conn.data(request.split(",")[0], request.split(",")[1]);
            }
            Connection.Response data = null;
            try {
                data = conn.method(Connection.Method.GET).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(data.body());

            response = new JSONObject(data.body());
            updateTokens(conn);
        }

        JSONObject getResponse() {
            return response;
        }

        private Connection buildConnection(String endpoint) {
            Connection conn = Jsoup.connect(endpoint).userAgent("Mozilla/5.0 (jsoup)").timeout(5000).ignoreContentType(true);
            conn.header("X-CSRF-Token", csrfToken);
            conn.header("Cookie", "refresh-token=" + refreshToken);
            return conn;
        }

        private void updateTokens(Connection conn) {
            Map<String, String> cookies = conn.response().cookies();
            if (cookies.size() > 0) {
                CookieManager manager = (CookieManager) CookieManager.getDefault();
                for (String cookie : cookies.keySet()) {
                    System.out.println(cookie + ": " + cookies.get(cookie));
                    if (cookie.equalsIgnoreCase("csrf-token")) {
                        csrfToken = cookies.get(cookie);
                        try {
                            manager.getCookieStore().add(new URI("https://mewe.com"), new HttpCookie("csrf-token", csrfToken));
                        } catch (URISyntaxException e) {

                        }
                        System.out.println("Updated CSRF Token");
                        serializeCookies();
                    }
                    if (cookie.equalsIgnoreCase("refresh-token")) {
                        refreshToken = cookies.get(cookie);
                        try {
                            manager.getCookieStore().add(new URI("https://mewe.com"), new HttpCookie("refresh-token", refreshToken));
                        } catch (URISyntaxException e) {

                        }
                        System.out.println("Updated Refresh Token");
                        serializeCookies();
                    }
                }
            }
        }
    }

    private void serializeCookies() {
        CookieManager manager = (CookieManager) CookieManager.getDefault();
        File savedCookies = new File("cookies.json");
        JSONObject obj = null;
        try {
            FileUtils.touch(savedCookies);
            obj = new JSONObject();

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            for (HttpCookie httpCookie : manager.getCookieStore().get(new URI("https://mewe.com"))) {
                if (httpCookie.getName().equals("refresh-token") || httpCookie.getName().equals("csrf-token"))
                    obj.put(httpCookie.getName(), httpCookie.getValue());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            Files.write(savedCookies.toPath(), obj.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
