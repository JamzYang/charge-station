package com.charge.station.domain.model.station;

import com.charge.station.domain.event.StationCreatedEvent;
import com.charge.station.domain.event.StationStatusChangedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 充电站聚合根
 * 
 * 管理充电站的生命周期和业务规则，是充电站聚合的入口点。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA需要
public class Station {
    
    private StationId stationId;
    private StationInfo stationInfo;
    private Location location;
    private String operatorId;
    private StationStatus status;
    private BusinessHours businessHours;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    
    // 领域事件列表
    private final List<Object> domainEvents = new ArrayList<>();

    /**
     * 创建新的充电站
     *
     * @param stationInfo 充电站信息
     * @param location 地理位置
     * @param operatorId 运营商ID
     * @param businessHours 营业时间
     */
    public Station(StationInfo stationInfo, Location location, String operatorId, BusinessHours businessHours) {
        this.stationId = StationId.generate();
        this.stationInfo = Objects.requireNonNull(stationInfo, "充电站信息不能为空");
        this.location = Objects.requireNonNull(location, "地理位置不能为空");
        this.operatorId = validateOperatorId(operatorId);
        this.businessHours = Objects.requireNonNull(businessHours, "营业时间不能为空");
        this.status = StationStatus.INACTIVE; // 新建充电站默认为非活跃状态
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;

        // 发布充电站创建事件
        addDomainEvent(new StationCreatedEvent(
            this.stationId,
            this.stationInfo.name(),
            this.location,
            this.operatorId,
            this.createdAt
        ));
    }

    /**
     * 从数据库重建充电站对象（用于持久化层）
     *
     * @param stationId 充电站ID
     * @param stationInfo 充电站信息
     * @param location 地理位置
     * @param operatorId 运营商ID
     * @param status 充电站状态
     * @param businessHours 营业时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @param version 版本号
     * @return 重建的充电站对象
     */
    public static Station reconstruct(
            StationId stationId,
            StationInfo stationInfo,
            Location location,
            String operatorId,
            StationStatus status,
            BusinessHours businessHours,
            Instant createdAt,
            Instant updatedAt,
            Long version) {

        Station station = new Station();
        station.stationId = Objects.requireNonNull(stationId, "充电站ID不能为空");
        station.stationInfo = Objects.requireNonNull(stationInfo, "充电站信息不能为空");
        station.location = Objects.requireNonNull(location, "地理位置不能为空");
        station.operatorId = station.validateOperatorId(operatorId);
        station.status = Objects.requireNonNull(status, "充电站状态不能为空");
        station.businessHours = Objects.requireNonNull(businessHours, "营业时间不能为空");
        station.createdAt = Objects.requireNonNull(createdAt, "创建时间不能为空");
        station.updatedAt = Objects.requireNonNull(updatedAt, "更新时间不能为空");
        station.version = Objects.requireNonNull(version, "版本号不能为空");

        // 从数据库重建的对象不发布领域事件
        return station;
    }

    /**
     * 验证运营商ID
     */
    private String validateOperatorId(String operatorId) {
        Objects.requireNonNull(operatorId, "运营商ID不能为空");
        if (operatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("运营商ID不能为空字符串");
        }
        if (operatorId.length() > 32) {
            throw new IllegalArgumentException("运营商ID长度不能超过32个字符");
        }
        return operatorId.trim();
    }

    /**
     * 更新充电站信息
     * 
     * @param newStationInfo 新的充电站信息
     */
    public void updateStationInfo(StationInfo newStationInfo) {
        Objects.requireNonNull(newStationInfo, "充电站信息不能为空");
        this.stationInfo = newStationInfo;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新地理位置
     * 
     * @param newLocation 新的地理位置
     */
    public void updateLocation(Location newLocation) {
        Objects.requireNonNull(newLocation, "地理位置不能为空");
        this.location = newLocation;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新营业时间
     * 
     * @param newBusinessHours 新的营业时间
     */
    public void updateBusinessHours(BusinessHours newBusinessHours) {
        Objects.requireNonNull(newBusinessHours, "营业时间不能为空");
        this.businessHours = newBusinessHours;
        this.updatedAt = Instant.now();
    }

    /**
     * 激活充电站
     */
    public void activate() {
        if (this.status != StationStatus.ACTIVE) {
            StationStatus oldStatus = this.status;
            this.status = StationStatus.ACTIVE;
            this.updatedAt = Instant.now();
            
            addDomainEvent(new StationStatusChangedEvent(
                this.stationId,
                oldStatus,
                this.status,
                this.updatedAt
            ));
        }
    }

    /**
     * 停用充电站
     */
    public void deactivate() {
        if (this.status != StationStatus.INACTIVE) {
            StationStatus oldStatus = this.status;
            this.status = StationStatus.INACTIVE;
            this.updatedAt = Instant.now();
            
            addDomainEvent(new StationStatusChangedEvent(
                this.stationId,
                oldStatus,
                this.status,
                this.updatedAt
            ));
        }
    }

    /**
     * 设置为维护状态
     */
    public void setMaintenance() {
        if (this.status != StationStatus.MAINTENANCE) {
            StationStatus oldStatus = this.status;
            this.status = StationStatus.MAINTENANCE;
            this.updatedAt = Instant.now();
            
            addDomainEvent(new StationStatusChangedEvent(
                this.stationId,
                oldStatus,
                this.status,
                this.updatedAt
            ));
        }
    }

    /**
     * 检查是否可以提供服务
     * 
     * @return true如果可以提供服务
     */
    public boolean canProvideService() {
        return status.canProvideService() && businessHours.isOpenNow();
    }

    /**
     * 计算到指定位置的距离
     * 
     * @param targetLocation 目标位置
     * @return 距离（米）
     */
    public double distanceTo(Location targetLocation) {
        return location.distanceTo(targetLocation);
    }

    /**
     * 检查是否在指定半径范围内
     * 
     * @param targetLocation 目标位置
     * @param radiusMeters 半径（米）
     * @return true如果在范围内
     */
    public boolean isWithinRadius(Location targetLocation, double radiusMeters) {
        return location.isWithinRadius(targetLocation, radiusMeters);
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
