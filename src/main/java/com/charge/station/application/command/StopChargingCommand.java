package com.charge.station.application.command;

import com.charge.station.domain.model.chargepoint.ChargePointId;

/**
 * 停止充电命令
 * 
 * 封装远程停止充电所需的所有参数。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StopChargingCommand(
    ChargePointId chargePointId,
    Integer transactionId
) {
    
    /**
     * 创建停止充电命令
     * 
     * @param chargePointId 充电桩ID
     * @param transactionId 交易ID
     * @return StopChargingCommand实例
     */
    public static StopChargingCommand of(ChargePointId chargePointId, Integer transactionId) {
        return new StopChargingCommand(chargePointId, transactionId);
    }
}
