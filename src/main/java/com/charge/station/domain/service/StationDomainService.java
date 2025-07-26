package com.charge.station.domain.service;

import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 充电站领域服务
 * 
 * 提供充电站相关的业务逻辑和规则验证。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class StationDomainService {

    private final StationRepository stationRepository;

    /**
     * 检查充电站名称是否已存在
     * 
     * @param name 充电站名称
     * @return true如果名称已存在
     */
    public boolean isStationNameExists(String name) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        return stationRepository.existsByName(name.trim());
    }

    /**
     * 检查充电站名称是否已存在（排除指定充电站）
     * 
     * @param name 充电站名称
     * @param excludeStationId 要排除的充电站ID
     * @return true如果名称已存在
     */
    public boolean isStationNameExists(String name, StationId excludeStationId) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        Objects.requireNonNull(excludeStationId, "充电站ID不能为空");
        return stationRepository.existsByNameAndIdNot(name.trim(), excludeStationId);
    }

    /**
     * 验证充电站名称的唯一性
     * 
     * @param name 充电站名称
     * @throws IllegalArgumentException 如果名称已存在
     */
    public void validateStationNameUniqueness(String name) {
        if (isStationNameExists(name)) {
            throw new IllegalArgumentException("充电站名称'" + name + "'已存在");
        }
    }

    /**
     * 验证充电站名称的唯一性（排除指定充电站）
     * 
     * @param name 充电站名称
     * @param excludeStationId 要排除的充电站ID
     * @throws IllegalArgumentException 如果名称已存在
     */
    public void validateStationNameUniqueness(String name, StationId excludeStationId) {
        if (isStationNameExists(name, excludeStationId)) {
            throw new IllegalArgumentException("充电站名称'" + name + "'已存在");
        }
    }

    /**
     * 查找附近的充电站
     * 
     * @param centerLocation 中心位置
     * @param radiusMeters 搜索半径（米）
     * @return 附近的充电站列表，按距离排序
     */
    public List<Station> findNearbyStations(Location centerLocation, double radiusMeters) {
        Objects.requireNonNull(centerLocation, "中心位置不能为空");
        
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("搜索半径必须大于0");
        }
        
        if (radiusMeters > 50000) { // 最大50公里
            throw new IllegalArgumentException("搜索半径不能超过50公里");
        }
        
        List<Station> nearbyStations = stationRepository.findNearbyStations(centerLocation, radiusMeters);
        
        // 按距离排序
        nearbyStations.sort((s1, s2) -> {
            double distance1 = s1.distanceTo(centerLocation);
            double distance2 = s2.distanceTo(centerLocation);
            return Double.compare(distance1, distance2);
        });
        
        return nearbyStations;
    }

    /**
     * 查找附近的活跃充电站
     * 
     * @param centerLocation 中心位置
     * @param radiusMeters 搜索半径（米）
     * @return 附近的活跃充电站列表，按距离排序
     */
    public List<Station> findNearbyActiveStations(Location centerLocation, double radiusMeters) {
        Objects.requireNonNull(centerLocation, "中心位置不能为空");
        
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("搜索半径必须大于0");
        }
        
        if (radiusMeters > 50000) { // 最大50公里
            throw new IllegalArgumentException("搜索半径不能超过50公里");
        }
        
        List<Station> nearbyActiveStations = stationRepository.findNearbyActiveStations(centerLocation, radiusMeters);
        
        // 按距离排序
        nearbyActiveStations.sort((s1, s2) -> {
            double distance1 = s1.distanceTo(centerLocation);
            double distance2 = s2.distanceTo(centerLocation);
            return Double.compare(distance1, distance2);
        });
        
        return nearbyActiveStations;
    }

    /**
     * 检查位置是否过于接近现有充电站
     * 
     * @param location 要检查的位置
     * @param minDistanceMeters 最小距离（米）
     * @return true如果位置过于接近现有充电站
     */
    public boolean isTooCloseToExistingStation(Location location, double minDistanceMeters) {
        Objects.requireNonNull(location, "位置不能为空");
        
        if (minDistanceMeters <= 0) {
            throw new IllegalArgumentException("最小距离必须大于0");
        }
        
        List<Station> nearbyStations = stationRepository.findNearbyStations(location, minDistanceMeters);
        return !nearbyStations.isEmpty();
    }

    /**
     * 检查位置是否过于接近现有充电站（排除指定充电站）
     * 
     * @param location 要检查的位置
     * @param minDistanceMeters 最小距离（米）
     * @param excludeStationId 要排除的充电站ID
     * @return true如果位置过于接近现有充电站
     */
    public boolean isTooCloseToExistingStation(Location location, double minDistanceMeters, StationId excludeStationId) {
        Objects.requireNonNull(location, "位置不能为空");
        Objects.requireNonNull(excludeStationId, "充电站ID不能为空");
        
        if (minDistanceMeters <= 0) {
            throw new IllegalArgumentException("最小距离必须大于0");
        }
        
        List<Station> nearbyStations = stationRepository.findNearbyStations(location, minDistanceMeters);
        return nearbyStations.stream()
            .anyMatch(station -> !station.getStationId().equals(excludeStationId));
    }

    /**
     * 验证充电站位置的合理性
     * 
     * @param location 充电站位置
     * @param minDistanceMeters 与现有充电站的最小距离（米）
     * @throws IllegalArgumentException 如果位置不合理
     */
    public void validateStationLocation(Location location, double minDistanceMeters) {
        if (isTooCloseToExistingStation(location, minDistanceMeters)) {
            throw new IllegalArgumentException(
                String.format("充电站位置过于接近现有充电站，最小距离应为%.0f米", minDistanceMeters)
            );
        }
    }

    /**
     * 验证充电站位置的合理性（排除指定充电站）
     * 
     * @param location 充电站位置
     * @param minDistanceMeters 与现有充电站的最小距离（米）
     * @param excludeStationId 要排除的充电站ID
     * @throws IllegalArgumentException 如果位置不合理
     */
    public void validateStationLocation(Location location, double minDistanceMeters, StationId excludeStationId) {
        if (isTooCloseToExistingStation(location, minDistanceMeters, excludeStationId)) {
            throw new IllegalArgumentException(
                String.format("充电站位置过于接近现有充电站，最小距离应为%.0f米", minDistanceMeters)
            );
        }
    }
}
