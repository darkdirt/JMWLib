package com.scorchedcode.JMWLib;

import com.scorchedcode.JMWLib.event.MeWeEventListener;
import com.scorchedcode.JMWLib.model.Group;
import com.scorchedcode.JMWLib.model.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class MeWeSocket extends WebSocketClient {
    public MeWeSocket(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connection opened: " + handshakedata.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        JSONObject response = new JSONObject();
        response.put("message", "pong");
        send(response.toString()
        );
        JSONObject obj = new JSONObject(message);
        if(obj.has("msgType") && obj.getString("msgType").equals("GroupChatMessage")) {
            String msgID = obj.getJSONObject("data").getString("id");
            try {
                for (Group g : JMW.getInstance().getGroups()) {
                    if (g.isChatEnabled()) {
                        ArrayList<Message> msgs = g.getChatHistory(5).getMessages();
                        for(Message msg : msgs) {
                            if (msg.getMsgID().equals(msgID)) {
                                for (MeWeEventListener e : JMW.registeredListeners)
                                    e.onGroupMessageReceived(msg);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
