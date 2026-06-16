package com.ycyu.istudyspotbackend.config;

import com.ycyu.istudyspotbackend.interceptor.JwtInterceptor;
import com.ycyu.istudyspotbackend.interceptor.MetricsInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private MetricsInterceptor metricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health/**", "/api/wx/card/image/**");
        
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/api/user/wxlogin",
                        "/api/test",
                        "/api/studyrooms",
                        "/api/studyrooms/**",
                        "/api/seats",
                        "/api/seats/**",
                        "/api/announcements",
                        "/api/announcements/**",
                        "/api/rules",
                        "/api/rules/**",
                        "/api/characters",
                        "/api/card/**",
                        // 微信小程序端无需JWT的接口
                        "/api/wx/user/login",
                        "/api/wx/studyrooms",
                        "/api/wx/studyrooms/**",
                        "/api/wx/seats",
                        "/api/wx/seats/**",
                        "/api/wx/announcements",
                        "/api/wx/announcements/**",
                        "/api/wx/rules",
                        "/api/wx/rules/**",
                        "/api/wx/reservations/rules",
                        "/api/wx/reservations/{id}",
                        "/api/wx/card/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .allowCredentials(true)
                .maxAge(3600);
    }
}