package com.charge.station.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

/**
 * 充电站响应DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StationResponse(
    String stationId,
    String name,
    String address,
    String description,
    BigDecimal latitude,
    BigDecimal longitude,
    String operatorId,
    String status,
    String statusDescription,
    @JsonFormat(pattern = "HH:mm")
    LocalTime openTime,
    @JsonFormat(pattern = "HH:mm")
    LocalTime closeTime,
    String businessHoursFormatted,
    boolean canProvideService,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant updatedAt
) {
    
    /**
     * 创建充电站响应
     * 
     * @param stationId 充电站ID
     * @param name 名称
     * @param address 地址
     * @param description 描述
     * @param latitude 纬度
     * @param longitude 经度
     * @param operatorId 运营商ID
     * @param status 状态代码
     * @param statusDescription 状态描述
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @param businessHoursFormatted 格式化的营业时间
     * @param canProvideService 是否可以提供服务
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @return StationResponse实例
     */
    public static StationResponse of(String stationId, String name, String address, String description,
                                   BigDecimal latitude, BigDecimal longitude, String operatorId,
                                   String status, String statusDescription,
                                   LocalTime openTime, LocalTime closeTime, String businessHoursFormatted,
                                   boolean canProvideService, Instant createdAt, Instant updatedAt) {
        return new StationResponse(stationId, name, address, description, latitude, longitude,
                                 operatorId, status, statusDescription, openTime, closeTime,
                                 businessHoursFormatted, canProvideService, createdAt, updatedAt);
    }

    /**
     * 创建简化的充电站响应
     * 
     * @param stationId 充电站ID
     * @param name 名称
     * @param address 地址
     * @param latitude 纬度
     * @param longitude 经度
     * @param status 状态代码
     * @param statusDescription 状态描述
     * @param canProvideService 是否可以提供服务
     * @return StationResponse实例
     */
    public static StationResponse simple(String stationId, String name, String address,
                                       BigDecimal latitude, BigDecimal longitude,
                                       String status, String statusDescription,
                                       boolean canProvideService) {
        return new StationResponse(stationId, name, address, null, latitude, longitude,
                                 null, status, statusDescription, null, null, null,
                                 canProvideService, null, null);
    }
}
