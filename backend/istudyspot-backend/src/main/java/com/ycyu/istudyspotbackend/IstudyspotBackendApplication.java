package com.ycyu.istudyspotbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ycyu.istudyspotbackend.mapper")
@EnableScheduling
public class IstudyspotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IstudyspotBackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("iStudySpot 后端启动成功！");
        System.out.println("API地址: http://localhost:8080/api/test");
        System.out.println("========================================");
    }
}
