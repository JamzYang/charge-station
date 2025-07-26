package com.charge.station.interfaces.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 创建充电桩请求DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record CreateChargePointRequest(
    @NotBlank(message = "充电桩名称不能为空")
    @Size(max = 100, message = "充电桩名称长度不能超过100字符")
    String name,
    
    @Size(max = 50, message = "型号长度不能超过50字符")
    String model,
    
    @Size(max = 50, message = "厂商长度不能超过50字符")
    String vendor,
    
    @Size(max = 100, message = "序列号长度不能超过100字符")
    String serialNumber,
    
    @NotNull(message = "最大功率不能为空")
    @DecimalMin(value = "0.1", message = "最大功率必须大于0")
    @DecimalMax(value = "500.0", message = "最大功率不能超过500kW")
    @Digits(integer = 3, fraction = 2, message = "最大功率格式不正确")
    BigDecimal maxPower
) {
    
    /**
     * 创建创建充电桩请求
     * 
     * @param name 充电桩名称
     * @param model 型号
     * @param vendor 厂商
     * @param serialNumber 序列号
     * @param maxPower 最大功率
     * @return CreateChargePointRequest实例
     */
    public static CreateChargePointRequest of(String name, String model, String vendor, 
                                            String serialNumber, BigDecimal maxPower) {
        return new CreateChargePointRequest(name, model, vendor, serialNumber, maxPower);
    }

    /**
     * 创建创建充电桩请求（简化版本）
     * 
     * @param name 充电桩名称
     * @param serialNumber 序列号
     * @param maxPower 最大功率
     * @return CreateChargePointRequest实例
     */
    public static CreateChargePointRequest of(String name, String serialNumber, BigDecimal maxPower) {
        return new CreateChargePointRequest(name, null, null, serialNumber, maxPower);
    }
}
