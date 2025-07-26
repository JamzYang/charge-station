package com.charge.station.domain.model.chargepoint;

import java.util.Objects;
import java.util.UUID;

/**
 * 充电桩ID值对象
 * 
 * 封装充电桩的唯一标识符，确保ID的有效性和一致性。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record ChargePointId(String value) {
    
    /**
     * ID前缀
     */
    public static final String PREFIX = "CP";
    
    /**
     * ID最大长度
     */
    public static final int MAX_LENGTH = 32;

    public ChargePointId {
        Objects.requireNonNull(value, "充电桩ID不能为空");
        
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("充电桩ID不能为空字符串");
        }
        
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("充电桩ID长度不能超过" + MAX_LENGTH + "个字符");
        }
        
        // 验证ID格式：必须以CP开头
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("充电桩ID必须以" + PREFIX + "开头");
        }
    }

    /**
     * 生成新的充电桩ID
     * 
     * @return 新的ChargePointId实例
     */
    public static ChargePointId generate() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        return new ChargePointId(PREFIX + uuid);
    }

    /**
     * 从字符串创建ChargePointId
     * 
     * @param value ID字符串
     * @return ChargePointId实例
     */
    public static ChargePointId of(String value) {
        return new ChargePointId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
