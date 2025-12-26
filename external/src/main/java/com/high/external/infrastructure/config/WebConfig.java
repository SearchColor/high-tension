package com.high.external.infrastructure.config;

import com.high.external.infrastructure.monitoring.ApiMonitoringInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiMonitoringInterceptor apiMonitoringInterceptor;

    @Autowired
    public WebConfig(ApiMonitoringInterceptor apiMonitoringInterceptor) {
        this.apiMonitoringInterceptor = apiMonitoringInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiMonitoringInterceptor)
                .addPathPatterns("/**")
        ;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}