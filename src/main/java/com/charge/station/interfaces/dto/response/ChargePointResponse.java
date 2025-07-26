package com.charge.station.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 充电桩响应DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record ChargePointResponse(
    String chargePointId,
    String stationId,
    String name,
    String model,
    String vendor,
    String serialNumber,
    String firmwareVersion,
    String status,
    String statusDescription,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant lastHeartbeat,
    BigDecimal maxPower,
    String maxPowerFormatted,
    boolean isOnline,
    boolean isAvailableForCharging,
    boolean hasAvailableConnector,
    int availableConnectorCount,
    List<ConnectorResponse> connectors,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant updatedAt
) {
    
    /**
     * 创建充电桩响应
     * 
     * @param chargePointId 充电桩ID
     * @param stationId 充电站ID
     * @param name 名称
     * @param model 型号
     * @param vendor 厂商
     * @param serialNumber 序列号
     * @param firmwareVersion 固件版本
     * @param status 状态代码
     * @param statusDescription 状态描述
     * @param lastHeartbeat 最后心跳时间
     * @param maxPower 最大功率
     * @param maxPowerFormatted 格式化的最大功率
     * @param isOnline 是否在线
     * @param isAvailableForCharging 是否可用于充电
     * @param hasAvailableConnector 是否有可用连接器
     * @param availableConnectorCount 可用连接器数量
     * @param connectors 连接器列表
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @return ChargePointResponse实例
     */
    public static ChargePointResponse of(String chargePointId, String stationId, String name,
                                       String model, String vendor, String serialNumber,
                                       String firmwareVersion, String status, String statusDescription,
                                       Instant lastHeartbeat, BigDecimal maxPower, String maxPowerFormatted,
                                       boolean isOnline, boolean isAvailableForCharging,
                                       boolean hasAvailableConnector, int availableConnectorCount,
                                       List<ConnectorResponse> connectors,
                                       Instant createdAt, Instant updatedAt) {
        return new ChargePointResponse(chargePointId, stationId, name, model, vendor, serialNumber,
                                     firmwareVersion, status, statusDescription, lastHeartbeat,
                                     maxPower, maxPowerFormatted, isOnline, isAvailableForCharging,
                                     hasAvailableConnector, availableConnectorCount, connectors,
                                     createdAt, updatedAt);
    }

    /**
     * 创建简化的充电桩响应
     * 
     * @param chargePointId 充电桩ID
     * @param stationId 充电站ID
     * @param name 名称
     * @param status 状态代码
     * @param statusDescription 状态描述
     * @param isOnline 是否在线
     * @param isAvailableForCharging 是否可用于充电
     * @param availableConnectorCount 可用连接器数量
     * @return ChargePointResponse实例
     */
    public static ChargePointResponse simple(String chargePointId, String stationId, String name,
                                           String status, String statusDescription,
                                           boolean isOnline, boolean isAvailableForCharging,
                                           int availableConnectorCount) {
        return new ChargePointResponse(chargePointId, stationId, name, null, null, null, null,
                                     status, statusDescription, null, null, null,
                                     isOnline, isAvailableForCharging, availableConnectorCount > 0,
                                     availableConnectorCount, null, null, null);
    }
}
