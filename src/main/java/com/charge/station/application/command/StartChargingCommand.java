package com.charge.station.application.command;

import com.charge.station.domain.model.chargepoint.ChargePointId;

/**
 * 启动充电命令
 * 
 * 封装远程启动充电所需的所有参数。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StartChargingCommand(
    ChargePointId chargePointId,
    Integer connectorId,
    String idTag
) {
    
    /**
     * 创建启动充电命令
     * 
     * @param chargePointId 充电桩ID
     * @param connectorId 连接器ID
     * @param idTag 用户标识
     * @return StartChargingCommand实例
     */
    public static StartChargingCommand of(ChargePointId chargePointId, Integer connectorId, String idTag) {
        return new StartChargingCommand(chargePointId, connectorId, idTag);
    }

    /**
     * 创建启动充电命令（使用默认连接器）
     * 
     * @param chargePointId 充电桩ID
     * @param idTag 用户标识
     * @return StartChargingCommand实例
     */
    public static StartChargingCommand of(ChargePointId chargePointId, String idTag) {
        return new StartChargingCommand(chargePointId, 1, idTag); // 默认使用连接器1
    }
}
