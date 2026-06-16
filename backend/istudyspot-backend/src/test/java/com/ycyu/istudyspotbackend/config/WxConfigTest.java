package com.ycyu.istudyspotbackend.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WxConfigTest {

    @Test
    void testGettersAndSetters() {
        WxConfig config = new WxConfig();
        config.setAppId("wx-app-id");
        config.setAppSecret("wx-app-secret");

        assertEquals("wx-app-id", config.getAppId());
        assertEquals("wx-app-secret", config.getAppSecret());
    }
}