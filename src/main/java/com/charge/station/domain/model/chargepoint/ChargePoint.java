package com.charge.station.domain.model.chargepoint;

import com.charge.station.domain.event.ChargePointCreatedEvent;
import com.charge.station.domain.event.ChargePointStatusChangedEvent;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import com.charge.station.domain.model.station.StationId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * 充电桩聚合根
 * 
 * 管理充电桩的生命周期和业务规则，包含多个充电枪。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA需要
public class ChargePoint {
    
    private ChargePointId chargePointId;
    private StationId stationId;
    private String name;
    private String model;
    private String vendor;
    private String serialNumber;
    private String firmwareVersion;
    private DeviceStatus status;
    private Instant lastHeartbeat;
    private PowerSpecification powerSpecification;
    private List<Connector> connectors;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    
    // 领域事件列表
    private final List<Object> domainEvents = new ArrayList<>();

    /**
     * 创建新的充电桩
     * 
     * @param stationId 所属充电站ID
     * @param name 充电桩名称
     * @param model 型号
     * @param vendor 厂商
     * @param serialNumber 序列号
     * @param powerSpecification 功率规格
     */
    public ChargePoint(StationId stationId, String name, String model, String vendor, 
                      String serialNumber, PowerSpecification powerSpecification) {
        this.chargePointId = ChargePointId.generate();
        this.stationId = Objects.requireNonNull(stationId, "充电站ID不能为空");
        this.name = validateName(name);
        this.model = validateModel(model);
        this.vendor = validateVendor(vendor);
        this.serialNumber = validateSerialNumber(serialNumber);
        this.powerSpecification = Objects.requireNonNull(powerSpecification, "功率规格不能为空");
        this.status = DeviceStatus.UNAVAILABLE; // 新建充电桩默认为不可用状态
        this.connectors = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
        
        // 发布充电桩创建事件
        addDomainEvent(new ChargePointCreatedEvent(
            this.chargePointId,
            this.stationId,
            this.name,
            this.serialNumber,
            this.createdAt
        ));
    }

    /**
     * 验证充电桩名称
     */
    private String validateName(String name) {
        Objects.requireNonNull(name, "充电桩名称不能为空");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("充电桩名称不能为空字符串");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("充电桩名称长度不能超过100个字符");
        }
        return name.trim();
    }

    /**
     * 验证型号
     */
    private String validateModel(String model) {
        if (model != null && model.length() > 50) {
            throw new IllegalArgumentException("充电桩型号长度不能超过50个字符");
        }
        return model != null ? model.trim() : null;
    }

    /**
     * 验证厂商
     */
    private String validateVendor(String vendor) {
        if (vendor != null && vendor.length() > 50) {
            throw new IllegalArgumentException("充电桩厂商长度不能超过50个字符");
        }
        return vendor != null ? vendor.trim() : null;
    }

    /**
     * 验证序列号
     */
    private String validateSerialNumber(String serialNumber) {
        if (serialNumber != null && serialNumber.length() > 100) {
            throw new IllegalArgumentException("充电桩序列号长度不能超过100个字符");
        }
        return serialNumber != null ? serialNumber.trim() : null;
    }

    /**
     * 添加充电枪
     * 
     * @param connectorType 连接器类型
     * @param maxPower 最大功率
     */
    public void addConnector(ConnectorType connectorType, BigDecimal maxPower) {
        Objects.requireNonNull(connectorType, "连接器类型不能为空");
        Objects.requireNonNull(maxPower, "最大功率不能为空");
        
        int nextConnectorId = connectors.size() + 1;
        if (nextConnectorId > 10) {
            throw new IllegalArgumentException("充电桩最多支持10个充电枪");
        }
        
        // 检查连接器编号是否已存在
        boolean exists = connectors.stream()
            .anyMatch(c -> c.getConnectorId().equals(nextConnectorId));
        if (exists) {
            throw new IllegalArgumentException("连接器编号" + nextConnectorId + "已存在");
        }
        
        PowerSpecification connectorPower = PowerSpecification.ofKilowatts(maxPower);
        Connector connector = new Connector(nextConnectorId, connectorType, connectorPower);
        connectors.add(connector);
        
        this.updatedAt = Instant.now();
    }

    /**
     * 更新充电桩状态
     * 
     * @param newStatus 新状态
     * @param timestamp 状态变更时间
     */
    public void updateStatus(DeviceStatus newStatus, Instant timestamp) {
        Objects.requireNonNull(newStatus, "设备状态不能为空");
        Objects.requireNonNull(timestamp, "状态变更时间不能为空");
        
        if (!this.status.equals(newStatus)) {
            DeviceStatus oldStatus = this.status;
            this.status = newStatus;
            this.updatedAt = timestamp;
            
            // 发布状态变更事件
            addDomainEvent(new ChargePointStatusChangedEvent(
                this.chargePointId,
                oldStatus,
                newStatus,
                timestamp
            ));
        }
    }

    /**
     * 更新心跳时间
     * 
     * @param heartbeatTime 心跳时间
     */
    public void updateHeartbeat(Instant heartbeatTime) {
        Objects.requireNonNull(heartbeatTime, "心跳时间不能为空");
        this.lastHeartbeat = heartbeatTime;
        this.updatedAt = heartbeatTime;
    }

    /**
     * 更新固件版本
     * 
     * @param newFirmwareVersion 新固件版本
     */
    public void updateFirmwareVersion(String newFirmwareVersion) {
        if (newFirmwareVersion != null && newFirmwareVersion.length() > 50) {
            throw new IllegalArgumentException("固件版本长度不能超过50个字符");
        }
        this.firmwareVersion = newFirmwareVersion != null ? newFirmwareVersion.trim() : null;
        this.updatedAt = Instant.now();
    }

    /**
     * 根据连接器编号获取连接器
     * 
     * @param connectorId 连接器编号
     * @return 连接器，如果不存在则返回null
     */
    public Connector getConnector(Integer connectorId) {
        return connectors.stream()
            .filter(c -> c.getConnectorId().equals(connectorId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取可用的连接器数量
     * 
     * @return 可用连接器数量
     */
    public int getAvailableConnectorCount() {
        return (int) connectors.stream()
            .filter(Connector::isAvailableForCharging)
            .count();
    }

    /**
     * 检查是否有可用的连接器
     * 
     * @return true如果有可用连接器
     */
    public boolean hasAvailableConnector() {
        return getAvailableConnectorCount() > 0;
    }

    /**
     * 检查是否可用于充电
     * 
     * @return true如果可用于充电
     */
    public boolean isAvailableForCharging() {
        return status.isAvailableForCharging() && hasAvailableConnector();
    }

    /**
     * 检查是否在线
     * 
     * @return true如果在线
     */
    public boolean isOnline() {
        if (lastHeartbeat == null) {
            return false;
        }
        // 如果超过5分钟没有心跳，认为离线
        return Instant.now().minusSeconds(300).isBefore(lastHeartbeat);
    }

    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }

    /**
     * 获取并清空领域事件
     * 
     * @return 领域事件列表
     */
    public List<Object> getDomainEventsAndClear() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    /**
     * 检查是否有未处理的领域事件
     * 
     * @return true如果有未处理的领域事件
     */
    public boolean hasDomainEvents() {
        return !this.domainEvents.isEmpty();
    }
}
