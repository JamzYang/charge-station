package com.charge.station.infrastructure.persistence.jpa.converter;

import com.charge.station.domain.model.station.*;
import com.charge.station.infrastructure.persistence.jpa.entity.StationEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 充电站实体转换器
 * 
 * 负责充电站领域对象与JPA实体之间的转换。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class StationEntityConverter {
    
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    /**
     * 将充电站领域对象转换为JPA实体
     * 
     * @param station 充电站领域对象
     * @return JPA实体
     */
    public StationEntity toEntity(Station station) {
        Objects.requireNonNull(station, "充电站不能为空");
        
        Point locationPoint = createPoint(station.getLocation());
        
        StationEntity entity = new StationEntity(
            station.getStationId().value(),
            station.getStationInfo().name(),
            station.getStationInfo().address(),
            station.getStationInfo().description(),
            locationPoint,
            station.getOperatorId(),
            station.getStatus().getCode(),
            station.getBusinessHours().openTime(),
            station.getBusinessHours().closeTime()
        );
        
        // 设置时间戳和版本号
        entity.setCreatedAt(station.getCreatedAt());
        entity.setUpdatedAt(station.getUpdatedAt());
        entity.setVersion(station.getVersion());
        
        return entity;
    }
    
    /**
     * 将JPA实体转换为充电站领域对象
     * 
     * @param entity JPA实体
     * @return 充电站领域对象
     */
    public Station toDomain(StationEntity entity) {
        Objects.requireNonNull(entity, "充电站实体不能为空");
        
        StationId stationId = StationId.of(entity.getId());
        StationInfo stationInfo = StationInfo.of(
            entity.getName(), 
            entity.getAddress(), 
            entity.getDescription()
        );
        Location location = createLocation(entity.getLocation());
        StationStatus status = StationStatus.fromCode(entity.getStatus());
        BusinessHours businessHours = new BusinessHours(
            entity.getOpenTime(), 
            entity.getCloseTime()
        );
        
        // 使用静态工厂方法重建充电站对象，保持数据库中的原始ID和时间戳
        return Station.reconstruct(
            stationId,
            stationInfo,
            location,
            entity.getOperatorId(),
            status,
            businessHours,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }
    
    /**
     * 创建PostGIS Point对象
     * 
     * @param location 位置值对象
     * @return PostGIS Point
     */
    private Point createPoint(Location location) {
        Coordinate coordinate = new Coordinate(
            location.longitude().doubleValue(),
            location.latitude().doubleValue()
        );
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326); // WGS84坐标系
        return point;
    }
    
    /**
     * 从PostGIS Point创建位置值对象
     * 
     * @param point PostGIS Point
     * @return 位置值对象
     */
    private Location createLocation(Point point) {
        return Location.of(
            point.getY(), // 纬度
            point.getX()  // 经度
        );
    }
    

}
