package com.charge.station.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 远程启动交易指令载荷
 * 
 * 符合OCPP协议的RemoteStartTransaction指令格式。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteStartTransactionInstruction {
    
    /**
     * 连接器ID
     */
    @JsonProperty("connectorId")
    private Integer connectorId;
    
    /**
     * 用户标识
     */
    @JsonProperty("idTag")
    private String idTag;
    
    /**
     * 充电配置（可选）
     */
    @JsonProperty("chargingProfile")
    private Object chargingProfile;
    
    /**
     * 创建远程启动交易指令
     * 
     * @param connectorId 连接器ID
     * @param idTag 用户标识
     * @return RemoteStartTransactionInstruction实例
     */
    public static RemoteStartTransactionInstruction of(Integer connectorId, String idTag) {
        return new RemoteStartTransactionInstruction(connectorId, idTag, null);
    }
    
    /**
     * 创建带充电配置的远程启动交易指令
     * 
     * @param connectorId 连接器ID
     * @param idTag 用户标识
     * @param chargingProfile 充电配置
     * @return RemoteStartTransactionInstruction实例
     */
    public static RemoteStartTransactionInstruction of(Integer connectorId, String idTag, Object chargingProfile) {
        return new RemoteStartTransactionInstruction(connectorId, idTag, chargingProfile);
    }
}
