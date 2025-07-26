package com.charge.station.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 网关指令DTO
 * 
 * 表示发送给充电桩网关的标准化指令数据。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayCommandDTO {
    
    /**
     * 指令唯一ID
     */
    @JsonProperty("commandId")
    private String commandId;
    
    /**
     * 指令类型
     */
    @JsonProperty("commandType")
    private String commandType;
    
    /**
     * 充电桩ID
     */
    @JsonProperty("chargePointId")
    private String chargePointId;
    
    /**
     * 指令载荷
     */
    @JsonProperty("payload")
    private Object payload;
    
    /**
     * 指令发送时间
     */
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    /**
     * 追踪ID（用于分布式追踪）
     */
    @JsonProperty("traceId")
    private String traceId;
    
    /**
     * 创建网关指令DTO
     * 
     * @param commandId 指令ID
     * @param commandType 指令类型
     * @param chargePointId 充电桩ID
     * @param payload 指令载荷
     * @return GatewayCommandDTO实例
     */
    public static GatewayCommandDTO of(String commandId, String commandType, String chargePointId, Object payload) {
        return new GatewayCommandDTO(commandId, commandType, chargePointId, payload, Instant.now(), null);
    }
    
    /**
     * 创建带追踪ID的网关指令DTO
     * 
     * @param commandId 指令ID
     * @param commandType 指令类型
     * @param chargePointId 充电桩ID
     * @param payload 指令载荷
     * @param traceId 追踪ID
     * @return GatewayCommandDTO实例
     */
    public static GatewayCommandDTO of(String commandId, String commandType, String chargePointId, Object payload, String traceId) {
        return new GatewayCommandDTO(commandId, commandType, chargePointId, payload, Instant.now(), traceId);
    }
}
