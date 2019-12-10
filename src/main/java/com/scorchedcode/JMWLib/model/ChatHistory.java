package com.scorchedcode.JMWLib.model;

import org.json.JSONObject;

import java.util.ArrayList;

public class ChatHistory {

    private ArrayList<Message> messages = new ArrayList<>();

    public ChatHistory(JSONObject data) {
        for(Object msg : data.getJSONArray("messages"))
            messages.add(new Message((JSONObject)msg));
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
}
