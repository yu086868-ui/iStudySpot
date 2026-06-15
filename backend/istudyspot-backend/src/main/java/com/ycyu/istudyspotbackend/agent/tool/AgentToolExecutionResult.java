package com.ycyu.istudyspotbackend.agent.tool;

import java.util.Map;

public class AgentToolExecutionResult {
    private final String schemaVersion;
    private final String referenceScope;
    private final String tool;
    private final String summary;
    private final Map<String, Object> data;
    private final Map<String, Object> uiAction;
    private final Map<String, Object> references;

    public AgentToolExecutionResult(String tool, String summary, Map<String, Object> data,
                                    Map<String, Object> uiAction, Map<String, Object> references) {
        this.schemaVersion = "1.0";
        this.referenceScope = "response";
        this.tool = tool;
        this.summary = summary;
        this.data = data;
        this.uiAction = uiAction;
        this.references = references;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getReferenceScope() {
        return referenceScope;
    }

    public String getTool() {
        return tool;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getUiAction() {
        return uiAction;
    }

    public Map<String, Object> getReferences() {
        return references;
    }
}
