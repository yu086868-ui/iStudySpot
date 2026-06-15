package com.ycyu.istudyspotbackend.agent.chat;

import java.util.List;

public class AgentReplyBlock {
    private final String type;
    private final String text;
    private final List<String> items;

    public AgentReplyBlock(String type, String text, List<String> items) {
        this.type = type;
        this.text = text;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public static AgentReplyBlock paragraph(String text) {
        return new AgentReplyBlock("paragraph", text, List.of());
    }

    public static AgentReplyBlock bullet(List<String> items) {
        return new AgentReplyBlock("bullet", null, items);
    }

    public static AgentReplyBlock numbered(List<String> items) {
        return new AgentReplyBlock("numbered", null, items);
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public List<String> getItems() {
        return items;
    }
}
