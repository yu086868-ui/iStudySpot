package com.ycyu.istudyspotbackend.agent.chat;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;

import java.util.List;
import java.util.Map;

public class AgentChatResponse {
    private final String schemaVersion;
    private final String sessionId;
    private final String reply;
    private final String replyText;
    private final String replyFormat;
    private final List<AgentReplyBlock> blocks;
    private final AgentToolExecutionResult toolResult;
    private final List<AgentToolExecutionResult> toolResults;
    private final Map<String, Object> uiAction;
    private final List<String> suggestedPrompts;

    public AgentChatResponse(String sessionId, String reply, AgentToolExecutionResult toolResult,
                             List<AgentToolExecutionResult> toolResults, List<String> suggestedPrompts) {
        this(sessionId, reply, toolResult, toolResults, null, suggestedPrompts);
    }

    public AgentChatResponse(String sessionId, String reply, AgentToolExecutionResult toolResult,
                             List<AgentToolExecutionResult> toolResults, Map<String, Object> uiAction,
                             List<String> suggestedPrompts) {
        this.schemaVersion = "1.0";
        this.sessionId = sessionId;
        this.reply = reply;
        this.replyText = AgentReplyFormatter.toPlainText(reply);
        this.replyFormat = "plain";
        this.blocks = AgentReplyFormatter.toBlocks(reply);
        this.toolResult = toolResult;
        this.toolResults = toolResults == null ? List.of() : List.copyOf(toolResults);
        this.uiAction = uiAction == null ? Map.of() : Map.copyOf(uiAction);
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

    public String getReplyText() {
        return replyText;
    }

    public String getReplyFormat() {
        return replyFormat;
    }

    public List<AgentReplyBlock> getBlocks() {
        return blocks;
    }

    public AgentToolExecutionResult getToolResult() {
        return toolResult;
    }

    public List<AgentToolExecutionResult> getToolResults() {
        return toolResults;
    }

    public Map<String, Object> getUiAction() {
        return uiAction;
    }

    public List<String> getSuggestedPrompts() {
        return suggestedPrompts;
    }
}
