package com.charge.station.domain.service;

import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.repository.ChargePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 充电桩领域服务
 * 
 * 提供充电桩相关的业务逻辑和规则验证。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ChargePointDomainService {

    private final ChargePointRepository chargePointRepository;

    /**
     * 检查充电桩序列号是否已存在
     * 
     * @param serialNumber 序列号
     * @return true如果序列号已存在
     */
    public boolean isSerialNumberExists(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return false; // 空序列号不检查重复
        }
        return chargePointRepository.existsBySerialNumber(serialNumber.trim());
    }

    /**
     * 检查充电桩序列号是否已存在（排除指定充电桩）
     * 
     * @param serialNumber 序列号
     * @param excludeChargePointId 要排除的充电桩ID
     * @return true如果序列号已存在
     */
    public boolean isSerialNumberExists(String serialNumber, ChargePointId excludeChargePointId) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return false; // 空序列号不检查重复
        }
        Objects.requireNonNull(excludeChargePointId, "充电桩ID不能为空");
        return chargePointRepository.existsBySerialNumberAndIdNot(serialNumber.trim(), excludeChargePointId);
    }

    /**
     * 验证充电桩序列号的唯一性
     * 
     * @param serialNumber 序列号
     * @throws IllegalArgumentException 如果序列号已存在
     */
    public void validateSerialNumberUniqueness(String serialNumber) {
        if (isSerialNumberExists(serialNumber)) {
            throw new IllegalArgumentException("充电桩序列号'" + serialNumber + "'已存在");
        }
    }

    /**
     * 验证充电桩序列号的唯一性（排除指定充电桩）
     * 
     * @param serialNumber 序列号
     * @param excludeChargePointId 要排除的充电桩ID
     * @throws IllegalArgumentException 如果序列号已存在
     */
    public void validateSerialNumberUniqueness(String serialNumber, ChargePointId excludeChargePointId) {
        if (isSerialNumberExists(serialNumber, excludeChargePointId)) {
            throw new IllegalArgumentException("充电桩序列号'" + serialNumber + "'已存在");
        }
    }

    /**
     * 检查充电桩是否可以启动充电
     * 
     * @param chargePoint 充电桩
     * @return true如果可以启动充电
     */
    public boolean canStartCharging(ChargePoint chargePoint) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        // 检查充电桩状态
        if (!chargePoint.isAvailableForCharging()) {
            return false;
        }
        
        // 检查是否在线
        if (!chargePoint.isOnline()) {
            return false;
        }
        
        // 检查是否有可用的连接器
        return chargePoint.hasAvailableConnector();
    }

    /**
     * 验证充电桩是否可以启动充电
     * 
     * @param chargePoint 充电桩
     * @throws IllegalStateException 如果不能启动充电
     */
    public void validateCanStartCharging(ChargePoint chargePoint) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        if (!chargePoint.isAvailableForCharging()) {
            throw new IllegalStateException("充电桩当前状态不可用：" + chargePoint.getStatus());
        }
        
        if (!chargePoint.isOnline()) {
            throw new IllegalStateException("充电桩离线，无法启动充电");
        }
        
        if (!chargePoint.hasAvailableConnector()) {
            throw new IllegalStateException("充电桩没有可用的充电枪");
        }
    }

    /**
     * 查找充电站下的可用充电桩
     * 
     * @param stationId 充电站ID
     * @return 可用的充电桩列表
     */
    public List<ChargePoint> findAvailableChargePointsInStation(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        return chargePointRepository.findAvailableChargePointsByStationId(stationId);
    }

    /**
     * 统计充电站下的可用充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 可用充电桩数量
     */
    public long countAvailableChargePointsInStation(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        return chargePointRepository.countByStationIdAndStatus(stationId, DeviceStatus.AVAILABLE);
    }

    /**
     * 查找离线的充电桩
     * 
     * @param offlineThresholdMinutes 离线阈值（分钟）
     * @return 离线的充电桩列表
     */
    public List<ChargePoint> findOfflineChargePoints(int offlineThresholdMinutes) {
        if (offlineThresholdMinutes <= 0) {
            throw new IllegalArgumentException("离线阈值必须大于0");
        }
        
        Instant threshold = Instant.now().minusSeconds(offlineThresholdMinutes * 60L);
        return chargePointRepository.findOfflineChargePoints(threshold);
    }

    /**
     * 检查充电桩是否应该被标记为离线
     * 
     * @param chargePoint 充电桩
     * @param offlineThresholdMinutes 离线阈值（分钟）
     * @return true如果应该被标记为离线
     */
    public boolean shouldMarkAsOffline(ChargePoint chargePoint, int offlineThresholdMinutes) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        if (offlineThresholdMinutes <= 0) {
            throw new IllegalArgumentException("离线阈值必须大于0");
        }
        
        if (chargePoint.getLastHeartbeat() == null) {
            return true; // 从未有过心跳，应该标记为离线
        }
        
        Instant threshold = Instant.now().minusSeconds(offlineThresholdMinutes * 60L);
        return chargePoint.getLastHeartbeat().isBefore(threshold);
    }

    /**
     * 批量更新离线充电桩的状态
     * 
     * @param offlineThresholdMinutes 离线阈值（分钟）
     * @return 更新的充电桩数量
     */
    public int markOfflineChargePoints(int offlineThresholdMinutes) {
        List<ChargePoint> offlineChargePoints = findOfflineChargePoints(offlineThresholdMinutes);
        
        int updatedCount = 0;
        for (ChargePoint chargePoint : offlineChargePoints) {
            if (chargePoint.getStatus() != DeviceStatus.OFFLINE) {
                chargePoint.updateStatus(DeviceStatus.OFFLINE, Instant.now());
                chargePointRepository.save(chargePoint);
                updatedCount++;
            }
        }
        
        return updatedCount;
    }

    /**
     * 验证充电桩是否属于指定充电站
     * 
     * @param chargePoint 充电桩
     * @param expectedStationId 期望的充电站ID
     * @throws IllegalArgumentException 如果充电桩不属于指定充电站
     */
    public void validateChargePointBelongsToStation(ChargePoint chargePoint, StationId expectedStationId) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        Objects.requireNonNull(expectedStationId, "充电站ID不能为空");
        
        if (!chargePoint.getStationId().equals(expectedStationId)) {
            throw new IllegalArgumentException(
                String.format("充电桩%s不属于充电站%s", 
                    chargePoint.getChargePointId().value(), 
                    expectedStationId.value())
            );
        }
    }
}
