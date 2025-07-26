package com.charge.station.application.command;

import com.charge.station.domain.model.station.StationId;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 更新充电站命令
 * 
 * 封装更新充电站所需的所有参数。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record UpdateStationCommand(
    StationId stationId,
    String name,
    String address,
    String description,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalTime openTime,
    LocalTime closeTime
) {
    
    /**
     * 创建更新充电站命令
     * 
     * @param stationId 充电站ID
     * @param name 充电站名称
     * @param address 地址
     * @param description 描述
     * @param latitude 纬度
     * @param longitude 经度
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @return UpdateStationCommand实例
     */
    public static UpdateStationCommand of(StationId stationId, String name, String address, String description,
                                        BigDecimal latitude, BigDecimal longitude,
                                        LocalTime openTime, LocalTime closeTime) {
        return new UpdateStationCommand(stationId, name, address, description, latitude, longitude, 
                                      openTime, closeTime);
    }

    /**
     * 创建更新充电站命令（不带描述）
     * 
     * @param stationId 充电站ID
     * @param name 充电站名称
     * @param address 地址
     * @param latitude 纬度
     * @param longitude 经度
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @return UpdateStationCommand实例
     */
    public static UpdateStationCommand of(StationId stationId, String name, String address,
                                        BigDecimal latitude, BigDecimal longitude,
                                        LocalTime openTime, LocalTime closeTime) {
        return new UpdateStationCommand(stationId, name, address, null, latitude, longitude, 
                                      openTime, closeTime);
    }
}
