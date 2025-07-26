package com.charge.station.domain.model.chargepoint;

import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * 充电枪实体
 * 
 * 表示充电桩上的具体充电接口，包含连接器类型、状态和功率规格。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA需要
public class Connector {
    
    private Long id; // 数据库主键
    private Integer connectorId; // 充电桩内的连接器编号
    private ConnectorType connectorType;
    private DeviceStatus status;
    private PowerSpecification powerSpecification;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的充电枪
     * 
     * @param connectorId 连接器编号（在充电桩内唯一）
     * @param connectorType 连接器类型
     * @param powerSpecification 功率规格
     */
    public Connector(Integer connectorId, ConnectorType connectorType, PowerSpecification powerSpecification) {
        this.connectorId = validateConnectorId(connectorId);
        this.connectorType = Objects.requireNonNull(connectorType, "连接器类型不能为空");
        this.powerSpecification = Objects.requireNonNull(powerSpecification, "功率规格不能为空");
        this.status = DeviceStatus.UNAVAILABLE; // 新建连接器默认为不可用状态
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 验证连接器编号
     */
    private Integer validateConnectorId(Integer connectorId) {
        Objects.requireNonNull(connectorId, "连接器编号不能为空");
        if (connectorId <= 0) {
            throw new IllegalArgumentException("连接器编号必须大于0");
        }
        if (connectorId > 10) {
            throw new IllegalArgumentException("连接器编号不能超过10");
        }
        return connectorId;
    }

    /**
     * 更新连接器状态
     * 
     * @param newStatus 新状态
     * @param timestamp 状态变更时间
     */
    public void updateStatus(DeviceStatus newStatus, Instant timestamp) {
        Objects.requireNonNull(newStatus, "设备状态不能为空");
        Objects.requireNonNull(timestamp, "状态变更时间不能为空");
        
        this.status = newStatus;
        this.updatedAt = timestamp;
    }

    /**
     * 更新功率规格
     * 
     * @param newPowerSpecification 新的功率规格
     */
    public void updatePowerSpecification(PowerSpecification newPowerSpecification) {
        Objects.requireNonNull(newPowerSpecification, "功率规格不能为空");
        this.powerSpecification = newPowerSpecification;
        this.updatedAt = Instant.now();
    }

    /**
     * 检查是否可用于充电
     * 
     * @return true如果可用于充电
     */
    public boolean isAvailableForCharging() {
        return status.isAvailableForCharging();
    }

    /**
     * 检查是否正在充电
     * 
     * @return true如果正在充电
     */
    public boolean isCharging() {
        return status.isCharging();
    }

    /**
     * 检查是否离线
     * 
     * @return true如果离线
     */
    public boolean isOffline() {
        return status.isOffline();
    }

    /**
     * 检查是否为快充连接器
     * 
     * @return true如果是快充连接器
     */
    public boolean isFastCharging() {
        return connectorType.isDcFastCharging() && powerSpecification.isFastCharging();
    }

    /**
     * 检查是否为超充连接器
     * 
     * @return true如果是超充连接器
     */
    public boolean isSuperCharging() {
        return connectorType.isDcFastCharging() && powerSpecification.isSuperCharging();
    }

    /**
     * 检查是否兼容指定的连接器类型
     * 
     * @param targetType 目标连接器类型
     * @return true如果兼容
     */
    public boolean isCompatibleWith(ConnectorType targetType) {
        return connectorType.isCompatibleWith(targetType);
    }

    /**
     * 获取连接器的显示名称
     * 
     * @return 显示名称，如 "1号枪(国标直流快充-120.00kW)"
     */
    public String getDisplayName() {
        return String.format("%d号枪(%s-%s)", 
            connectorId, 
            connectorType.getDescription(), 
            powerSpecification.getFormattedPower());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connector connector = (Connector) o;
        return Objects.equals(connectorId, connector.connectorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectorId);
    }

    @Override
    public String toString() {
        return "Connector{" +
                "connectorId=" + connectorId +
                ", connectorType=" + connectorType +
                ", status=" + status +
                ", powerSpecification=" + powerSpecification +
                '}';
    }
}
