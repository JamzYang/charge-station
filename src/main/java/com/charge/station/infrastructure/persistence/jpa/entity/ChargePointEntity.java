package com.charge.station.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 充电桩JPA实体
 *
 * 映射到charge_points表，提供充电桩数据的持久化支持。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Entity
@Table(name = "charge_points", indexes = {
    @Index(name = "idx_charge_points_station", columnList = "station_id"),
    @Index(name = "idx_charge_points_status", columnList = "status"),
    @Index(name = "idx_charge_points_serial", columnList = "serial_number")
})
public class ChargePointEntity {
    
    @Id
    @Column(name = "id", length = 32)
    private String id;
    
    @Column(name = "station_id", length = 32, nullable = false)
    private String stationId;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "model", length = 50)
    private String model;
    
    @Column(name = "vendor", length = 50)
    private String vendor;
    
    @Column(name = "serial_number", length = 100)
    private String serialNumber;
    
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;
    
    @Column(name = "power_output", precision = 10, scale = 2)
    private BigDecimal powerOutput;
    
    @Column(name = "connector_count", nullable = false)
    private Integer connectorCount;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    // 关联关系：一个充电桩有多个充电枪
    @OneToMany(mappedBy = "chargePointId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConnectorEntity> connectors = new ArrayList<>();
    
    /**
     * 构造函数
     * 
     * @param id 充电桩ID
     * @param stationId 充电站ID
     * @param name 充电桩名称
     * @param model 型号
     * @param vendor 厂商
     * @param serialNumber 序列号
     * @param firmwareVersion 固件版本
     * @param status 状态
     * @param powerOutput 功率输出
     * @param connectorCount 充电枪数量
     */
    public ChargePointEntity(String id, String stationId, String name, String model, 
                           String vendor, String serialNumber, String firmwareVersion, 
                           String status, BigDecimal powerOutput, Integer connectorCount) {
        this.id = id;
        this.stationId = stationId;
        this.name = name;
        this.model = model;
        this.vendor = vendor;
        this.serialNumber = serialNumber;
        this.firmwareVersion = firmwareVersion;
        this.status = status;
        this.powerOutput = powerOutput;
        this.connectorCount = connectorCount;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }
    
    /**
     * 默认构造函数（JPA需要）
     */
    protected ChargePointEntity() {
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * 添加充电枪
     *
     * @param connector 充电枪实体
     */
    public void addConnector(ConnectorEntity connector) {
        connectors.add(connector);
        connector.setChargePointId(this.id);
    }

    /**
     * 移除充电枪
     *
     * @param connector 充电枪实体
     */
    public void removeConnector(ConnectorEntity connector) {
        connectors.remove(connector);
        connector.setChargePointId(null);
    }

    // Getter methods
    public String getId() { return id; }
    public String getStationId() { return stationId; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getVendor() { return vendor; }
    public String getSerialNumber() { return serialNumber; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public String getStatus() { return status; }
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public BigDecimal getPowerOutput() { return powerOutput; }
    public Integer getConnectorCount() { return connectorCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public List<ConnectorEntity> getConnectors() { return connectors; }

    // Setter methods
    public void setId(String id) { this.id = id; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public void setStatus(String status) { this.status = status; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public void setPowerOutput(BigDecimal powerOutput) { this.powerOutput = powerOutput; }
    public void setConnectorCount(Integer connectorCount) { this.connectorCount = connectorCount; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setVersion(Long version) { this.version = version; }
    public void setConnectors(List<ConnectorEntity> connectors) { this.connectors = connectors; }
}
