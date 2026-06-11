package com.ycyu.istudyspotbackend.agent.chat;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;

import java.util.List;

public class AgentChatResponse {
    private final String schemaVersion;
    private final String sessionId;
    private final String reply;
    private final AgentToolExecutionResult toolResult;
    private final List<AgentToolExecutionResult> toolResults;
    private final List<String> suggestedPrompts;

    public AgentChatResponse(String sessionId, String reply, AgentToolExecutionResult toolResult,
                             List<AgentToolExecutionResult> toolResults, List<String> suggestedPrompts) {
        this.schemaVersion = "1.0";
        this.sessionId = sessionId;
        this.reply = reply;
        this.toolResult = toolResult;
        this.toolResults = toolResults == null ? List.of() : List.copyOf(toolResults);
        this.suggestedPrompts = suggestedPrompts == null ? List.of() : List.copyOf(suggestedPrompts);
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getReply() {
        return reply;
    }

    public AgentToolExecutionResult getToolResult() {
        return toolResult;
    }

    public List<AgentToolExecutionResult> getToolResults() {
        return toolResults;
    }

    public List<String> getSuggestedPrompts() {
        return suggestedPrompts;
    }
}
