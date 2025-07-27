package com.charge.station.application.service;

import com.charge.station.application.command.CreateChargePointCommand;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.domain.service.ChargePointDomainService;
import com.charge.station.shared.exception.BusinessException;
import com.charge.station.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 充电桩应用服务
 * 
 * 编排充电桩相关的业务流程，调用领域服务和仓储。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChargePointApplicationService {

    private final ChargePointRepository chargePointRepository;
    private final StationRepository stationRepository;
    private final ChargePointDomainService chargePointDomainService;
    private final DomainEventPublishingService domainEventPublishingService;

    /**
     * 创建充电桩
     * 
     * @param command 创建充电桩命令
     * @return 创建的充电桩
     */
    @Transactional
    public ChargePoint createChargePoint(CreateChargePointCommand command) {
        Objects.requireNonNull(command, "创建充电桩命令不能为空");
        
        log.info("开始创建充电桩: stationId={}, name={}", command.stationId(), command.name());
        
        // 1. 验证充电站是否存在
        if (!stationRepository.existsById(command.stationId())) {
            throw new ResourceNotFoundException("充电站不存在: " + command.stationId());
        }
        
        // 2. 验证序列号唯一性（如果提供了序列号）
        if (command.serialNumber() != null && !command.serialNumber().trim().isEmpty()) {
            chargePointDomainService.validateSerialNumberUniqueness(command.serialNumber());
        }
        
        // 3. 创建功率规格
        PowerSpecification powerSpecification = PowerSpecification.ofKilowatts(command.maxPower());
        
        // 4. 创建充电桩聚合
        ChargePoint chargePoint = new ChargePoint(
            command.stationId(),
            command.name(),
            command.model(),
            command.vendor(),
            command.serialNumber(),
            powerSpecification
        );
        
        // 5. 保存充电桩
        ChargePoint savedChargePoint = chargePointRepository.save(chargePoint);

        // 6. 发布领域事件
        domainEventPublishingService.publishDomainEvents(savedChargePoint);

        log.info("充电桩创建成功: chargePointId={}, stationId={}, name={}",
            savedChargePoint.getChargePointId(), savedChargePoint.getStationId(), savedChargePoint.getName());

        return savedChargePoint;
    }

    /**
     * 根据ID查找充电桩
     * 
     * @param chargePointId 充电桩ID
     * @return 充电桩
     */
    @Transactional(readOnly = true)
    public ChargePoint findChargePointById(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        
        return chargePointRepository.findById(chargePointId)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
    }

    /**
     * 根据序列号查找充电桩
     * 
     * @param serialNumber 序列号
     * @return 充电桩
     */
    @Transactional(readOnly = true)
    public ChargePoint findChargePointBySerialNumber(String serialNumber) {
        Objects.requireNonNull(serialNumber, "序列号不能为空");
        
        return chargePointRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + serialNumber));
    }

    /**
     * 根据充电站ID查找充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 充电桩列表
     */
    @Transactional(readOnly = true)
    public List<ChargePoint> findChargePointsByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        return chargePointRepository.findByStationId(stationId);
    }

    /**
     * 查找可用的充电桩列表
     * 
     * @return 可用的充电桩列表
     */
    @Transactional(readOnly = true)
    public List<ChargePoint> findAvailableChargePoints() {
        return chargePointRepository.findAvailableChargePoints();
    }

    @Transactional(readOnly = true)
    public List<ChargePoint> findAllChargePoints(int page, int size) {
        return chargePointRepository.findAllChargePoints(page, size);
    }

    /**
     * 根据充电站ID查找可用的充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 可用的充电桩列表
     */
    @Transactional(readOnly = true)
    public List<ChargePoint> findAvailableChargePointsByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        return chargePointDomainService.findAvailableChargePointsInStation(stationId);
    }

    /**
     * 更新充电桩状态
     * 
     * @param chargePointId 充电桩ID
     * @param newStatus 新状态
     * @param timestamp 状态变更时间
     * @return 更新后的充电桩
     */
    @Transactional
    public ChargePoint updateChargePointStatus(ChargePointId chargePointId, DeviceStatus newStatus, Instant timestamp) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        Objects.requireNonNull(newStatus, "设备状态不能为空");
        Objects.requireNonNull(timestamp, "状态变更时间不能为空");
        
        log.info("开始更新充电桩状态: chargePointId={}, newStatus={}", chargePointId, newStatus);
        
        ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
        
        chargePoint.updateStatus(newStatus, timestamp);
        ChargePoint updatedChargePoint = chargePointRepository.save(chargePoint);

        // 发布领域事件
        domainEventPublishingService.publishDomainEvents(updatedChargePoint);

        log.info("充电桩状态更新成功: chargePointId={}, newStatus={}", chargePointId, newStatus);

        return updatedChargePoint;
    }

    /**
     * 更新充电桩心跳时间
     * 
     * @param chargePointId 充电桩ID
     * @param heartbeatTime 心跳时间
     * @return 更新后的充电桩
     */
    @Transactional
    public ChargePoint updateChargePointHeartbeat(ChargePointId chargePointId, Instant heartbeatTime) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        Objects.requireNonNull(heartbeatTime, "心跳时间不能为空");
        
        ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
        
        chargePoint.updateHeartbeat(heartbeatTime);
        return chargePointRepository.save(chargePoint);
    }

    /**
     * 更新充电桩固件版本
     * 
     * @param chargePointId 充电桩ID
     * @param firmwareVersion 固件版本
     * @return 更新后的充电桩
     */
    @Transactional
    public ChargePoint updateChargePointFirmware(ChargePointId chargePointId, String firmwareVersion) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        
        log.info("开始更新充电桩固件版本: chargePointId={}, firmwareVersion={}", chargePointId, firmwareVersion);
        
        ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
        
        chargePoint.updateFirmwareVersion(firmwareVersion);
        ChargePoint updatedChargePoint = chargePointRepository.save(chargePoint);
        
        log.info("充电桩固件版本更新成功: chargePointId={}, firmwareVersion={}", chargePointId, firmwareVersion);
        
        return updatedChargePoint;
    }

    /**
     * 检查充电桩是否可以启动充电
     * 
     * @param chargePointId 充电桩ID
     * @return true如果可以启动充电
     */
    @Transactional(readOnly = true)
    public boolean canStartCharging(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        
        ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
        
        return chargePointDomainService.canStartCharging(chargePoint);
    }

    /**
     * 批量标记离线充电桩
     * 
     * @param offlineThresholdMinutes 离线阈值（分钟）
     * @return 标记为离线的充电桩数量
     */
    @Transactional
    public int markOfflineChargePoints(int offlineThresholdMinutes) {
        if (offlineThresholdMinutes <= 0) {
            throw new BusinessException("离线阈值必须大于0");
        }
        
        log.info("开始批量标记离线充电桩: offlineThresholdMinutes={}", offlineThresholdMinutes);
        
        int updatedCount = chargePointDomainService.markOfflineChargePoints(offlineThresholdMinutes);
        
        log.info("批量标记离线充电桩完成: updatedCount={}", updatedCount);
        
        return updatedCount;
    }

    /**
     * 删除充电桩
     * 
     * @param chargePointId 充电桩ID
     */
    @Transactional
    public void deleteChargePoint(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        
        log.info("开始删除充电桩: chargePointId={}", chargePointId);
        
        // 检查充电桩是否存在
        if (!chargePointRepository.existsById(chargePointId)) {
            throw new ResourceNotFoundException("充电桩不存在: " + chargePointId);
        }
        
        // TODO: 检查充电桩是否正在使用中，如果是则不允许删除
        
        chargePointRepository.deleteById(chargePointId);
        
        log.info("充电桩删除成功: chargePointId={}", chargePointId);
    }

    /**
     * 统计充电桩总数
     * 
     * @return 充电桩总数
     */
    @Transactional(readOnly = true)
    public long countChargePoints() {
        return chargePointRepository.count();
    }

    /**
     * 根据充电站ID统计充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 充电桩数量
     */
    @Transactional(readOnly = true)
    public long countChargePointsByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        return chargePointRepository.countByStationId(stationId);
    }

    /**
     * 根据充电站ID统计可用充电桩数量
     * 
     * @param stationId 充电站ID
     * @return 可用充电桩数量
     */
    @Transactional(readOnly = true)
    public long countAvailableChargePointsByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        return chargePointDomainService.countAvailableChargePointsInStation(stationId);
    }
}
