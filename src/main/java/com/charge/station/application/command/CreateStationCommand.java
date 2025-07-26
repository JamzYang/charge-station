package com.charge.station.application.command;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 创建充电站命令
 * 
 * 封装创建充电站所需的所有参数。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record CreateStationCommand(
    String name,
    String address,
    String description,
    BigDecimal latitude,
    BigDecimal longitude,
    String operatorId,
    LocalTime openTime,
    LocalTime closeTime
) {
    
    /**
     * 创建创建充电站命令
     * 
     * @param name 充电站名称
     * @param address 地址
     * @param description 描述
     * @param latitude 纬度
     * @param longitude 经度
     * @param operatorId 运营商ID
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @return CreateStationCommand实例
     */
    public static CreateStationCommand of(String name, String address, String description,
                                        BigDecimal latitude, BigDecimal longitude, String operatorId,
                                        LocalTime openTime, LocalTime closeTime) {
        return new CreateStationCommand(name, address, description, latitude, longitude, 
                                      operatorId, openTime, closeTime);
    }

    /**
     * 创建创建充电站命令（不带描述）
     * 
     * @param name 充电站名称
     * @param address 地址
     * @param latitude 纬度
     * @param longitude 经度
     * @param operatorId 运营商ID
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @return CreateStationCommand实例
     */
    public static CreateStationCommand of(String name, String address,
                                        BigDecimal latitude, BigDecimal longitude, String operatorId,
                                        LocalTime openTime, LocalTime closeTime) {
        return new CreateStationCommand(name, address, null, latitude, longitude, 
                                      operatorId, openTime, closeTime);
    }
}
