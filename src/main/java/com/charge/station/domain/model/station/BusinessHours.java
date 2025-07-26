package com.charge.station.domain.model.station;

import java.time.LocalTime;
import java.util.Objects;

/**
 * 营业时间值对象
 * 
 * 封装充电站的营业时间信息，提供时间相关的业务方法。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record BusinessHours(
    LocalTime openTime,
    LocalTime closeTime
) {
    
    /**
     * 24小时营业的营业时间
     */
    public static final BusinessHours TWENTY_FOUR_HOURS = new BusinessHours(
        LocalTime.of(0, 0), 
        LocalTime.of(23, 59)
    );

    public BusinessHours {
        Objects.requireNonNull(openTime, "开始营业时间不能为空");
        Objects.requireNonNull(closeTime, "结束营业时间不能为空");
        
        // 验证时间逻辑：开始时间不能等于或晚于结束时间
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("开始营业时间必须早于结束营业时间");
        }
    }

    /**
     * 创建营业时间
     * 
     * @param openHour 开始小时
     * @param openMinute 开始分钟
     * @param closeHour 结束小时
     * @param closeMinute 结束分钟
     * @return BusinessHours实例
     */
    public static BusinessHours of(int openHour, int openMinute, int closeHour, int closeMinute) {
        return new BusinessHours(
            LocalTime.of(openHour, openMinute),
            LocalTime.of(closeHour, closeMinute)
        );
    }

    /**
     * 创建营业时间（分钟默认为0）
     * 
     * @param openHour 开始小时
     * @param closeHour 结束小时
     * @return BusinessHours实例
     */
    public static BusinessHours of(int openHour, int closeHour) {
        return of(openHour, 0, closeHour, 0);
    }

    /**
     * 创建24小时营业时间
     * 
     * @return 24小时营业的BusinessHours实例
     */
    public static BusinessHours twentyFourHours() {
        return TWENTY_FOUR_HOURS;
    }

    /**
     * 检查指定时间是否在营业时间内
     * 
     * @param time 要检查的时间
     * @return true如果在营业时间内
     */
    public boolean isOpen(LocalTime time) {
        Objects.requireNonNull(time, "检查时间不能为空");
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    /**
     * 检查当前时间是否在营业时间内
     * 
     * @return true如果当前时间在营业时间内
     */
    public boolean isOpenNow() {
        return isOpen(LocalTime.now());
    }

    /**
     * 检查是否为24小时营业
     * 
     * @return true如果是24小时营业
     */
    public boolean isTwentyFourHours() {
        return openTime.equals(LocalTime.of(0, 0)) && 
               closeTime.equals(LocalTime.of(23, 59));
    }

    /**
     * 计算营业时长（分钟）
     * 
     * @return 营业时长（分钟）
     */
    public long getBusinessDurationMinutes() {
        return java.time.Duration.between(openTime, closeTime).toMinutes();
    }

    /**
     * 获取格式化的营业时间字符串
     * 
     * @return 格式化的营业时间字符串，如 "08:00-22:00"
     */
    public String getFormattedHours() {
        return openTime.toString() + "-" + closeTime.toString();
    }

    /**
     * 更新开始营业时间
     * 
     * @param newOpenTime 新的开始时间
     * @return 新的BusinessHours实例
     */
    public BusinessHours withOpenTime(LocalTime newOpenTime) {
        return new BusinessHours(newOpenTime, closeTime);
    }

    /**
     * 更新结束营业时间
     * 
     * @param newCloseTime 新的结束时间
     * @return 新的BusinessHours实例
     */
    public BusinessHours withCloseTime(LocalTime newCloseTime) {
        return new BusinessHours(openTime, newCloseTime);
    }
}
