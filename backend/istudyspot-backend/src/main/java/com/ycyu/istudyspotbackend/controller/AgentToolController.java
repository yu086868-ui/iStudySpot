package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecuteRequest;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolService;
import com.ycyu.istudyspotbackend.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent/tools")
public class AgentToolController {

    @Autowired
    private AgentToolService agentToolService;

    @GetMapping("/catalog")
    public Result<List<?>> getCatalog() {
        return Result.success("success", agentToolService.getCatalog());
    }

    @PostMapping("/execute")
    public Result<?> execute(
            @RequestBody AgentToolExecuteRequest request,
            HttpServletRequest servletRequest) {
        try {
            Long userId = (Long) servletRequest.getAttribute("userId");
            AgentToolExecutionResult result = agentToolService.execute(userId, request);
            return Result.success("success", result);
        } catch (IllegalArgumentException e) {
            return errorResult(resolveStatus(e.getMessage()), e.getMessage(), false);
        } catch (RuntimeException e) {
            return errorResult(500, e.getMessage(), true);
        }
    }

    private int resolveStatus(String errorCode) {
        if ("AUTH_REQUIRED".equals(errorCode)) {
            return 401;
        }
        return 400;
    }

    private Result<Map<String, Object>> errorResult(int code, String errorCode, boolean retryable) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", errorCode);
        error.put("retryable", retryable);
        String field = extractField(errorCode);
        if (field != null) {
            error.put("field", field);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schemaVersion", "1.0");
        payload.put("error", error);

        return new Result<>(code, errorCode, payload);
    }

    private String extractField(String errorCode) {
        if (errorCode == null || !errorCode.startsWith("MISSING_")) {
            return null;
        }
        String raw = errorCode.substring("MISSING_".length()).toLowerCase();
        return switch (raw) {
            case "studyroomid" -> "studyRoomId";
            default -> raw;
        };
    }
}
