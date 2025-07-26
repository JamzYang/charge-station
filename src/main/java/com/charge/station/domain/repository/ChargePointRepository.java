package com.charge.station.domain.repository;

import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.station.StationId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 充电桩仓储接口
 * 
 * 定义充电桩聚合的持久化操作。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public interface ChargePointRepository {

    /**
     * 保存充电桩
     * 
     * @param chargePoint 充电桩聚合
     * @return 保存后的充电桩聚合
     */
    ChargePoint save(ChargePoint chargePoint);

    /**
     * 根据ID查找充电桩
     * 
     * @param chargePointId 充电桩ID
     * @return 充电桩聚合，如果不存在则返回空
     */
    Optional<ChargePoint> findById(ChargePointId chargePointId);

    /**
     * 根据序列号查找充电桩
     * 
     * @param serialNumber 序列号
     * @return 充电桩聚合，如果不存在则返回空
     */
    Optional<ChargePoint> findBySerialNumber(String serialNumber);

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
     * @param excludeChargePointId 要排除的充电桩ID
     * @return true如果序列号已存在
     */
    boolean existsBySerialNumberAndIdNot(String serialNumber, ChargePointId excludeChargePointId);

    /**
     * 根据充电站ID查找充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 充电桩列表
     */
    List<ChargePoint> findByStationId(StationId stationId);

    /**
     * 根据状态查找充电桩列表
     * 
     * @param status 充电桩状态
     * @return 充电桩列表
     */
    List<ChargePoint> findByStatus(DeviceStatus status);

    /**
     * 根据充电站ID和状态查找充电桩列表
     * 
     * @param stationId 充电站ID
     * @param status 充电桩状态
     * @return 充电桩列表
     */
    List<ChargePoint> findByStationIdAndStatus(StationId stationId, DeviceStatus status);

    /**
     * 查找可用的充电桩列表
     * 
     * @return 可用的充电桩列表
     */
    List<ChargePoint> findAvailableChargePoints();

    /**
     * 根据充电站ID查找可用的充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 可用的充电桩列表
     */
    List<ChargePoint> findAvailableChargePointsByStationId(StationId stationId);

    /**
     * 查找离线的充电桩列表（超过指定时间没有心跳）
     * 
     * @param lastHeartbeatBefore 最后心跳时间之前
     * @return 离线的充电桩列表
     */
    List<ChargePoint> findOfflineChargePoints(Instant lastHeartbeatBefore);

    /**
     * 分页查询充电桩
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 充电桩列表
     */
    List<ChargePoint> findAll(int page, int size);

    /**
     * 统计充电桩总数
     * 
     * @return 充电桩总数
     */
    long count();

    /**
     * 根据充电站ID统计充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 充电桩数量
     */
    long countByStationId(StationId stationId);

    /**
     * 根据状态统计充电桩数量
     * 
     * @param status 充电桩状态
     * @return 充电桩数量
     */
    long countByStatus(DeviceStatus status);

    /**
     * 根据充电站ID和状态统计充电桩数量
     * 
     * @param stationId 充电站ID
     * @param status 充电桩状态
     * @return 充电桩数量
     */
    long countByStationIdAndStatus(StationId stationId, DeviceStatus status);

    /**
     * 删除充电桩
     * 
     * @param chargePointId 充电桩ID
     */
    void deleteById(ChargePointId chargePointId);

    /**
     * 根据充电站ID删除所有充电桩
     * 
     * @param stationId 充电站ID
     */
    void deleteByStationId(StationId stationId);

    /**
     * 检查充电桩是否存在
     * 
     * @param chargePointId 充电桩ID
     * @return true如果存在
     */
    boolean existsById(ChargePointId chargePointId);
}
