package com.charge.station.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 充电枪响应DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record ConnectorResponse(
    Integer connectorId,
    String connectorType,
    String connectorTypeDescription,
    String status,
    String statusDescription,
    BigDecimal maxPower,
    String maxPowerFormatted,
    boolean isAvailableForCharging,
    boolean isCharging,
    boolean isOffline,
    boolean isFastCharging,
    boolean isSuperCharging,
    String displayName,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant updatedAt
) {
    
    /**
     * 创建充电枪响应
     * 
     * @param connectorId 连接器编号
     * @param connectorType 连接器类型代码
     * @param connectorTypeDescription 连接器类型描述
     * @param status 状态代码
     * @param statusDescription 状态描述
     * @param maxPower 最大功率
     * @param maxPowerFormatted 格式化的最大功率
     * @param isAvailableForCharging 是否可用于充电
     * @param isCharging 是否正在充电
     * @param isOffline 是否离线
     * @param isFastCharging 是否为快充
     * @param isSuperCharging 是否为超充
     * @param displayName 显示名称
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @return ConnectorResponse实例
     */
    public static ConnectorResponse of(Integer connectorId, String connectorType, String connectorTypeDescription,
                                     String status, String statusDescription,
                                     BigDecimal maxPower, String maxPowerFormatted,
                                     boolean isAvailableForCharging, boolean isCharging, boolean isOffline,
                                     boolean isFastCharging, boolean isSuperCharging, String displayName,
                                     Instant createdAt, Instant updatedAt) {
        return new ConnectorResponse(connectorId, connectorType, connectorTypeDescription,
                                   status, statusDescription, maxPower, maxPowerFormatted,
                                   isAvailableForCharging, isCharging, isOffline,
                                   isFastCharging, isSuperCharging, displayName,
                                   createdAt, updatedAt);
    }

    /**
     * 创建简化的充电枪响应
     * 
     * @param connectorId 连接器编号
     * @param connectorType 连接器类型代码
     * @param connectorTypeDescription 连接器类型描述
     * @param status 状态代码
     * @param statusDescription 状态描述
     * @param isAvailableForCharging 是否可用于充电
     * @param displayName 显示名称
     * @return ConnectorResponse实例
     */
    public static ConnectorResponse simple(Integer connectorId, String connectorType, String connectorTypeDescription,
                                         String status, String statusDescription,
                                         boolean isAvailableForCharging, String displayName) {
        return new ConnectorResponse(connectorId, connectorType, connectorTypeDescription,
                                   status, statusDescription, null, null,
                                   isAvailableForCharging, false, false,
                                   false, false, displayName, null, null);
    }
}
