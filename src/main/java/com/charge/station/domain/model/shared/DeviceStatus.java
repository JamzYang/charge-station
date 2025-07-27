package com.charge.station.domain.model.shared;

/**
 * 设备状态枚举
 * 
 * 封装设备状态，提供与外部系统（如OCPP协议）状态字符串之间的安全转换，
 * 避免在业务逻辑中使用"魔法字符串"。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public enum DeviceStatus {
    AVAILABLE("Available"),
    PREPARING("Preparing"),
    CHARGING("Charging"),
    SUSPENDED_EVSE("SuspendedEVSE"),
    SUSPENDED_EV("SuspendedEV"),
    FINISHING("Finishing"),
    RESERVED("Reserved"),
    UNAVAILABLE("Unavailable"),
    FAULTED("Faulted"),
    OFFLINE("Offline"); // 内部状态，非OCPP标准

    private final String ocppStatus;

    DeviceStatus(String ocppStatus) {
        this.ocppStatus = ocppStatus;
    }

    public String getOcppStatus() {
        return ocppStatus;
    }

    /**
     * 从OCPP状态字符串安全地转换为DeviceStatus枚举。
     * 
     * @param statusString 来自网关事件的状态字符串
     * @return 对应的DeviceStatus枚举
     */
    public static DeviceStatus fromOcppStatus(String statusString) {
        if (statusString == null) {
            return UNAVAILABLE; // 或根据业务决定抛出异常
        }
        for (DeviceStatus status : values()) {
            if (status.ocppStatus.equalsIgnoreCase(statusString)) {
                return status;
            }
        }
        // 对于未知的状态字符串，记录警告并返回一个默认的安全状态
        return UNAVAILABLE;
    }

    /**
     * 检查设备是否可用于充电
     * 
     * @return true如果设备可用于充电
     */
    public boolean isAvailableForCharging() {
        return this == AVAILABLE || this == PREPARING;
    }

    /**
     * 检查设备是否正在充电
     * 
     * @return true如果设备正在充电
     */
    public boolean isCharging() {
        return this == CHARGING || this == PREPARING || this == FINISHING;
    }

    /**
     * 检查设备是否离线
     * 
     * @return true如果设备离线
     */
    public boolean isOffline() {
        return this == OFFLINE || this == UNAVAILABLE;
    }
}
