package com.charge.station.domain.repository;

import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.model.station.StationStatus;

import java.util.List;
import java.util.Optional;

/**
 * 充电站仓储接口
 * 
 * 定义充电站聚合的持久化操作。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public interface StationRepository {

    /**
     * 保存充电站
     * 
     * @param station 充电站聚合
     * @return 保存后的充电站聚合
     */
    Station save(Station station);

    /**
     * 根据ID查找充电站
     * 
     * @param stationId 充电站ID
     * @return 充电站聚合，如果不存在则返回空
     */
    Optional<Station> findById(StationId stationId);

    /**
     * 根据名称查找充电站
     * 
     * @param name 充电站名称
     * @return 充电站聚合，如果不存在则返回空
     */
    Optional<Station> findByName(String name);

    /**
     * 检查充电站名称是否存在
     * 
     * @param name 充电站名称
     * @return true如果名称已存在
     */
    boolean existsByName(String name);

    /**
     * 检查充电站名称是否存在（排除指定ID）
     * 
     * @param name 充电站名称
     * @param excludeStationId 要排除的充电站ID
     * @return true如果名称已存在
     */
    boolean existsByNameAndIdNot(String name, StationId excludeStationId);

    /**
     * 根据运营商ID查找充电站列表
     * 
     * @param operatorId 运营商ID
     * @return 充电站列表
     */
    List<Station> findByOperatorId(String operatorId);

    /**
     * 根据状态查找充电站列表
     * 
     * @param status 充电站状态
     * @return 充电站列表
     */
    List<Station> findByStatus(StationStatus status);

    /**
     * 查找指定位置附近的充电站
     * 
     * @param centerLocation 中心位置
     * @param radiusMeters 搜索半径（米）
     * @return 附近的充电站列表
     */
    List<Station> findNearbyStations(Location centerLocation, double radiusMeters);

    /**
     * 查找指定位置附近的活跃充电站
     * 
     * @param centerLocation 中心位置
     * @param radiusMeters 搜索半径（米）
     * @return 附近的活跃充电站列表
     */
    List<Station> findNearbyActiveStations(Location centerLocation, double radiusMeters);

    /**
     * 分页查询充电站
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 充电站列表
     */
    List<Station> findAll(int page, int size);

    /**
     * 统计充电站总数
     * 
     * @return 充电站总数
     */
    long count();

    /**
     * 根据运营商ID统计充电站数量
     * 
     * @param operatorId 运营商ID
     * @return 充电站数量
     */
    long countByOperatorId(String operatorId);

    /**
     * 根据状态统计充电站数量
     * 
     * @param status 充电站状态
     * @return 充电站数量
     */
    long countByStatus(StationStatus status);

    /**
     * 删除充电站
     * 
     * @param stationId 充电站ID
     */
    void deleteById(StationId stationId);

    /**
     * 检查充电站是否存在
     * 
     * @param stationId 充电站ID
     * @return true如果存在
     */
    boolean existsById(StationId stationId);
}
