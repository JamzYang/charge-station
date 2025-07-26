package com.charge.station.domain.model.chargepoint;

/**
 * 充电枪类型枚举
 * 
 * 定义充电枪的接口类型。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public enum ConnectorType {
    /**
     * 国标直流快充
     */
    GB_DC("GB_DC", "国标直流快充", true),
    
    /**
     * 国标交流慢充
     */
    GB_AC("GB_AC", "国标交流慢充", false),
    
    /**
     * 欧标Type2
     */
    TYPE2("TYPE2", "欧标Type2", false),
    
    /**
     * 美标CCS1
     */
    CCS1("CCS1", "美标CCS1", true),
    
    /**
     * 欧标CCS2
     */
    CCS2("CCS2", "欧标CCS2", true),
    
    /**
     * 日标CHAdeMO
     */
    CHADEMO("CHADEMO", "日标CHAdeMO", true),
    
    /**
     * 特斯拉专用
     */
    TESLA("TESLA", "特斯拉专用", true);

    private final String code;
    private final String description;
    private final boolean isDcFastCharging;

    ConnectorType(String code, String description, boolean isDcFastCharging) {
        this.code = code;
        this.description = description;
        this.isDcFastCharging = isDcFastCharging;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDcFastCharging() {
        return isDcFastCharging;
    }

    public boolean isAcCharging() {
        return !isDcFastCharging;
    }

    /**
     * 从代码字符串转换为连接器类型枚举
     * 
     * @param code 类型代码
     * @return 对应的连接器类型枚举
     */
    public static ConnectorType fromCode(String code) {
        if (code == null) {
            return GB_AC; // 默认类型
        }
        
        for (ConnectorType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        
        return GB_AC; // 默认类型
    }

    /**
     * 检查是否兼容指定的充电类型
     * 
     * @param other 另一个连接器类型
     * @return true如果兼容
     */
    public boolean isCompatibleWith(ConnectorType other) {
        if (other == null) {
            return false;
        }
        
        // 相同类型肯定兼容
        if (this == other) {
            return true;
        }
        
        // 国标之间可能兼容（简化处理）
        if ((this == GB_DC || this == GB_AC) && (other == GB_DC || other == GB_AC)) {
            return true;
        }
        
        // 欧标之间可能兼容
        if ((this == TYPE2 || this == CCS2) && (other == TYPE2 || other == CCS2)) {
            return true;
        }
        
        return false;
    }

    @Override
    public String toString() {
        return code;
    }
}
