package com.charge.station.application.service;

import com.charge.station.application.command.CreateStationCommand;
import com.charge.station.application.command.UpdateStationCommand;
import com.charge.station.domain.model.station.*;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.domain.service.StationDomainService;
import com.charge.station.shared.exception.BusinessException;
import com.charge.station.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 充电站应用服务
 * 
 * 编排充电站相关的业务流程，调用领域服务和仓储。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StationApplicationService {

    private final StationRepository stationRepository;
    private final StationDomainService stationDomainService;

    /**
     * 创建充电站
     * 
     * @param command 创建充电站命令
     * @return 创建的充电站
     */
    @Transactional
    public Station createStation(CreateStationCommand command) {
        Objects.requireNonNull(command, "创建充电站命令不能为空");
        
        log.info("开始创建充电站: {}", command.name());
        
        // 1. 验证充电站名称唯一性
        stationDomainService.validateStationNameUniqueness(command.name());
        
        // 2. 创建值对象
        StationInfo stationInfo = StationInfo.of(command.name(), command.address(), command.description());
        Location location = new Location(command.latitude(), command.longitude());
        BusinessHours businessHours = new BusinessHours(command.openTime(), command.closeTime());
        
        // 3. 验证位置合理性（与现有充电站最小距离100米）
        stationDomainService.validateStationLocation(location, 100.0);
        
        // 4. 创建充电站聚合
        Station station = new Station(stationInfo, location, command.operatorId(), businessHours);
        
        // 5. 保存充电站
        Station savedStation = stationRepository.save(station);
        
        log.info("充电站创建成功: stationId={}, name={}", savedStation.getStationId(), savedStation.getStationInfo().name());
        
        return savedStation;
    }

    /**
     * 更新充电站
     * 
     * @param command 更新充电站命令
     * @return 更新后的充电站
     */
    @Transactional
    public Station updateStation(UpdateStationCommand command) {
        Objects.requireNonNull(command, "更新充电站命令不能为空");
        
        log.info("开始更新充电站: stationId={}", command.stationId());
        
        // 1. 查找充电站
        Station station = stationRepository.findById(command.stationId())
            .orElseThrow(() -> new ResourceNotFoundException("充电站不存在: " + command.stationId()));
        
        // 2. 验证充电站名称唯一性（排除当前充电站）
        stationDomainService.validateStationNameUniqueness(command.name(), command.stationId());
        
        // 3. 更新充电站信息
        StationInfo newStationInfo = StationInfo.of(command.name(), command.address(), command.description());
        station.updateStationInfo(newStationInfo);
        
        // 4. 更新地理位置
        Location newLocation = new Location(command.latitude(), command.longitude());
        // 验证新位置的合理性（排除当前充电站）
        stationDomainService.validateStationLocation(newLocation, 100.0, command.stationId());
        station.updateLocation(newLocation);
        
        // 5. 更新营业时间
        BusinessHours newBusinessHours = new BusinessHours(command.openTime(), command.closeTime());
        station.updateBusinessHours(newBusinessHours);
        
        // 6. 保存更新
        Station updatedStation = stationRepository.save(station);
        
        log.info("充电站更新成功: stationId={}, name={}", updatedStation.getStationId(), updatedStation.getStationInfo().name());
        
        return updatedStation;
    }

    /**
     * 根据ID查找充电站
     * 
     * @param stationId 充电站ID
     * @return 充电站
     */
    @Transactional(readOnly = true)
    public Station findStationById(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("充电站不存在: " + stationId));
    }

    /**
     * 根据名称查找充电站
     * 
     * @param name 充电站名称
     * @return 充电站
     */
    @Transactional(readOnly = true)
    public Station findStationByName(String name) {
        Objects.requireNonNull(name, "充电站名称不能为空");
        
        return stationRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("充电站不存在: " + name));
    }

    /**
     * 查找附近的充电站
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param radiusMeters 搜索半径（米）
     * @return 附近的充电站列表
     */
    @Transactional(readOnly = true)
    public List<Station> findNearbyStations(double latitude, double longitude, double radiusMeters) {
        Location centerLocation = Location.of(latitude, longitude);
        return stationDomainService.findNearbyActiveStations(centerLocation, radiusMeters);
    }

    /**
     * 激活充电站
     * 
     * @param stationId 充电站ID
     * @return 激活后的充电站
     */
    @Transactional
    public Station activateStation(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.info("开始激活充电站: stationId={}", stationId);
        
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("充电站不存在: " + stationId));
        
        station.activate();
        Station activatedStation = stationRepository.save(station);
        
        log.info("充电站激活成功: stationId={}", stationId);
        
        return activatedStation;
    }

    /**
     * 停用充电站
     * 
     * @param stationId 充电站ID
     * @return 停用后的充电站
     */
    @Transactional
    public Station deactivateStation(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.info("开始停用充电站: stationId={}", stationId);
        
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("充电站不存在: " + stationId));
        
        station.deactivate();
        Station deactivatedStation = stationRepository.save(station);
        
        log.info("充电站停用成功: stationId={}", stationId);
        
        return deactivatedStation;
    }

    /**
     * 设置充电站为维护状态
     * 
     * @param stationId 充电站ID
     * @return 设置维护状态后的充电站
     */
    @Transactional
    public Station setStationMaintenance(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.info("开始设置充电站维护状态: stationId={}", stationId);
        
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("充电站不存在: " + stationId));
        
        station.setMaintenance();
        Station maintenanceStation = stationRepository.save(station);
        
        log.info("充电站维护状态设置成功: stationId={}", stationId);
        
        return maintenanceStation;
    }

    /**
     * 删除充电站
     * 
     * @param stationId 充电站ID
     */
    @Transactional
    public void deleteStation(StationId stationId) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        
        log.info("开始删除充电站: stationId={}", stationId);
        
        // 检查充电站是否存在
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("充电站不存在: " + stationId);
        }
        
        // TODO: 检查是否有关联的充电桩，如果有则不允许删除
        
        stationRepository.deleteById(stationId);
        
        log.info("充电站删除成功: stationId={}", stationId);
    }

    /**
     * 分页查询充电站
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 充电站列表
     */
    @Transactional(readOnly = true)
    public List<Station> findStations(int page, int size) {
        if (page < 0) {
            throw new BusinessException("页码不能小于0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("每页大小必须在1-100之间");
        }
        
        return stationRepository.findAll(page, size);
    }

    /**
     * 统计充电站总数
     * 
     * @return 充电站总数
     */
    @Transactional(readOnly = true)
    public long countStations() {
        return stationRepository.count();
    }
}
