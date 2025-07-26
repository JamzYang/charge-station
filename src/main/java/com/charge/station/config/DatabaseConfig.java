package com.charge.station.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置类
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.charge.station.infrastructure.persistence")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    
    // JPA配置已在application.yml中完成
    // 这里可以添加自定义的数据库配置
}
