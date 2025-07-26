package com.charge.station.domain.model.station;

/**
 * 充电站状态枚举
 * 
 * 定义充电站的运营状态。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public enum StationStatus {
    /**
     * 活跃状态 - 正常运营
     */
    ACTIVE("ACTIVE", "正常运营"),
    
    /**
     * 非活跃状态 - 暂停运营
     */
    INACTIVE("INACTIVE", "暂停运营"),
    
    /**
     * 维护状态 - 维护中
     */
    MAINTENANCE("MAINTENANCE", "维护中");

    private final String code;
    private final String description;

    StationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 从代码字符串转换为状态枚举
     * 
     * @param code 状态代码
     * @return 对应的状态枚举
     */
    public static StationStatus fromCode(String code) {
        if (code == null) {
            return INACTIVE;
        }
        
        for (StationStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        
        return INACTIVE; // 默认状态
    }

    /**
     * 检查是否为活跃状态
     * 
     * @return true如果是活跃状态
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 检查是否可以提供服务
     * 
     * @return true如果可以提供服务
     */
    public boolean canProvideService() {
        return this == ACTIVE;
    }

    /**
     * 检查是否在维护中
     * 
     * @return true如果在维护中
     */
    public boolean isUnderMaintenance() {
        return this == MAINTENANCE;
    }

    @Override
    public String toString() {
        return code;
    }
}
