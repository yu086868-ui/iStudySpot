package com.ycyu.istudyspotbackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {

    @Test
    void testJacksonCustomizer() {
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilderCustomizer customizer = config.jacksonCustomizer();

        assertNotNull(customizer);
    }
}