package com.ycyu.istudyspotbackend.entity;

import java.util.ArrayList;
import java.util.List;

public class Session {
    private String session_id;
    private String character_id;
    private List<Message> messages;

    public Session() {
        this.messages = new ArrayList<>();
    }

    public Session(String session_id, String character_id) {
        this.session_id = session_id;
        this.character_id = character_id;
        this.messages = new ArrayList<>();
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getCharacter_id() {
        return character_id;
    }

    public void setCharacter_id(String character_id) {
        this.character_id = character_id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public List<Message> getRecentMessages(int limit) {
        if (messages.size() <= limit) {
            return messages;
        }
        return messages.subList(messages.size() - limit, messages.size());
    }
}
