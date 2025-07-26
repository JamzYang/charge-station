package com.charge.station.infrastructure.messaging.kafka.producer;

import com.charge.station.infrastructure.messaging.event.IntegrationEvent;
import com.charge.station.infrastructure.messaging.mapper.IntegrationEventMapper;
import com.charge.station.shared.constant.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 集成事件生产者
 * 
 * 负责将集成事件发布到Kafka消息队列。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IntegrationEventMapper eventMapper;
    
    /**
     * 发送集成事件
     * 
     * @param integrationEvent 集成事件
     */
    public void send(IntegrationEvent integrationEvent) {
        try {
            String topic = determineTopicByEventType(integrationEvent.eventType());
            String key = integrationEvent.aggregateId();
            String message = eventMapper.toJson(integrationEvent);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("集成事件发送成功: eventId={}, eventType={}, topic={}, partition={}, offset={}", 
                        integrationEvent.eventId(), 
                        integrationEvent.eventType(),
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("集成事件发送失败: eventId={}, eventType={}, topic={}", 
                        integrationEvent.eventId(), 
                        integrationEvent.eventType(),
                        topic, ex);
                    throw new RuntimeException("集成事件发送失败", ex);
                }
            });
            
        } catch (Exception e) {
            log.error("发送集成事件时发生异常: eventId={}, eventType={}", 
                integrationEvent.eventId(), integrationEvent.eventType(), e);
            throw new RuntimeException("发送集成事件失败", e);
        }
    }
    
    /**
     * 根据事件类型确定目标Topic
     * 
     * @param eventType 事件类型
     * @return Kafka Topic名称
     */
    private String determineTopicByEventType(String eventType) {
        // 目前所有集成事件都发送到同一个Topic
        // 后续可以根据事件类型路由到不同的Topic
        return KafkaTopics.INTEGRATION_EVENTS;
    }
}
