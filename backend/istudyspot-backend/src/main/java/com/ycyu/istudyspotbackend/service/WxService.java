package com.ycyu.istudyspotbackend.service;

public interface WxService {
    /**
     * 通过微信code获取openId
     * @param code 微信登录临时code
     * @return openId
     */
    String getOpenIdByCode(String code);
}
