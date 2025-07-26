package com.charge.station.infrastructure.persistence.jpa.repository;

import com.charge.station.infrastructure.persistence.jpa.entity.StationEntity;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 充电站Spring Data JPA仓储
 *
 * 提供充电站实体的基础数据访问操作。
 *
 * @author 架构师团队
 * @version 1.0
 */
public interface StationJpaRepository extends JpaRepository<StationEntity, String> {
    
    /**
     * 根据名称查找充电站
     * 
     * @param name 充电站名称
     * @return 充电站实体
     */
    Optional<StationEntity> findByName(String name);
    
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
     * @param excludeId 要排除的充电站ID
     * @return true如果名称已存在
     */
    boolean existsByNameAndIdNot(String name, String excludeId);
    
    /**
     * 根据运营商ID查找充电站列表
     * 
     * @param operatorId 运营商ID
     * @return 充电站列表
     */
    List<StationEntity> findByOperatorId(String operatorId);
    
    /**
     * 根据状态查找充电站列表
     * 
     * @param status 充电站状态
     * @return 充电站列表
     */
    List<StationEntity> findByStatus(String status);
    
    /**
     * 查找指定位置附近的充电站
     *
     * @param centerPoint 中心位置
     * @param radiusMeters 搜索半径（米）
     * @return 附近的充电站列表
     */
    @Query(value = "SELECT * FROM stations s WHERE ST_DWithin(s.location, :centerPoint, :radiusMeters) ORDER BY ST_Distance(s.location, :centerPoint)", nativeQuery = true)
    List<StationEntity> findNearbyStations(@Param("centerPoint") Point centerPoint, @Param("radiusMeters") double radiusMeters);
    
    /**
     * 查找指定位置附近的活跃充电站
     *
     * @param centerPoint 中心位置
     * @param radiusMeters 搜索半径（米）
     * @param activeStatus 活跃状态
     * @return 附近的活跃充电站列表
     */
    @Query(value = "SELECT * FROM stations s WHERE s.status = :activeStatus AND ST_DWithin(s.location, :centerPoint, :radiusMeters) ORDER BY ST_Distance(s.location, :centerPoint)", nativeQuery = true)
    List<StationEntity> findNearbyActiveStations(@Param("centerPoint") Point centerPoint, @Param("radiusMeters") double radiusMeters, @Param("activeStatus") String activeStatus);
    
    /**
     * 分页查询充电站
     * 
     * @param pageable 分页参数
     * @return 充电站列表
     */
    List<StationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
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
    long countByStatus(String status);
}
