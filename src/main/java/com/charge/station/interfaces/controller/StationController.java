package com.charge.station.interfaces.controller;

import com.charge.station.application.command.CreateStationCommand;
import com.charge.station.application.command.UpdateStationCommand;
import com.charge.station.application.service.cached.StationCachedApplicationService;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.interfaces.assembler.StationAssembler;
import com.charge.station.interfaces.dto.request.CreateStationRequest;
import com.charge.station.interfaces.dto.request.UpdateStationRequest;
import com.charge.station.interfaces.dto.response.StationResponse;
import com.charge.station.shared.response.ApiResponse;
import com.charge.station.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 充电站控制器
 * 
 * 提供充电站相关的REST API接口。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "充电站管理", description = "充电站的创建、查询、更新和删除操作")
public class StationController {

    private final StationCachedApplicationService stationApplicationService;
    private final StationAssembler stationAssembler;

    /**
     * 创建充电站
     * 
     * @param request 创建请求
     * @return 创建的充电站信息
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建充电站", description = "创建新的充电站")
    public ApiResponse<StationResponse> createStation(@Valid @RequestBody CreateStationRequest request) {
        log.info("收到创建充电站请求: {}", request.name());
        
        CreateStationCommand command = stationAssembler.toCreateCommand(request);
        Station station = stationApplicationService.createStation(command);
        StationResponse response = stationAssembler.toResponse(station);
        
        log.info("充电站创建成功: stationId={}", response.stationId());
        
        return ApiResponse.success(response, "充电站创建成功");
    }

    /**
     * 根据ID查询充电站
     * 
     * @param stationId 充电站ID
     * @return 充电站信息
     */
    @GetMapping("/{stationId}")
    @Operation(summary = "查询充电站", description = "根据ID查询充电站详细信息")
    public ApiResponse<StationResponse> getStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到查询充电站请求: stationId={}", stationId);
        
        StationId id = StationId.of(stationId);
        Station station = stationApplicationService.findStationById(id);
        StationResponse response = stationAssembler.toResponse(station);
        
        return ApiResponse.success(response);
    }

    /**
     * 更新充电站
     * 
     * @param stationId 充电站ID
     * @param request 更新请求
     * @return 更新后的充电站信息
     */
    @PutMapping("/{stationId}")
    @Operation(summary = "更新充电站", description = "更新充电站信息")
    public ApiResponse<StationResponse> updateStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId,
        @Valid @RequestBody UpdateStationRequest request) {
        
        log.info("收到更新充电站请求: stationId={}", stationId);
        
        StationId id = StationId.of(stationId);
        UpdateStationCommand command = stationAssembler.toUpdateCommand(id, request);
        Station station = stationApplicationService.updateStation(command);
        StationResponse response = stationAssembler.toResponse(station);
        
        log.info("充电站更新成功: stationId={}", stationId);
        
        return ApiResponse.success(response, "充电站更新成功");
    }

    /**
     * 删除充电站
     * 
     * @param stationId 充电站ID
     * @return 删除结果
     */
    @DeleteMapping("/{stationId}")
    @Operation(summary = "删除充电站", description = "删除指定的充电站")
    public ApiResponse<Void> deleteStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到删除充电站请求: stationId={}", stationId);
        
        StationId id = StationId.of(stationId);
        stationApplicationService.deleteStation(id);
        
        log.info("充电站删除成功: stationId={}", stationId);
        
        return ApiResponse.success(null, "充电站删除成功");
    }

    /**
     * 分页查询充电站列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 充电站列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询充电站列表", description = "分页查询充电站列表")
    public ApiResponse<PageResponse<StationResponse>> getStations(
        @Parameter(description = "页码（从0开始）")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小")
        @RequestParam(defaultValue = "20") int size) {
        
        log.info("收到查询充电站列表请求: page={}, size={}", page, size);
        
        List<Station> stations = stationApplicationService.findStations(page, size);
        List<StationResponse> responses = stationAssembler.toResponseList(stations);
        long total = stationApplicationService.countStations();
        
        PageResponse<StationResponse> pageResponse = PageResponse.of(responses, page, size, total);
        
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查询附近的充电站
     * 
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 搜索半径（米）
     * @return 附近的充电站列表
     */
    @GetMapping("/nearby")
    @Operation(summary = "查询附近充电站", description = "根据位置查询附近的充电站")
    public ApiResponse<List<StationResponse>> getNearbyStations(
        @Parameter(description = "纬度", required = true)
        @RequestParam double latitude,
        @Parameter(description = "经度", required = true)
        @RequestParam double longitude,
        @Parameter(description = "搜索半径（米）")
        @RequestParam(defaultValue = "5000") double radius) {
        
        log.info("收到查询附近充电站请求: latitude={}, longitude={}, radius={}", latitude, longitude, radius);
        
        List<Station> stations = stationApplicationService.findNearbyStations(latitude, longitude, radius);
        List<StationResponse> responses = stationAssembler.toSimpleResponseList(stations);
        
        return ApiResponse.success(responses);
    }

    /**
     * 激活充电站
     * 
     * @param stationId 充电站ID
     * @return 激活后的充电站信息
     */
    @PostMapping("/{stationId}/activate")
    @Operation(summary = "激活充电站", description = "激活指定的充电站")
    public ApiResponse<StationResponse> activateStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到激活充电站请求: stationId={}", stationId);
        
        StationId id = StationId.of(stationId);
        Station station = stationApplicationService.activateStation(id);
        StationResponse response = stationAssembler.toResponse(station);
        
        log.info("充电站激活成功: stationId={}", stationId);
        
        return ApiResponse.success(response, "充电站激活成功");
    }

    /**
     * 停用充电站
     * 
     * @param stationId 充电站ID
     * @return 停用后的充电站信息
     */
    @PostMapping("/{stationId}/deactivate")
    @Operation(summary = "停用充电站", description = "停用指定的充电站")
    public ApiResponse<StationResponse> deactivateStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到停用充电站请求: stationId={}", stationId);
        
        StationId id = StationId.of(stationId);
        Station station = stationApplicationService.deactivateStation(id);
        StationResponse response = stationAssembler.toResponse(station);
        
        log.info("充电站停用成功: stationId={}", stationId);
        
        return ApiResponse.success(response, "充电站停用成功");
    }

    /**
     * 设置充电站为维护状态
     * 
     * @param stationId 充电站ID
     * @return 设置维护状态后的充电站信息
     */
    @PostMapping("/{stationId}/maintenance")
    @Operation(summary = "设置维护状态", description = "设置充电站为维护状态")
    public ApiResponse<StationResponse> setStationMaintenance(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到设置充电站维护状态请求: stationId={}", stationId);
        
        StationId id = StationId.of(stationId);
        Station station = stationApplicationService.setStationMaintenance(id);
        StationResponse response = stationAssembler.toResponse(station);
        
        log.info("充电站维护状态设置成功: stationId={}", stationId);
        
        return ApiResponse.success(response, "充电站维护状态设置成功");
    }
}
