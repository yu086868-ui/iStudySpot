package com.ycyu.istudyspotbackend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.config.WxConfig;
import com.ycyu.istudyspotbackend.service.WxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxServiceImpl implements WxService {

    private static final Logger logger = LoggerFactory.getLogger(WxServiceImpl.class);
    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WxConfig wxConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getOpenIdByCode(String code) {
        // 测试模式：code 以 "test_" 开头时，直接返回 openId，不调用微信API
        if (code.startsWith("test_")) {
            logger.info("测试模式登录, openId: {}", code.substring(5));
            return code.substring(5);
        }

        try {
            String url = WX_LOGIN_URL + "?appid=" + wxConfig.getAppId()
                    + "&secret=" + wxConfig.getAppSecret()
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                logger.error("微信登录失败: {}", errmsg);
                throw new RuntimeException("微信登录失败: " + errmsg);
            }

            if (!jsonNode.has("openid")) {
                throw new RuntimeException("微信登录失败: 未获取到openid");
            }

            return jsonNode.get("openid").asText();
        } catch (Exception e) {
            logger.error("调用微信API失败", e);
            throw new RuntimeException("调用微信API失败: " + e.getMessage());
        }
    }
}
