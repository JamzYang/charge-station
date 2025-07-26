package com.charge.station.domain.model.shared;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 功率规格值对象
 * 
 * 封装设备的功率相关信息，确保功率值的有效性和一致性。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record PowerSpecification(
    BigDecimal maxPower,
    String unit
) {
    
    /**
     * 默认单位为千瓦(kW)
     */
    public static final String DEFAULT_UNIT = "kW";
    
    /**
     * 最小功率值
     */
    public static final BigDecimal MIN_POWER = BigDecimal.ZERO;
    
    /**
     * 最大功率值 (500kW)
     */
    public static final BigDecimal MAX_POWER = new BigDecimal("500.00");

    public PowerSpecification {
        Objects.requireNonNull(maxPower, "最大功率不能为空");
        Objects.requireNonNull(unit, "功率单位不能为空");
        
        if (maxPower.compareTo(MIN_POWER) <= 0) {
            throw new IllegalArgumentException("最大功率必须大于0");
        }
        
        if (maxPower.compareTo(MAX_POWER) > 0) {
            throw new IllegalArgumentException("最大功率不能超过" + MAX_POWER + unit);
        }
        
        if (unit.trim().isEmpty()) {
            throw new IllegalArgumentException("功率单位不能为空字符串");
        }
    }

    /**
     * 创建默认单位(kW)的功率规格
     * 
     * @param maxPower 最大功率值
     * @return PowerSpecification实例
     */
    public static PowerSpecification ofKilowatts(BigDecimal maxPower) {
        return new PowerSpecification(maxPower, DEFAULT_UNIT);
    }

    /**
     * 创建默认单位(kW)的功率规格
     * 
     * @param maxPower 最大功率值
     * @return PowerSpecification实例
     */
    public static PowerSpecification ofKilowatts(double maxPower) {
        return new PowerSpecification(BigDecimal.valueOf(maxPower), DEFAULT_UNIT);
    }

    /**
     * 检查是否为快充功率 (>= 50kW)
     * 
     * @return true如果是快充功率
     */
    public boolean isFastCharging() {
        return DEFAULT_UNIT.equals(unit) && maxPower.compareTo(new BigDecimal("50.00")) >= 0;
    }

    /**
     * 检查是否为超充功率 (>= 150kW)
     * 
     * @return true如果是超充功率
     */
    public boolean isSuperCharging() {
        return DEFAULT_UNIT.equals(unit) && maxPower.compareTo(new BigDecimal("150.00")) >= 0;
    }

    /**
     * 获取格式化的功率字符串
     * 
     * @return 格式化的功率字符串，如 "7.00kW"
     */
    public String getFormattedPower() {
        return maxPower.toString() + unit;
    }
}
