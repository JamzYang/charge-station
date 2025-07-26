package com.charge.station.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 远程停止交易指令载荷
 * 
 * 符合OCPP协议的RemoteStopTransaction指令格式。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteStopTransactionInstruction {
    
    /**
     * 交易ID
     */
    @JsonProperty("transactionId")
    private Integer transactionId;
    
    /**
     * 创建远程停止交易指令
     * 
     * @param transactionId 交易ID
     * @return RemoteStopTransactionInstruction实例
     */
    public static RemoteStopTransactionInstruction of(Integer transactionId) {
        return new RemoteStopTransactionInstruction(transactionId);
    }
}
