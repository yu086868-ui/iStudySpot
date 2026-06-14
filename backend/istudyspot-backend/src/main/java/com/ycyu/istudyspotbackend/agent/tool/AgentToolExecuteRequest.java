package com.ycyu.istudyspotbackend.agent.tool;

import java.util.HashMap;
import java.util.Map;

public class AgentToolExecuteRequest {
    private String tool;
    private Map<String, Object> arguments = new HashMap<>();

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}
