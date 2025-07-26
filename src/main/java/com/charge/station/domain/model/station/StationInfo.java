package com.charge.station.domain.model.station;

import java.util.Objects;

/**
 * 充电站信息值对象
 * 
 * 封装充电站的基本信息，包括名称、地址和描述。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StationInfo(
    String name,
    String address,
    String description
) {
    
    /**
     * 名称最大长度
     */
    public static final int MAX_NAME_LENGTH = 100;
    
    /**
     * 地址最大长度
     */
    public static final int MAX_ADDRESS_LENGTH = 200;
    
    /**
     * 描述最大长度
     */
    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    public StationInfo {
        Objects.requireNonNull(name, "充电站名称不能为空");
        Objects.requireNonNull(address, "充电站地址不能为空");
        
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("充电站名称不能为空字符串");
        }
        
        if (address.trim().isEmpty()) {
            throw new IllegalArgumentException("充电站地址不能为空字符串");
        }
        
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("充电站名称长度不能超过" + MAX_NAME_LENGTH + "个字符");
        }
        
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("充电站地址长度不能超过" + MAX_ADDRESS_LENGTH + "个字符");
        }
        
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("充电站描述长度不能超过" + MAX_DESCRIPTION_LENGTH + "个字符");
        }
        
        // 标准化处理
        name = name.trim();
        address = address.trim();
        description = description != null ? description.trim() : null;
    }

    /**
     * 创建带描述的充电站信息
     * 
     * @param name 名称
     * @param address 地址
     * @param description 描述
     * @return StationInfo实例
     */
    public static StationInfo of(String name, String address, String description) {
        return new StationInfo(name, address, description);
    }

    /**
     * 创建不带描述的充电站信息
     * 
     * @param name 名称
     * @param address 地址
     * @return StationInfo实例
     */
    public static StationInfo of(String name, String address) {
        return new StationInfo(name, address, null);
    }

    /**
     * 检查是否有描述信息
     * 
     * @return true如果有描述信息
     */
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    /**
     * 获取显示名称（如果没有描述则返回名称，否则返回"名称 - 描述"）
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        if (hasDescription()) {
            return name + " - " + description;
        }
        return name;
    }

    /**
     * 更新名称
     * 
     * @param newName 新名称
     * @return 新的StationInfo实例
     */
    public StationInfo withName(String newName) {
        return new StationInfo(newName, address, description);
    }

    /**
     * 更新地址
     * 
     * @param newAddress 新地址
     * @return 新的StationInfo实例
     */
    public StationInfo withAddress(String newAddress) {
        return new StationInfo(name, newAddress, description);
    }

    /**
     * 更新描述
     * 
     * @param newDescription 新描述
     * @return 新的StationInfo实例
     */
    public StationInfo withDescription(String newDescription) {
        return new StationInfo(name, address, newDescription);
    }
}
