package com.charge.station.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.LocalTime;

/**
 * 充电站JPA实体
 *
 * 映射到stations表，提供充电站数据的持久化支持。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Entity
@Table(name = "stations", indexes = {
    @Index(name = "idx_stations_name", columnList = "name", unique = true),
    @Index(name = "idx_stations_location", columnList = "location"),
    @Index(name = "idx_stations_operator", columnList = "operator_id"),
    @Index(name = "idx_stations_status", columnList = "status")
})
public class StationEntity {
    
    @Id
    @Column(name = "id", length = 32)
    private String id;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "address", length = 200, nullable = false)
    private String address;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point location;
    
    @Column(name = "operator_id", length = 32, nullable = false)
    private String operatorId;
    
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;
    
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    /**
     * 构造函数
     * 
     * @param id 充电站ID
     * @param name 充电站名称
     * @param address 地址
     * @param description 描述
     * @param location 地理位置
     * @param operatorId 运营商ID
     * @param status 状态
     * @param openTime 开始营业时间
     * @param closeTime 结束营业时间
     */
    public StationEntity(String id, String name, String address, String description, 
                        Point location, String operatorId, String status, 
                        LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.location = location;
        this.operatorId = operatorId;
        this.status = status;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }
    
    /**
     * 默认构造函数（JPA需要）
     */
    protected StationEntity() {
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getter methods
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public Point getLocation() { return location; }
    public String getOperatorId() { return operatorId; }
    public String getStatus() { return status; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    // Setter methods
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(Point location) { this.location = location; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public void setStatus(String status) { this.status = status; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public void setVersion(Long version) { this.version = version; }
}
