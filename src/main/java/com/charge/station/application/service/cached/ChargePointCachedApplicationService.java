package com.charge.station.application.service.cached;

import com.charge.station.application.command.CreateChargePointCommand;
import com.charge.station.application.service.ChargePointApplicationService;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.infrastructure.cache.CacheConfig;
import com.charge.station.infrastructure.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 带缓存的充电桩应用服务
 *
 * 使用装饰器模式为充电桩应用服务添加缓存功能，实现Cache-Aside模式。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Service
public class ChargePointCachedApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ChargePointCachedApplicationService.class);

    private final ChargePointApplicationService chargePointApplicationService;
    private final CacheService cacheService;

    public ChargePointCachedApplicationService(ChargePointApplicationService chargePointApplicationService,
                                              CacheService cacheService) {
        this.chargePointApplicationService = chargePointApplicationService;
        this.cacheService = cacheService;
    }

    /**
     * 创建充电桩
     * 
     * @param command 创建充电桩命令
     * @return 创建的充电桩
     */
    public ChargePoint createChargePoint(CreateChargePointCommand command) {
        ChargePoint chargePoint = chargePointApplicationService.createChargePoint(command);
        
        // 缓存新创建的充电桩
        cacheChargePoint(chargePoint);
        
        return chargePoint;
    }

    /**
     * 根据ID查找充电桩
     * 
     * @param chargePointId 充电桩ID
     * @return 充电桩
     */
    public ChargePoint findChargePointById(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        
        String cacheKey = chargePointId.value();
        
        return cacheService.getOrSet(
            CacheConfig.CHARGE_POINT_CACHE,
            cacheKey,
            ChargePoint.class,
            () -> {
                log.debug("从数据库加载充电桩: chargePointId={}", chargePointId);
                return chargePointApplicationService.findChargePointById(chargePointId);
            }
        );
    }

    /**
     * 根据序列号查找充电桩
     * 
     * @param serialNumber 序列号
     * @return 充电桩
     */
    public ChargePoint findChargePointBySerialNumber(String serialNumber) {
        Objects.requireNonNull(serialNumber, "序列号不能为空");
        
        String cacheKey = serialNumber.trim();
        
        return cacheService.getOrSet(
            CacheConfig.CHARGE_POINT_BY_SERIAL_CACHE,
            cacheKey,
            ChargePoint.class,
            () -> {
                log.debug("从数据库加载充电桩: serialNumber={}", serialNumber);
                return chargePointApplicationService.findChargePointBySerialNumber(serialNumber);
            }
        );
    }

    /**
     * 根据充电站ID查找充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 充电桩列表
     */
    public List<ChargePoint> findChargePointsByStationId(StationId stationId) {
        // 充电桩列表变化频繁，不缓存
        return chargePointApplicationService.findChargePointsByStationId(stationId);
    }

    /**
     * 查找可用的充电桩列表
     * 
     * @return 可用的充电桩列表
     */
    public List<ChargePoint> findAvailableChargePoints() {
        // 可用充电桩列表变化频繁，不缓存
        return chargePointApplicationService.findAvailableChargePoints();
    }

    /**
     * 根据充电站ID查找可用的充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 可用的充电桩列表
     */
    public List<ChargePoint> findAvailableChargePointsByStationId(StationId stationId) {
        // 可用充电桩列表变化频繁，不缓存
        return chargePointApplicationService.findAvailableChargePointsByStationId(stationId);
    }

    /**
     * 更新充电桩状态
     * 
     * @param chargePointId 充电桩ID
     * @param newStatus 新状态
     * @param timestamp 状态变更时间
     * @return 更新后的充电桩
     */
    public ChargePoint updateChargePointStatus(ChargePointId chargePointId, DeviceStatus newStatus, Instant timestamp) {
        ChargePoint chargePoint = chargePointApplicationService.updateChargePointStatus(chargePointId, newStatus, timestamp);
        
        // 更新缓存
        cacheChargePoint(chargePoint);
        
        return chargePoint;
    }

    /**
     * 更新充电桩心跳时间
     * 
     * @param chargePointId 充电桩ID
     * @param heartbeatTime 心跳时间
     * @return 更新后的充电桩
     */
    public ChargePoint updateChargePointHeartbeat(ChargePointId chargePointId, Instant heartbeatTime) {
        ChargePoint chargePoint = chargePointApplicationService.updateChargePointHeartbeat(chargePointId, heartbeatTime);
        
        // 更新缓存
        cacheChargePoint(chargePoint);
        
        return chargePoint;
    }

    /**
     * 更新充电桩固件版本
     * 
     * @param chargePointId 充电桩ID
     * @param firmwareVersion 固件版本
     * @return 更新后的充电桩
     */
    public ChargePoint updateChargePointFirmware(ChargePointId chargePointId, String firmwareVersion) {
        ChargePoint chargePoint = chargePointApplicationService.updateChargePointFirmware(chargePointId, firmwareVersion);
        
        // 更新缓存
        cacheChargePoint(chargePoint);
        
        return chargePoint;
    }

    /**
     * 检查充电桩是否可以启动充电
     * 
     * @param chargePointId 充电桩ID
     * @return true如果可以启动充电
     */
    public boolean canStartCharging(ChargePointId chargePointId) {
        // 先从缓存获取充电桩信息
        ChargePoint chargePoint = findChargePointById(chargePointId);
        
        // 使用缓存的充电桩信息进行判断
        return chargePoint.isAvailableForCharging() && chargePoint.isOnline() && chargePoint.hasAvailableConnector();
    }

    /**
     * 批量标记离线充电桩
     * 
     * @param offlineThresholdMinutes 离线阈值（分钟）
     * @return 标记为离线的充电桩数量
     */
    public int markOfflineChargePoints(int offlineThresholdMinutes) {
        int updatedCount = chargePointApplicationService.markOfflineChargePoints(offlineThresholdMinutes);
        
        // 清除所有充电桩缓存，因为状态可能已变更
        if (updatedCount > 0) {
            cacheService.clear(CacheConfig.CHARGE_POINT_CACHE);
            log.debug("充电桩缓存已清空，因为批量更新了{}个充电桩状态", updatedCount);
        }
        
        return updatedCount;
    }

    /**
     * 删除充电桩
     * 
     * @param chargePointId 充电桩ID
     */
    public void deleteChargePoint(ChargePointId chargePointId) {
        chargePointApplicationService.deleteChargePoint(chargePointId);
        
        // 清除缓存
        evictChargePointCache(chargePointId);
    }

    /**
     * 统计充电桩总数
     * 
     * @return 充电桩总数
     */
    public long countChargePoints() {
        // 统计数据不缓存，因为变化频繁
        return chargePointApplicationService.countChargePoints();
    }

    /**
     * 根据充电站ID统计充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 充电桩数量
     */
    public long countChargePointsByStationId(StationId stationId) {
        // 统计数据不缓存，因为变化频繁
        return chargePointApplicationService.countChargePointsByStationId(stationId);
    }

    /**
     * 根据充电站ID统计可用充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 可用充电桩数量
     */
    public long countAvailableChargePointsByStationId(StationId stationId) {
        // 统计数据不缓存，因为变化频繁
        return chargePointApplicationService.countAvailableChargePointsByStationId(stationId);
    }

    /**
     * 缓存充电桩
     * 
     * @param chargePoint 充电桩
     */
    private void cacheChargePoint(ChargePoint chargePoint) {
        if (chargePoint == null) {
            return;
        }
        
        // 按ID缓存
        cacheService.put(CacheConfig.CHARGE_POINT_CACHE, chargePoint.getChargePointId().value(), chargePoint);
        
        // 按序列号缓存（如果有序列号）
        if (chargePoint.getSerialNumber() != null && !chargePoint.getSerialNumber().trim().isEmpty()) {
            cacheService.put(CacheConfig.CHARGE_POINT_BY_SERIAL_CACHE, chargePoint.getSerialNumber(), chargePoint);
        }
        
        log.debug("充电桩已缓存: chargePointId={}, serialNumber={}", 
            chargePoint.getChargePointId(), chargePoint.getSerialNumber());
    }

    /**
     * 清除充电桩缓存
     * 
     * @param chargePointId 充电桩ID
     */
    private void evictChargePointCache(ChargePointId chargePointId) {
        cacheService.evict(CacheConfig.CHARGE_POINT_CACHE, chargePointId.value());
        
        // 注意：按序列号的缓存需要知道序列号才能清除，这里简化处理
        // 实际项目中可以考虑维护一个ID到序列号的映射
        
        log.debug("充电桩缓存已清除: chargePointId={}", chargePointId);
    }
}
