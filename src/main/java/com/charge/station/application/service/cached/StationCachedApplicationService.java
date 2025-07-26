package com.charge.station.application.service.cached;

import com.charge.station.application.command.CreateStationCommand;
import com.charge.station.application.command.UpdateStationCommand;
import com.charge.station.application.service.StationApplicationService;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.infrastructure.cache.CacheConfig;
import com.charge.station.infrastructure.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 带缓存的充电站应用服务
 * 
 * 使用装饰器模式为充电站应用服务添加缓存功能，实现Cache-Aside模式。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class StationCachedApplicationService {

    private final StationApplicationService stationApplicationService;
    private final CacheService cacheService;

    /**
     * 创建充电站
     * 
     * @param command 创建充电站命令
     * @return 创建的充电站
     */
    public Station createStation(CreateStationCommand command) {
        Station station = stationApplicationService.createStation(command);
        
        // 缓存新创建的充电站
        cacheStation(station);
        
        // 清除相关缓存
        evictNearbyStationsCache();
        
        return station;
    }

    /**
     * 更新充电站
     * 
     * @param command 更新充电站命令
     * @return 更新后的充电站
     */
    public Station updateStation(UpdateStationCommand command) {
        Station station = stationApplicationService.updateStation(command);
        
        // 更新缓存
        cacheStation(station);
        
        // 清除相关缓存
        evictNearbyStationsCache();
        
        return station;
    }

    /**
     * 根据ID查找充电站
     * 
     * @param stationId 充电站ID
     * @return 充电站
     */
    public Station findStationById(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        String cacheKey = stationId.value();
        
        return cacheService.getOrSet(
            CacheConfig.STATION_CACHE,
            cacheKey,
            Station.class,
            () -> {
                log.debug("从数据库加载充电站: stationId={}", stationId);
                return stationApplicationService.findStationById(stationId);
            }
        );
    }

    /**
     * 根据名称查找充电站
     * 
     * @param name 充电站名称
     * @return 充电站
     */
    public Station findStationByName(String name) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        
        String cacheKey = name.trim();
        
        return cacheService.getOrSet(
            CacheConfig.STATION_BY_NAME_CACHE,
            cacheKey,
            Station.class,
            () -> {
                log.debug("从数据库加载充电站: name={}", name);
                return stationApplicationService.findStationByName(name);
            }
        );
    }

    /**
     * 查找附近的充电站
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param radiusMeters 搜索半径（米）
     * @return 附近的充电站列表
     */
    @SuppressWarnings("unchecked")
    public List<Station> findNearbyStations(double latitude, double longitude, double radiusMeters) {
        String cacheKey = String.format("%.6f,%.6f,%.0f", latitude, longitude, radiusMeters);
        
        return cacheService.getOrSet(
            CacheConfig.NEARBY_STATIONS_CACHE,
            cacheKey,
            List.class,
            () -> {
                log.debug("从数据库加载附近充电站: latitude={}, longitude={}, radius={}", 
                    latitude, longitude, radiusMeters);
                return stationApplicationService.findNearbyStations(latitude, longitude, radiusMeters);
            }
        );
    }

    /**
     * 激活充电站
     * 
     * @param stationId 充电站ID
     * @return 激活后的充电站
     */
    public Station activateStation(StationId stationId) {
        Station station = stationApplicationService.activateStation(stationId);
        
        // 更新缓存
        cacheStation(station);
        
        return station;
    }

    /**
     * 停用充电站
     * 
     * @param stationId 充电站ID
     * @return 停用后的充电站
     */
    public Station deactivateStation(StationId stationId) {
        Station station = stationApplicationService.deactivateStation(stationId);
        
        // 更新缓存
        cacheStation(station);
        
        return station;
    }

    /**
     * 设置充电站为维护状态
     * 
     * @param stationId 充电站ID
     * @return 设置维护状态后的充电站
     */
    public Station setStationMaintenance(StationId stationId) {
        Station station = stationApplicationService.setStationMaintenance(stationId);
        
        // 更新缓存
        cacheStation(station);
        
        return station;
    }

    /**
     * 删除充电站
     * 
     * @param stationId 充电站ID
     */
    public void deleteStation(StationId stationId) {
        stationApplicationService.deleteStation(stationId);
        
        // 清除缓存
        evictStationCache(stationId);
        evictNearbyStationsCache();
    }

    /**
     * 分页查询充电站
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 充电站列表
     */
    public List<Station> findStations(int page, int size) {
        // 分页查询不缓存，因为数据变化频繁
        return stationApplicationService.findStations(page, size);
    }

    /**
     * 统计充电站总数
     * 
     * @return 充电站总数
     */
    public long countStations() {
        // 统计数据不缓存，因为变化频繁
        return stationApplicationService.countStations();
    }

    /**
     * 缓存充电站
     * 
     * @param station 充电站
     */
    private void cacheStation(Station station) {
        if (station == null) {
            return;
        }
        
        // 按ID缓存
        cacheService.put(CacheConfig.STATION_CACHE, station.getStationId().value(), station);
        
        // 按名称缓存
        cacheService.put(CacheConfig.STATION_BY_NAME_CACHE, station.getStationInfo().name(), station);
        
        log.debug("充电站已缓存: stationId={}, name={}", 
            station.getStationId(), station.getStationInfo().name());
    }

    /**
     * 清除充电站缓存
     * 
     * @param stationId 充电站ID
     */
    private void evictStationCache(StationId stationId) {
        cacheService.evict(CacheConfig.STATION_CACHE, stationId.value());
        
        // 注意：按名称的缓存需要知道名称才能清除，这里简化处理
        // 实际项目中可以考虑维护一个ID到名称的映射
        
        log.debug("充电站缓存已清除: stationId={}", stationId);
    }

    /**
     * 清除附近充电站缓存
     */
    private void evictNearbyStationsCache() {
        cacheService.clear(CacheConfig.NEARBY_STATIONS_CACHE);
        log.debug("附近充电站缓存已清空");
    }
}
