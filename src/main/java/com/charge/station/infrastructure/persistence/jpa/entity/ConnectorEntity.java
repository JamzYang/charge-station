package com.charge.station.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 充电枪JPA实体
 *
 * 映射到connectors表，提供充电枪数据的持久化支持。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Entity
@Table(name = "connectors",
       indexes = {
           @Index(name = "idx_connectors_charge_point", columnList = "charge_point_id"),
           @Index(name = "idx_connectors_status", columnList = "status")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_connectors_charge_point_connector",
                           columnNames = {"charge_point_id", "connector_id"})
       })
public class ConnectorEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "charge_point_id", length = 32, nullable = false)
    private String chargePointId;
    
    @Column(name = "connector_id", nullable = false)
    private Integer connectorId;
    
    @Column(name = "connector_type", length = 20, nullable = false)
    private String connectorType;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "max_power", precision = 10, scale = 2)
    private BigDecimal maxPower;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    /**
     * 构造函数
     * 
     * @param chargePointId 充电桩ID
     * @param connectorId 连接器编号
     * @param connectorType 连接器类型
     * @param status 状态
     * @param maxPower 最大功率
     */
    public ConnectorEntity(String chargePointId, Integer connectorId, String connectorType, 
                          String status, BigDecimal maxPower) {
        this.chargePointId = chargePointId;
        this.connectorId = connectorId;
        this.connectorType = connectorType;
        this.status = status;
        this.maxPower = maxPower;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * 默认构造函数（JPA需要）
     */
    protected ConnectorEntity() {
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getter methods
    public Long getId() { return id; }
    public String getChargePointId() { return chargePointId; }
    public Integer getConnectorId() { return connectorId; }
    public String getConnectorType() { return connectorType; }
    public String getStatus() { return status; }
    public BigDecimal getMaxPower() { return maxPower; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setter methods
    public void setId(Long id) { this.id = id; }
    public void setChargePointId(String chargePointId) { this.chargePointId = chargePointId; }
    public void setConnectorId(Integer connectorId) { this.connectorId = connectorId; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
    public void setStatus(String status) { this.status = status; }
    public void setMaxPower(BigDecimal maxPower) { this.maxPower = maxPower; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
