package com.charge.station.infrastructure.persistence.jpa.repository;

import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.model.station.StationStatus;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.infrastructure.persistence.jpa.converter.StationEntityConverter;
import com.charge.station.infrastructure.persistence.jpa.entity.StationEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 充电站仓储JPA适配器
 *
 * 实现充电站仓储接口，提供基于JPA的数据持久化操作。
 * 
 * 注意：类名避免使用 "JpaRepositoryImpl" 后缀，以免与 Spring Data JPA 的命名约定冲突
 *
 * @author 架构师团队
 * @version 1.0
 */
@Repository("stationRepositoryImpl")
public class StationRepositoryImpl implements StationRepository {

    private static final Logger log = LoggerFactory.getLogger(StationRepositoryImpl.class);

    private final StationJpaRepository jpaRepository;
    private final StationEntityConverter converter;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public StationRepositoryImpl(StationJpaRepository jpaRepository,
                                      StationEntityConverter converter) {
        this.jpaRepository = jpaRepository;
        this.converter = converter;
    }

    
    @Override
    public Station save(Station station) {
        Objects.requireNonNull(station, "充电站不能为空");
        
        log.debug("保存充电站: {}", station.getStationId().value());
        
        StationEntity entity = converter.toEntity(station);
        StationEntity savedEntity = jpaRepository.save(entity);
        
        log.debug("充电站保存成功: {}", savedEntity.getId());
        
        return converter.toDomain(savedEntity);
    }
    
    @Override
    public Optional<Station> findById(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.debug("根据ID查找充电站: {}", stationId.value());
        
        return jpaRepository.findById(stationId.value())
            .map(converter::toDomain);
    }
    
    @Override
    public Optional<Station> findByName(String name) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        
        log.debug("根据名称查找充电站: {}", name);
        
        return jpaRepository.findByName(name)
            .map(converter::toDomain);
    }
    
    @Override
    public boolean existsByName(String name) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        
        log.debug("检查充电站名称是否存在: {}", name);
        
        return jpaRepository.existsByName(name);
    }
    
    @Override
    public boolean existsByNameAndIdNot(String name, StationId excludeStationId) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        Objects.requireNonNull(excludeStationId, "排除的充电站ID不能为空");
        
        log.debug("检查充电站名称是否存在（排除ID: {}）: {}", excludeStationId.value(), name);
        
        return jpaRepository.existsByNameAndIdNot(name, excludeStationId.value());
    }
    
    @Override
    public List<Station> findByOperatorId(String operatorId) {
        Objects.requireNonNull(operatorId, "运营商ID不能为空");
        
        log.debug("根据运营商ID查找充电站: {}", operatorId);
        
        return jpaRepository.findByOperatorId(operatorId)
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Station> findByStatus(StationStatus status) {
        Objects.requireNonNull(status, "充电站状态不能为空");
        
        log.debug("根据状态查找充电站: {}", status.getCode());
        
        return jpaRepository.findByStatus(status.getCode())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Station> findNearbyStations(Location centerLocation, double radiusMeters) {
        Objects.requireNonNull(centerLocation, "中心位置不能为空");
        
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("搜索半径必须大于0");
        }
        
        log.debug("查找附近充电站: 中心位置({}, {}), 半径{}米", 
            centerLocation.latitude(), centerLocation.longitude(), radiusMeters);
        
        Point centerPoint = createPoint(centerLocation);
        
        return jpaRepository.findNearbyStations(centerPoint, radiusMeters)
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Station> findNearbyActiveStations(Location centerLocation, double radiusMeters) {
        Objects.requireNonNull(centerLocation, "中心位置不能为空");
        
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("搜索半径必须大于0");
        }
        
        log.debug("查找附近活跃充电站: 中心位置({}, {}), 半径{}米", 
            centerLocation.latitude(), centerLocation.longitude(), radiusMeters);
        
        Point centerPoint = createPoint(centerLocation);
        
        return jpaRepository.findNearbyActiveStations(centerPoint, radiusMeters, StationStatus.ACTIVE.getCode())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Station> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }
        
        log.debug("分页查询充电站: 页码{}, 每页{}条", page, size);
        
        PageRequest pageRequest = PageRequest.of(page, size);
        
        return jpaRepository.findAllByOrderByCreatedAtDesc(pageRequest)
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        log.debug("统计充电站总数");
        
        return jpaRepository.count();
    }
    
    @Override
    public long countByOperatorId(String operatorId) {
        Objects.requireNonNull(operatorId, "运营商ID不能为空");
        
        log.debug("根据运营商ID统计充电站数量: {}", operatorId);
        
        return jpaRepository.countByOperatorId(operatorId);
    }
    
    @Override
    public long countByStatus(StationStatus status) {
        Objects.requireNonNull(status, "充电站状态不能为空");
        
        log.debug("根据状态统计充电站数量: {}", status.getCode());
        
        return jpaRepository.countByStatus(status.getCode());
    }
    
    @Override
    public void deleteById(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.debug("删除充电站: {}", stationId.value());
        
        jpaRepository.deleteById(stationId.value());
        
        log.debug("充电站删除成功: {}", stationId.value());
    }
    
    @Override
    public boolean existsById(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.debug("检查充电站是否存在: {}", stationId.value());
        
        return jpaRepository.existsById(stationId.value());
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
}
