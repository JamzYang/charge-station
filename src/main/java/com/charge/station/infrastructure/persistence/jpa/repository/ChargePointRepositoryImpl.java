package com.charge.station.infrastructure.persistence.jpa.repository;

import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.infrastructure.persistence.jpa.converter.ChargePointEntityConverter;
import com.charge.station.infrastructure.persistence.jpa.entity.ChargePointEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 充电桩仓储JPA适配器
 *
 * 实现充电桩仓储接口，提供基于JPA的数据持久化操作。
 * 
 * 注意：类名避免使用 "JpaRepositoryImpl" 后缀，以免与 Spring Data JPA 的命名约定冲突
 *
 * @author 架构师团队
 * @version 1.0
 */
@Repository("chargePointRepositoryImpl")
@Slf4j
public class ChargePointRepositoryImpl implements ChargePointRepository {

    private final ChargePointJpaRepository jpaRepository;
    private final ChargePointEntityConverter converter;

    public ChargePointRepositoryImpl(@Lazy ChargePointJpaRepository jpaRepository,
                                          ChargePointEntityConverter converter) {
        this.jpaRepository = jpaRepository;
        this.converter = converter;
    }
    
    @Override
    public ChargePoint save(ChargePoint chargePoint) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        log.debug("保存充电桩: {}", chargePoint.getChargePointId().value());
        
        ChargePointEntity entity = converter.toEntity(chargePoint);
        ChargePointEntity savedEntity = jpaRepository.save(entity);
        
        log.debug("充电桩保存成功: {}", savedEntity.getId());
        
        return converter.toDomain(savedEntity);
    }
    
    @Override
    public Optional<ChargePoint> findById(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");

        log.debug("根据ID查找充电桩: {}", chargePointId.value());

        return jpaRepository.findByIdWithConnectors(chargePointId.value())
            .map(converter::toDomain);
    }
    
    @Override
    public Optional<ChargePoint> findBySerialNumber(String serialNumber) {
        Objects.requireNonNull(serialNumber, "序列号不能为空");
        
        log.debug("根据序列号查找充电桩: {}", serialNumber);
        
        return jpaRepository.findBySerialNumber(serialNumber)
            .map(converter::toDomain);
    }
    
    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        Objects.requireNonNull(serialNumber, "序列号不能为空");
        
        log.debug("检查序列号是否存在: {}", serialNumber);
        
        return jpaRepository.existsBySerialNumber(serialNumber);
    }
    
    @Override
    public boolean existsBySerialNumberAndIdNot(String serialNumber, ChargePointId excludeChargePointId) {
        Objects.requireNonNull(serialNumber, "序列号不能为空");
        Objects.requireNonNull(excludeChargePointId, "排除的充电桩ID不能为空");
        
        log.debug("检查序列号是否存在（排除ID: {}）: {}", excludeChargePointId.value(), serialNumber);
        
        return jpaRepository.existsBySerialNumberAndIdNot(serialNumber, excludeChargePointId.value());
    }
    
    @Override
    public List<ChargePoint> findByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.debug("根据充电站ID查找充电桩: {}", stationId.value());
        
        return jpaRepository.findByStationId(stationId.value())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChargePoint> findByStatus(DeviceStatus status) {
        Objects.requireNonNull(status, "充电桩状态不能为空");
        
        log.debug("根据状态查找充电桩: {}", status.getOcppStatus());
        
        return jpaRepository.findByStatus(status.getOcppStatus())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChargePoint> findByStationIdAndStatus(StationId stationId, DeviceStatus status) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        Objects.requireNonNull(status, "充电桩状态不能为空");
        
        log.debug("根据充电站ID和状态查找充电桩: stationId={}, status={}", 
            stationId.value(), status.getOcppStatus());
        
        return jpaRepository.findByStationIdAndStatus(stationId.value(), status.getOcppStatus())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChargePoint> findAvailableChargePoints() {
        log.debug("查找可用的充电桩");

        return jpaRepository.findByStatusOrderByCreatedAtDesc(DeviceStatus.AVAILABLE.getOcppStatus())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChargePoint> findAllChargePoints(int page, int size) {
        log.debug("查找充电桩列表");
        PageRequest pageRequest = PageRequest.of(page, size);
        return jpaRepository.findAllByOrderByCreatedAtDesc(pageRequest)
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChargePoint> findAvailableChargePointsByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");

        log.debug("根据充电站ID查找可用的充电桩: {}", stationId.value());

        return jpaRepository.findByStationIdAndStatusOrderByCreatedAtDesc(stationId.value(), DeviceStatus.AVAILABLE.getOcppStatus())
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ChargePoint> findOfflineChargePoints(Instant lastHeartbeatBefore) {
        Objects.requireNonNull(lastHeartbeatBefore, "最后心跳时间不能为空");

        log.debug("查找离线的充电桩: 最后心跳时间早于{}", lastHeartbeatBefore);

        return jpaRepository.findOfflineChargePoints(lastHeartbeatBefore)
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChargePoint> findAll(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("每页大小必须大于0");
        }

        log.debug("分页查询充电桩: 页码{}, 每页{}条", page, size);

        PageRequest pageRequest = PageRequest.of(page, size);

        return jpaRepository.findAllByOrderByCreatedAtDesc(pageRequest)
            .stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long count() {
        log.debug("统计充电桩总数");

        return jpaRepository.count();
    }

    @Override
    public long countByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");

        log.debug("根据充电站ID统计充电桩数量: {}", stationId.value());

        return jpaRepository.countByStationId(stationId.value());
    }

    @Override
    public long countByStatus(DeviceStatus status) {
        Objects.requireNonNull(status, "充电桩状态不能为空");

        log.debug("根据状态统计充电桩数量: {}", status.getOcppStatus());

        return jpaRepository.countByStatus(status.getOcppStatus());
    }

    @Override
    public long countByStationIdAndStatus(StationId stationId, DeviceStatus status) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        Objects.requireNonNull(status, "充电桩状态不能为空");

        log.debug("根据充电站ID和状态统计充电桩数量: stationId={}, status={}",
            stationId.value(), status.getOcppStatus());

        return jpaRepository.countByStationIdAndStatus(stationId.value(), status.getOcppStatus());
    }

    @Override
    public void deleteById(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");

        log.debug("删除充电桩: {}", chargePointId.value());

        jpaRepository.deleteById(chargePointId.value());

        log.debug("充电桩删除成功: {}", chargePointId.value());
    }

    @Override
    public void deleteByStationId(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");

        log.debug("根据充电站ID删除所有充电桩: {}", stationId.value());

        jpaRepository.deleteByStationId(stationId.value());

        log.debug("充电站下所有充电桩删除成功: {}", stationId.value());
    }

    @Override
    public boolean existsById(ChargePointId chargePointId) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");

        log.debug("检查充电桩是否存在: {}", chargePointId.value());

        return jpaRepository.existsById(chargePointId.value());
    }
}
