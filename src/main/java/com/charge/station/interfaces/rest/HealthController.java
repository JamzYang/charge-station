package com.charge.station.interfaces.rest;

import com.charge.station.interfaces.dto.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author 架构师团队
 * @version 1.0
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping
    public ApiResult<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("timestamp", Instant.now());
        healthInfo.put("status", "UP");
        healthInfo.put("service", "charge-station-service");
        healthInfo.put("version", "1.0.0");

        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            healthInfo.put("database", "UP");
        } catch (Exception e) {
            log.error("数据库连接检查失败", e);
            healthInfo.put("database", "DOWN");
        }

        // 检查Redis连接
        try {
            stringRedisTemplate.opsForValue().set("health:check", "ok");
            String value = stringRedisTemplate.opsForValue().get("health:check");
            healthInfo.put("redis", "ok".equals(value) ? "UP" : "DOWN");
        } catch (Exception e) {
            log.error("Redis连接检查失败", e);
            healthInfo.put("redis", "DOWN");
        }

        // Kafka连接检查（简单检查）
        try {
            // 这里只是检查KafkaTemplate是否可用，实际的连接检查需要更复杂的逻辑
            healthInfo.put("kafka", kafkaTemplate != null ? "UP" : "DOWN");
        } catch (Exception e) {
            log.error("Kafka连接检查失败", e);
            healthInfo.put("kafka", "DOWN");
        }

        return ApiResult.success(healthInfo);
    }
}
