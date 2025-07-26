package com.charge.station.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 集成事件基类
 * 
 * 定义跨服务通信的事件契约，保持向后兼容性。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record IntegrationEvent(
    UUID eventId,
    String eventType,
    int eventVersion,
    String aggregateId,
    Instant occurredOn,
    String correlationId,
    Object payload
) {
    
    /**
     * 创建集成事件
     * 
     * @param eventType 事件类型
     * @param aggregateId 聚合ID
     * @param payload 事件负载
     * @return 集成事件
     */
    public static IntegrationEvent create(String eventType, String aggregateId, Object payload) {
        return new IntegrationEvent(
            UUID.randomUUID(),
            eventType,
            1, // 默认版本号
            aggregateId,
            Instant.now(),
            null, // 暂时不使用correlationId
            payload
        );
    }
    
    /**
     * 创建带相关ID的集成事件
     * 
     * @param eventType 事件类型
     * @param aggregateId 聚合ID
     * @param payload 事件负载
     * @param correlationId 相关ID
     * @return 集成事件
     */
    public static IntegrationEvent create(String eventType, String aggregateId, Object payload, String correlationId) {
        return new IntegrationEvent(
            UUID.randomUUID(),
            eventType,
            1,
            aggregateId,
            Instant.now(),
            correlationId,
            payload
        );
    }
}
