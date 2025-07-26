package com.charge.station.shared.constant;

/**
 * 缓存键常量
 * 
 * @author 架构师团队
 * @version 1.0
 */
public final class CacheKeys {
    
    /**
     * 充电站详情缓存键前缀
     */
    public static final String STATION_DETAIL_PREFIX = "station:";
    
    /**
     * 充电桩状态缓存键前缀
     */
    public static final String CHARGEPOINT_STATUS_PREFIX = "chargepoint:status:";
    
    /**
     * 设备连接映射缓存键前缀 (与网关集成手册保持一致)
     */
    public static final String DEVICE_CONNECTION_PREFIX = "conn:";
    
    private CacheKeys() {
        // 工具类，禁止实例化
    }
}
