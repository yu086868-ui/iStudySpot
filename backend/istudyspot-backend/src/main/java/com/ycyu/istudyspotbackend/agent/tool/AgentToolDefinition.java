package com.ycyu.istudyspotbackend.agent.tool;

import java.util.List;
import java.util.Map;

public class AgentToolDefinition {
    private final String name;
    private final String title;
    private final String description;
    private final boolean requiresAuth;
    private final List<String> tags;
    private final Map<String, Object> inputSchema;

    public AgentToolDefinition(String name, String title, String description,
                               boolean requiresAuth, List<String> tags, Map<String, Object> inputSchema) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.requiresAuth = requiresAuth;
        this.tags = tags;
        this.inputSchema = inputSchema;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }
}
