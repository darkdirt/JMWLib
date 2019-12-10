package com.scorchedcode.JMWLib.model;

import com.scorchedcode.JMWLib.JMW;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

public class Group {
    private String id, name, description, type, url, chatmode;
    private int members;
    private boolean publically, applypublic, indirectory;
    public Group(JSONObject data) {
        id = data.getString("id");
        name = data.getString("name");
        chatmode = data.getString("chatMode");
        publically = (data.has("isPublic") ? data.getBoolean("isPublic") : false);
        description = data.getString("descriptionPlain");
        members = data.getInt("membersCount");
        applypublic = (data.has("isPublicApply") ? data.getBoolean("isPublicApply") : false);
        indirectory = (data.has("showInPublicDirectory") ? data.getBoolean("showInPublicDirectory") : false);
        url = (data.has("publicUrl") ? data.getString("publicUrl") : "Not applicable");
        type = data.getString("groupThematicType");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public int getMemberCount() {
        return members;
    }

    public boolean isPublic() {
        return publically;
    }

    public boolean isApplyPublic() {
        return applypublic;
    }

    public boolean isInDirectory() {
        return indirectory;
    }

    public boolean isChatEnabled() {
        return (!chatmode.equals("off"));
    }

    public ChatHistory getChatHistory(int limit) {
        return new ChatHistory(JMW.getInstance().submitQuery("https://mewe.com/api/v2/chat/thread/"+getId(), "offset,0", "limit,"+limit));
    }

    public void sendChatMessage(InputStream image) {
        JSONObject payload = new JSONObject();
        JSONArray attachments = new JSONArray();
        JSONObject response = JMW.getInstance().submitPayload("https://mewe.com/api/v2/attachment/upload/gc", image);
        attachments.put(response.getString("id"));
        payload.put("attachments", attachments);
        payload.put("setAsRead", true);
        JMW.getInstance().submitPayload("https://mewe.com/api/v2/chat/thread/"+getId()+"/message", payload);
    }

    public void sendChatMessage(String message) {
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("setAsRead", true);
        JMW.getInstance().submitPayload("https://mewe.com/api/v2/chat/thread/"+getId()+"/message", payload);
    }
}
