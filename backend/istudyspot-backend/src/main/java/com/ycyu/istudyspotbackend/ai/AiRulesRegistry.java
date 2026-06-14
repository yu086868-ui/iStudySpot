package com.ycyu.istudyspotbackend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public final class AiRulesRegistry {
    private static final String RESOURCE_PATH = "ai-rules.json";
    private static final AiRules RULES = loadRules();

    private AiRulesRegistry() {
    }

    public static AiRules getRules() {
        return RULES;
    }

    private static AiRules loadRules() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        try (InputStream inputStream = new ClassPathResource(RESOURCE_PATH).getInputStream()) {
            return objectMapper.readValue(inputStream, AiRules.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load AI rules from " + RESOURCE_PATH, e);
        }
    }
}
