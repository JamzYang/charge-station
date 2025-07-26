package com.charge.station.infrastructure.persistence.jpa.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.charge.station.infrastructure.persistence.jpa.entity.ChargePointEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 充电桩Spring Data JPA仓储
 *
 * 提供充电桩实体的基础数据访问操作。
 *
 * @author 架构师团队
 * @version 1.0
 */
public interface ChargePointJpaRepository extends JpaRepository<ChargePointEntity, String> {
    
    /**
     * 根据ID查找充电桩（包含关联的充电枪）
     *
     * @param id 充电桩ID
     * @return 充电桩实体
     */
    @Query("SELECT cp FROM ChargePointEntity cp LEFT JOIN FETCH cp.connectors WHERE cp.id = :id")
    Optional<ChargePointEntity> findByIdWithConnectors(@Param("id") String id);

    /**
     * 根据序列号查找充电桩
     *
     * @param serialNumber 序列号
     * @return 充电桩实体
     */
    Optional<ChargePointEntity> findBySerialNumber(String serialNumber);
    
    /**
     * 检查序列号是否存在
     * 
     * @param serialNumber 序列号
     * @return true如果序列号已存在
     */
    boolean existsBySerialNumber(String serialNumber);
    
    /**
     * 检查序列号是否存在（排除指定ID）
     * 
     * @param serialNumber 序列号
     * @param excludeId 要排除的充电桩ID
     * @return true如果序列号已存在
     */
    boolean existsBySerialNumberAndIdNot(String serialNumber, String excludeId);
    
    /**
     * 根据充电站ID查找充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 充电桩列表
     */
    List<ChargePointEntity> findByStationId(String stationId);
    
    /**
     * 根据状态查找充电桩列表
     * 
     * @param status 充电桩状态
     * @return 充电桩列表
     */
    List<ChargePointEntity> findByStatus(String status);
    
    /**
     * 根据充电站ID和状态查找充电桩列表
     * 
     * @param stationId 充电站ID
     * @param status 充电桩状态
     * @return 充电桩列表
     */
    List<ChargePointEntity> findByStationIdAndStatus(String stationId, String status);
    
    /**
     * 查找可用的充电桩列表
     *
     * @param availableStatus 可用状态
     * @return 可用的充电桩列表
     */
    List<ChargePointEntity> findByStatusOrderByCreatedAtDesc(String availableStatus);

    /**
     * 根据充电站ID查找可用的充电桩列表
     *
     * @param stationId 充电站ID
     * @param availableStatus 可用状态
     * @return 可用的充电桩列表
     */
    List<ChargePointEntity> findByStationIdAndStatusOrderByCreatedAtDesc(String stationId, String availableStatus);
    
    /**
     * 查找离线的充电桩列表（超过指定时间没有心跳）
     * 
     * @param lastHeartbeatBefore 最后心跳时间之前
     * @return 离线的充电桩列表
     */
    @Query("SELECT cp FROM ChargePointEntity cp WHERE cp.lastHeartbeat IS NULL OR cp.lastHeartbeat < :lastHeartbeatBefore")
    List<ChargePointEntity> findOfflineChargePoints(@Param("lastHeartbeatBefore") Instant lastHeartbeatBefore);
    
    /**
     * 分页查询充电桩
     * 
     * @param pageable 分页参数
     * @return 充电桩列表
     */
    List<ChargePointEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据充电站ID统计充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 充电桩数量
     */
    long countByStationId(String stationId);
    
    /**
     * 根据状态统计充电桩数量
     * 
     * @param status 充电桩状态
     * @return 充电桩数量
     */
    long countByStatus(String status);
    
    /**
     * 根据充电站ID和状态统计充电桩数量
     * 
     * @param stationId 充电站ID
     * @param status 充电桩状态
     * @return 充电桩数量
     */
    long countByStationIdAndStatus(String stationId, String status);
    
    /**
     * 根据充电站ID删除所有充电桩
     * 
     * @param stationId 充电站ID
     */
    void deleteByStationId(String stationId);
}
