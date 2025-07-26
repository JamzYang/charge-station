package com.charge.station.infrastructure.messaging.kafka.consumer;

import com.charge.station.application.handler.DeviceStatusEventHandler;
import com.charge.station.infrastructure.messaging.dto.GatewayEventDTO;
import com.charge.station.shared.constant.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 设备事件消费者
 *
 * 监听来自充电桩网关的上行事件，并路由到相应的事件处理器。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class DeviceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeviceEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final DeviceStatusEventHandler deviceStatusEventHandler;

    public DeviceEventConsumer(ObjectMapper objectMapper,
                              DeviceStatusEventHandler deviceStatusEventHandler) {
        this.objectMapper = objectMapper;
        this.deviceStatusEventHandler = deviceStatusEventHandler;
    }
    
    /**
     * 消费网关上行事件
     * 
     * @param message 事件消息JSON字符串
     * @param partition 分区号
     * @param offset 偏移量
     * @param acknowledgment 确认对象
     */
    @KafkaListener(
        topics = KafkaTopics.OCPP_EVENTS_UP,
        groupId = "station-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGatewayEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.debug("收到网关事件: partition={}, offset={}, message={}", partition, offset, message);
        
        try {
            // 解析事件消息
            GatewayEventDTO<?> event = parseEvent(message);
            
            if (event == null) {
                log.warn("解析网关事件失败，消息为空: partition={}, offset={}", partition, offset);
                acknowledgment.acknowledge();
                return;
            }
            
            // 根据事件类型路由到不同的处理器
            routeEvent(event);
            
            // 手动确认消息
            acknowledgment.acknowledge();
            
            log.debug("网关事件处理完成: eventType={}, chargePointId={}, partition={}, offset={}", 
                event.getEventType(), event.getChargePointId(), partition, offset);
                
        } catch (Exception e) {
            log.error("处理网关事件失败: partition={}, offset={}, message={}", partition, offset, message, e);
            
            // 对于处理失败的消息，仍然确认以避免重复消费
            // 在生产环境中，可以考虑将失败的消息发送到死信队列
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * 解析事件消息
     * 
     * @param message JSON消息字符串
     * @return 解析后的事件对象
     */
    private GatewayEventDTO<?> parseEvent(String message) {
        try {
            return objectMapper.readValue(message, GatewayEventDTO.class);
        } catch (JsonProcessingException e) {
            log.error("解析网关事件JSON失败: message={}", message, e);
            return null;
        }
    }
    
    /**
     * 根据事件类型路由到相应的处理器
     * 
     * @param event 网关事件
     */
    private void routeEvent(GatewayEventDTO<?> event) {
        String eventType = event.getEventType();
        
        if (eventType == null) {
            log.warn("事件类型为空，跳过处理: eventId={}", event.getEventId());
            return;
        }
        
        switch (eventType) {
            case "connector.status_changed":
                deviceStatusEventHandler.handleStatusNotification(event);
                break;
                
            case "charge_point.connected":
                deviceStatusEventHandler.handleChargePointConnected(event);
                break;
                
            case "charge_point.disconnected":
                deviceStatusEventHandler.handleChargePointDisconnected(event);
                break;
                
            default:
                log.debug("未知的事件类型，跳过处理: eventType={}, eventId={}", eventType, event.getEventId());
                break;
        }
    }
}
