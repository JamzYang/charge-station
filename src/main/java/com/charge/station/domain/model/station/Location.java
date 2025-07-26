package com.charge.station.domain.model.station;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 地理位置值对象
 * 
 * 封装地理坐标信息，提供位置相关的业务方法。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record Location(
    BigDecimal latitude,
    BigDecimal longitude
) {
    
    /**
     * 纬度最小值
     */
    public static final BigDecimal MIN_LATITUDE = new BigDecimal("-90");
    
    /**
     * 纬度最大值
     */
    public static final BigDecimal MAX_LATITUDE = new BigDecimal("90");
    
    /**
     * 经度最小值
     */
    public static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180");
    
    /**
     * 经度最大值
     */
    public static final BigDecimal MAX_LONGITUDE = new BigDecimal("180");
    
    /**
     * 坐标精度（小数点后6位）
     */
    public static final int COORDINATE_SCALE = 6;

    public Location {
        Objects.requireNonNull(latitude, "纬度不能为空");
        Objects.requireNonNull(longitude, "经度不能为空");
        
        // 验证纬度范围
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("纬度必须在-90到90之间");
        }
        
        // 验证经度范围
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("经度必须在-180到180之间");
        }
        
        // 标准化精度
        latitude = latitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
        longitude = longitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 从double值创建Location
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @return Location实例
     */
    public static Location of(double latitude, double longitude) {
        return new Location(
            BigDecimal.valueOf(latitude),
            BigDecimal.valueOf(longitude)
        );
    }

    /**
     * 计算到另一个位置的距离（米）
     * 使用Haversine公式
     * 
     * @param other 另一个位置
     * @return 距离（米）
     */
    public double distanceTo(Location other) {
        Objects.requireNonNull(other, "目标位置不能为空");
        
        double lat1Rad = Math.toRadians(this.latitude.doubleValue());
        double lat2Rad = Math.toRadians(other.latitude.doubleValue());
        double deltaLatRad = Math.toRadians(other.latitude.subtract(this.latitude).doubleValue());
        double deltaLonRad = Math.toRadians(other.longitude.subtract(this.longitude).doubleValue());

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 地球半径（米）
        double earthRadius = 6371000;
        return earthRadius * c;
    }

    /**
     * 检查是否在指定半径范围内
     * 
     * @param other 目标位置
     * @param radiusMeters 半径（米）
     * @return true如果在范围内
     */
    public boolean isWithinRadius(Location other, double radiusMeters) {
        return distanceTo(other) <= radiusMeters;
    }

    /**
     * 获取格式化的坐标字符串
     * 
     * @return 格式化的坐标字符串，如 "39.904200,116.407400"
     */
    public String getFormattedCoordinates() {
        return latitude.toString() + "," + longitude.toString();
    }

    /**
     * 获取纬度的double值
     * 
     * @return 纬度double值
     */
    public double getLatitudeAsDouble() {
        return latitude.doubleValue();
    }

    /**
     * 获取经度的double值
     * 
     * @return 经度double值
     */
    public double getLongitudeAsDouble() {
        return longitude.doubleValue();
    }
}
