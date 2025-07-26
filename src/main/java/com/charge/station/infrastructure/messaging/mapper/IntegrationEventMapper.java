package com.charge.station.infrastructure.messaging.mapper;

import com.charge.station.infrastructure.messaging.event.IntegrationEvent;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 集成事件映射器
 * 
 * 负责将发件箱事件转换为集成事件。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationEventMapper {
    
    private final ObjectMapper objectMapper;
    
    /**
     * 将发件箱事件转换为集成事件
     * 
     * @param outboxEvent 发件箱事件
     * @return 集成事件
     */
    public IntegrationEvent fromOutboxEvent(OutboxEventEntity outboxEvent) {
        try {
            // 解析payload为JsonNode
            JsonNode payloadNode = objectMapper.readTree(outboxEvent.getPayload());
            
            return new IntegrationEvent(
                outboxEvent.getId(),
                outboxEvent.getEventType(),
                1, // 默认版本号
                outboxEvent.getAggregateId(),
                outboxEvent.getCreatedAt(),
                null, // 暂时不使用correlationId
                payloadNode
            );
            
        } catch (JsonProcessingException e) {
            log.error("解析发件箱事件payload失败: eventId={}, eventType={}", 
                outboxEvent.getId(), outboxEvent.getEventType(), e);
            throw new RuntimeException("解析发件箱事件payload失败", e);
        }
    }
    
    /**
     * 将集成事件转换为JSON字符串
     * 
     * @param integrationEvent 集成事件
     * @return JSON字符串
     */
    public String toJson(IntegrationEvent integrationEvent) {
        try {
            return objectMapper.writeValueAsString(integrationEvent);
        } catch (JsonProcessingException e) {
            log.error("序列化集成事件失败: eventId={}, eventType={}", 
                integrationEvent.eventId(), integrationEvent.eventType(), e);
            throw new RuntimeException("序列化集成事件失败", e);
        }
    }
}
