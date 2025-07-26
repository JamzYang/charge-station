package com.charge.station.interfaces.dto.request;

import jakarta.validation.constraints.*;

/**
 * 停止充电请求DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StopChargingRequest(
    @NotNull(message = "交易ID不能为空")
    @Min(value = 1, message = "交易ID必须大于0")
    Integer transactionId
) {
    
    /**
     * 创建停止充电请求
     * 
     * @param transactionId 交易ID
     * @return StopChargingRequest实例
     */
    public static StopChargingRequest of(Integer transactionId) {
        return new StopChargingRequest(transactionId);
    }
}
