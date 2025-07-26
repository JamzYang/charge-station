package com.charge.station.interfaces.assembler;

import com.charge.station.application.command.CreateStationCommand;
import com.charge.station.application.command.UpdateStationCommand;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.interfaces.dto.request.CreateStationRequest;
import com.charge.station.interfaces.dto.request.UpdateStationRequest;
import com.charge.station.interfaces.dto.response.StationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 充电站转换器
 * 
 * 负责在DTO和领域对象之间进行转换。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class StationAssembler {

    /**
     * 将创建请求转换为创建命令
     * 
     * @param request 创建请求
     * @return 创建命令
     */
    public CreateStationCommand toCreateCommand(CreateStationRequest request) {
        Objects.requireNonNull(request, "创建请求不能为空");
        
        return CreateStationCommand.of(
            request.name(),
            request.address(),
            request.description(),
            request.latitude(),
            request.longitude(),
            request.operatorId(),
            request.openTime(),
            request.closeTime()
        );
    }

    /**
     * 将更新请求转换为更新命令
     * 
     * @param stationId 充电站ID
     * @param request 更新请求
     * @return 更新命令
     */
    public UpdateStationCommand toUpdateCommand(StationId stationId, UpdateStationRequest request) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        Objects.requireNonNull(request, "更新请求不能为空");
        
        return UpdateStationCommand.of(
            stationId,
            request.name(),
            request.address(),
            request.description(),
            request.latitude(),
            request.longitude(),
            request.openTime(),
            request.closeTime()
        );
    }

    /**
     * 将充电站领域对象转换为响应DTO
     * 
     * @param station 充电站领域对象
     * @return 响应DTO
     */
    public StationResponse toResponse(Station station) {
        Objects.requireNonNull(station, "充电站不能为空");
        
        return StationResponse.of(
            station.getStationId().value(),
            station.getStationInfo().name(),
            station.getStationInfo().address(),
            station.getStationInfo().description(),
            station.getLocation().latitude(),
            station.getLocation().longitude(),
            station.getOperatorId(),
            station.getStatus().getCode(),
            station.getStatus().getDescription(),
            station.getBusinessHours().openTime(),
            station.getBusinessHours().closeTime(),
            station.getBusinessHours().getFormattedHours(),
            station.canProvideService(),
            station.getCreatedAt(),
            station.getUpdatedAt()
        );
    }

    /**
     * 将充电站领域对象转换为简化响应DTO
     * 
     * @param station 充电站领域对象
     * @return 简化响应DTO
     */
    public StationResponse toSimpleResponse(Station station) {
        Objects.requireNonNull(station, "充电站不能为空");
        
        return StationResponse.simple(
            station.getStationId().value(),
            station.getStationInfo().name(),
            station.getStationInfo().address(),
            station.getLocation().latitude(),
            station.getLocation().longitude(),
            station.getStatus().getCode(),
            station.getStatus().getDescription(),
            station.canProvideService()
        );
    }

    /**
     * 批量转换充电站列表为响应DTO列表
     * 
     * @param stations 充电站列表
     * @return 响应DTO列表
     */
    public List<StationResponse> toResponseList(List<Station> stations) {
        Objects.requireNonNull(stations, "充电站列表不能为空");
        
        return stations.stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * 批量转换充电站列表为简化响应DTO列表
     * 
     * @param stations 充电站列表
     * @return 简化响应DTO列表
     */
    public List<StationResponse> toSimpleResponseList(List<Station> stations) {
        Objects.requireNonNull(stations, "充电站列表不能为空");
        
        return stations.stream()
            .map(this::toSimpleResponse)
            .toList();
    }
}
