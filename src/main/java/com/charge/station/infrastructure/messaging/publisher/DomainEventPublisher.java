package com.charge.station.infrastructure.messaging.publisher;

import com.charge.station.domain.event.DomainEvent;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import com.charge.station.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 领域事件发布器
 * 
 * 实现事务性发件箱模式，将领域事件转换为发件箱事件并持久化。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {
    
    private final OutboxEventJpaRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 发布领域事件
     * 
     * 将领域事件转换为发件箱事件并持久化到数据库。
     * 此方法必须在事务内调用，以确保业务操作与事件创建的原子性。
     * 
     * @param domainEvent 领域事件
     */
    @Transactional
    public void publish(DomainEvent domainEvent) {
        try {
            // 将领域事件序列化为JSON
            String payload = objectMapper.writeValueAsString(domainEvent);
            
            // 确定聚合类型
            String aggregateType = determineAggregateType(domainEvent);
            
            // 创建发件箱事件
            OutboxEventEntity outboxEvent = new OutboxEventEntity(
                aggregateType,
                domainEvent.getAggregateId(),
                domainEvent.getEventType(),
                payload
            );
            
            // 持久化到发件箱表
            outboxEventRepository.save(outboxEvent);
            
            log.debug("领域事件已保存到发件箱: eventId={}, eventType={}, aggregateId={}", 
                domainEvent.getEventId(), domainEvent.getEventType(), domainEvent.getAggregateId());
                
        } catch (JsonProcessingException e) {
            log.error("序列化领域事件失败: eventId={}, eventType={}", 
                domainEvent.getEventId(), domainEvent.getEventType(), e);
            throw new RuntimeException("序列化领域事件失败", e);
        }
    }
    
    /**
     * 批量发布领域事件
     * 
     * @param domainEvents 领域事件列表
     */
    @Transactional
    public void publishAll(Iterable<DomainEvent> domainEvents) {
        for (DomainEvent domainEvent : domainEvents) {
            publish(domainEvent);
        }
    }
    
    /**
     * 根据领域事件确定聚合类型
     * 
     * @param domainEvent 领域事件
     * @return 聚合类型
     */
    private String determineAggregateType(DomainEvent domainEvent) {
        String eventType = domainEvent.getEventType();
        
        if (eventType.startsWith("station.")) {
            return "Station";
        } else if (eventType.startsWith("chargepoint.")) {
            return "ChargePoint";
        } else {
            // 默认使用事件类型的第一部分
            String[] parts = eventType.split("\\.");
            return parts.length > 0 ? parts[0] : "Unknown";
        }
    }
}
