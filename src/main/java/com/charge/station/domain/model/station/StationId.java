package com.charge.station.domain.model.station;

import java.util.Objects;
import java.util.UUID;

/**
 * 充电站ID值对象
 * 
 * 封装充电站的唯一标识符，确保ID的有效性和一致性。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StationId(String value) {
    
    /**
     * ID前缀
     */
    public static final String PREFIX = "ST";
    
    /**
     * ID最大长度
     */
    public static final int MAX_LENGTH = 32;

    public StationId {
        Objects.requireNonNull(value, "充电站ID不能为空");
        
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("充电站ID不能为空字符串");
        }
        
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("充电站ID长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 验证ID格式：必须以ST开头
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("充电站ID必须以" + PREFIX + "开头");
        }
    }

    /**
     * 生成新的充电站ID
     * 
     * @return 新的StationId实例
     */
    public static StationId generate() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        return new StationId(PREFIX + uuid);
    }

    /**
     * 从字符串创建StationId
     * 
     * @param value ID字符串
     * @return StationId实例
     */
    public static StationId of(String value) {
        return new StationId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
