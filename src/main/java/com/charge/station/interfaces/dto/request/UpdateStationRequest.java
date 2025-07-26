package com.charge.station.interfaces.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 更新充电站请求DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record UpdateStationRequest(
    @NotBlank(message = "充电站名称不能为空")
    @Size(max = 100, message = "充电站名称长度不能超过100字符")
    String name,
    
    @NotBlank(message = "地址不能为空")
    @Size(max = 200, message = "地址长度不能超过200字符")
    String address,
    
    @Size(max = 1000, message = "描述长度不能超过1000字符")
    String description,
    
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90", message = "纬度必须在-90到90之间")
    @Digits(integer = 2, fraction = 6, message = "纬度格式不正确")
    BigDecimal latitude,
    
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180", message = "经度必须在-180到180之间")
    @Digits(integer = 3, fraction = 6, message = "经度格式不正确")
    BigDecimal longitude,
    
    @NotNull(message = "营业开始时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    LocalTime openTime,
    
    @NotNull(message = "营业结束时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    LocalTime closeTime
) {
    
    /**
     * 创建更新充电站请求
     * 
     * @param name 充电站名称
     * @param address 地址
     * @param description 描述
     * @param latitude 纬度
     * @param longitude 经度
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @return UpdateStationRequest实例
     */
    public static UpdateStationRequest of(String name, String address, String description,
                                        BigDecimal latitude, BigDecimal longitude,
                                        LocalTime openTime, LocalTime closeTime) {
        return new UpdateStationRequest(name, address, description, latitude, longitude, 
                                      openTime, closeTime);
    }

    /**
     * 创建更新充电站请求（不带描述）
     * 
     * @param name 充电站名称
     * @param address 地址
     * @param latitude 纬度
     * @param longitude 经度
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     * @return UpdateStationRequest实例
     */
    public static UpdateStationRequest of(String name, String address,
                                        BigDecimal latitude, BigDecimal longitude,
                                        LocalTime openTime, LocalTime closeTime) {
        return new UpdateStationRequest(name, address, null, latitude, longitude, 
                                      openTime, closeTime);
    }
}
