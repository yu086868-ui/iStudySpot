package com.ycyu.istudyspotbackend.service;

public interface AlertService {
    
    void checkErrorRate();
    
    void checkResponseTime();
    
    void checkServiceHealth();
}
