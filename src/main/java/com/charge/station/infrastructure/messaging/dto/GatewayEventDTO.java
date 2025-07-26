package com.charge.station.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 网关事件DTO
 * 
 * 表示从充电桩网关接收的标准化事件数据。
 * 
 * @param <T> 事件载荷类型
 * @author 架构师团队
 * @version 1.0
 */
@Data
@NoArgsConstructor
public class GatewayEventDTO<T> {
    
    /**
     * 事件唯一ID
     */
    @JsonProperty("eventId")
    private String eventId;
    
    /**
     * 事件类型
     */
    @JsonProperty("eventType")
    private String eventType;
    
    /**
     * 充电桩ID
     */
    @JsonProperty("chargePointId")
    private String chargePointId;
    
    /**
     * 网关实例ID
     */
    @JsonProperty("gatewayId")
    private String gatewayId;
    
    /**
     * 事件发生时间
     */
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    /**
     * 事件载荷
     */
    @JsonProperty("payload")
    private T payload;
    
    /**
     * 创建网关事件DTO
     * 
     * @param eventId 事件ID
     * @param eventType 事件类型
     * @param chargePointId 充电桩ID
     * @param gatewayId 网关ID
     * @param timestamp 时间戳
     * @param payload 载荷
     */
    public GatewayEventDTO(String eventId, String eventType, String chargePointId, 
                          String gatewayId, Instant timestamp, T payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.chargePointId = chargePointId;
        this.gatewayId = gatewayId;
        this.timestamp = timestamp;
        this.payload = payload;
    }
}
