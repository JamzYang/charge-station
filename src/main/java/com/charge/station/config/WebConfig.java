package com.charge.station.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yangshen
 * @createTime 2025年08月01日 18:14:00
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // 允许所有路径
        .allowedOrigins("*") // 允许的源
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
        .allowedHeaders("*") // 允许所有请求头
        .allowCredentials(false) // 允许发送Cookie
        .maxAge(3600); // 预检请求的缓存时间（秒）
  }
}
