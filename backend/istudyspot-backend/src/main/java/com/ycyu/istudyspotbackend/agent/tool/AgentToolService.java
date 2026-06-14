package com.ycyu.istudyspotbackend.agent.tool;

import java.util.List;

public interface AgentToolService {
    List<AgentToolDefinition> getCatalog();

    AgentToolExecutionResult execute(Long userId, AgentToolExecuteRequest request);
}
