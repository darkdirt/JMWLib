package com.scorchedcode.JMWLib.model;

import com.scorchedcode.JMWLib.JMW;
import org.json.JSONObject;

public class Message {

    private String author, authorID, msgID, text, threadID;
    private long date;
    private String imageUrl;
    public Message(JSONObject data) {
        author = data.getJSONObject("author").getString("name");
        authorID = data.getString("authorId");
        msgID = data.getString("id");
        text = data.getString("text");
        threadID = data.getString("threadId");
        date = data.getLong("date");
        if(data.has("attachments") && ((JSONObject)data.getJSONArray("attachments").get(0)).getString("aType").equals("photo")) {
            JSONObject photo = ((JSONObject) data.getJSONArray("attachments").get(0));
            imageUrl = "https://img.mewe.com/" + photo.getJSONObject("_links").getJSONObject("self").getString("href").replaceAll("\\{imageSize}", "1600x1600");
        }
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorID() {
        return authorID;
    }

    public String getMsgID() {
        return msgID;
    }

    public String getText() {
        return text;
    }

    public String getThreadID() {
        return threadID;
    }

    public long getDate() {
        return date;
    }

    public Group getGroup() {
        return JMW.getInstance().getGroup(getThreadID());
    }

    public User getUser() {
        return JMW.getInstance().getUser(getAuthorID());
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
