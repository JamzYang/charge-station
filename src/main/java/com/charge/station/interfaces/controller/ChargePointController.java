package com.charge.station.interfaces.controller;

import com.charge.station.application.command.CreateChargePointCommand;
import com.charge.station.application.service.cached.ChargePointCachedApplicationService;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.interfaces.assembler.ChargePointAssembler;
import com.charge.station.interfaces.dto.request.CreateChargePointRequest;
import com.charge.station.interfaces.dto.response.ChargePointResponse;
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

import java.time.Instant;
import java.util.List;

/**
 * 充电桩控制器
 * 
 * 提供充电桩相关的REST API接口。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "充电桩管理", description = "充电桩的创建、查询、更新和删除操作")
public class ChargePointController {

    private final ChargePointCachedApplicationService chargePointApplicationService;
    private final ChargePointAssembler chargePointAssembler;

    /**
     * 在指定充电站创建充电桩
     * 
     * @param stationId 充电站ID
     * @param request 创建请求
     * @return 创建的充电桩信息
     */
    @PostMapping("/stations/{stationId}/charge-points")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建充电桩", description = "在指定充电站创建新的充电桩")
    public ApiResponse<ChargePointResponse> createChargePoint(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId,
        @Valid @RequestBody CreateChargePointRequest request) {
        
        log.info("收到创建充电桩请求: stationId={}, name={}", stationId, request.name());
        
        StationId stationIdObj = StationId.of(stationId);
        CreateChargePointCommand command = chargePointAssembler.toCreateCommand(stationIdObj, request);
        ChargePoint chargePoint = chargePointApplicationService.createChargePoint(command);
        ChargePointResponse response = chargePointAssembler.toResponse(chargePoint);
        
        log.info("充电桩创建成功: chargePointId={}", response.chargePointId());
        
        return ApiResponse.success(response, "充电桩创建成功");
    }

    /**
     * 根据ID查询充电桩
     * 
     * @param chargePointId 充电桩ID
     * @return 充电桩信息
     */
    @GetMapping("/charge-points/{chargePointId}")
    @Operation(summary = "查询充电桩", description = "根据ID查询充电桩详细信息")
    public ApiResponse<ChargePointResponse> getChargePoint(
        @Parameter(description = "充电桩ID", required = true)
        @PathVariable String chargePointId) {
        
        log.info("收到查询充电桩请求: chargePointId={}", chargePointId);
        
        ChargePointId id = ChargePointId.of(chargePointId);
        ChargePoint chargePoint = chargePointApplicationService.findChargePointById(id);
        ChargePointResponse response = chargePointAssembler.toResponse(chargePoint);
        
        return ApiResponse.success(response);
    }

    /**
     * 根据充电站ID查询充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 充电桩列表
     */
    @GetMapping("/stations/{stationId}/charge-points")
    @Operation(summary = "查询充电站下的充电桩", description = "查询指定充电站下的所有充电桩")
    public ApiResponse<List<ChargePointResponse>> getChargePointsByStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到查询充电站充电桩列表请求: stationId={}", stationId);
        
        StationId stationIdObj = StationId.of(stationId);
        List<ChargePoint> chargePoints = chargePointApplicationService.findChargePointsByStationId(stationIdObj);
        List<ChargePointResponse> responses = chargePointAssembler.toResponseList(chargePoints);
        
        return ApiResponse.success(responses);
    }

    /**
     * 查询所有充电桩列表
     *
     * @return 充电桩列表
     */
    @GetMapping("/charge-points")
    @Operation(summary = "查询充电桩列表", description = "查询所有可用的充电桩")
    public ApiResponse<PageResponse<ChargePointResponse>> getAllChargePoints(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        log.info("收到查询充电桩列表请求");

        List<ChargePoint> chargePoints = chargePointApplicationService.findAllChargePoints(page, size);
        List<ChargePointResponse> responses = chargePointAssembler.toResponseList(chargePoints);
        long total = chargePointApplicationService.countChargePoints();
        return ApiResponse.success(PageResponse.of(responses, page, size, total));
    }


    /**
     * 查询可用的充电桩列表
     * 
     * @return 可用的充电桩列表
     */
    @GetMapping("/charge-points/available")
    @Operation(summary = "查询可用充电桩", description = "查询所有可用的充电桩")
    public ApiResponse<List<ChargePointResponse>> getAvailableChargePoints() {
        
        log.info("收到查询可用充电桩列表请求");
        
        List<ChargePoint> chargePoints = chargePointApplicationService.findAvailableChargePoints();
        List<ChargePointResponse> responses = chargePointAssembler.toSimpleResponseList(chargePoints);
        
        return ApiResponse.success(responses);
    }

    /**
     * 根据充电站ID查询可用的充电桩列表
     * 
     * @param stationId 充电站ID
     * @return 可用的充电桩列表
     */
    @GetMapping("/stations/{stationId}/charge-points/available")
    @Operation(summary = "查询充电站可用充电桩", description = "查询指定充电站下可用的充电桩")
    public ApiResponse<List<ChargePointResponse>> getAvailableChargePointsByStation(
        @Parameter(description = "充电站ID", required = true)
        @PathVariable String stationId) {
        
        log.info("收到查询充电站可用充电桩列表请求: stationId={}", stationId);
        
        StationId stationIdObj = StationId.of(stationId);
        List<ChargePoint> chargePoints = chargePointApplicationService.findAvailableChargePointsByStationId(stationIdObj);
        List<ChargePointResponse> responses = chargePointAssembler.toSimpleResponseList(chargePoints);
        
        return ApiResponse.success(responses);
    }

    /**
     * 更新充电桩状态
     * 
     * @param chargePointId 充电桩ID
     * @param status 新状态
     * @return 更新后的充电桩信息
     */
    @PutMapping("/charge-points/{chargePointId}/status")
    @Operation(summary = "更新充电桩状态", description = "更新充电桩的运行状态")
    public ApiResponse<ChargePointResponse> updateChargePointStatus(
        @Parameter(description = "充电桩ID", required = true)
        @PathVariable String chargePointId,
        @Parameter(description = "新状态", required = true)
        @RequestParam String status) {
        
        log.info("收到更新充电桩状态请求: chargePointId={}, status={}", chargePointId, status);
        
        ChargePointId id = ChargePointId.of(chargePointId);
        DeviceStatus deviceStatus = DeviceStatus.fromOcppStatus(status);
        ChargePoint chargePoint = chargePointApplicationService.updateChargePointStatus(id, deviceStatus, Instant.now());
        ChargePointResponse response = chargePointAssembler.toResponse(chargePoint);
        
        log.info("充电桩状态更新成功: chargePointId={}, status={}", chargePointId, status);
        
        return ApiResponse.success(response, "充电桩状态更新成功");
    }

    /**
     * 更新充电桩心跳
     * 
     * @param chargePointId 充电桩ID
     * @return 更新后的充电桩信息
     */
    @PostMapping("/charge-points/{chargePointId}/heartbeat")
    @Operation(summary = "更新充电桩心跳", description = "更新充电桩的心跳时间")
    public ApiResponse<ChargePointResponse> updateChargePointHeartbeat(
        @Parameter(description = "充电桩ID", required = true)
        @PathVariable String chargePointId) {
        
        log.debug("收到更新充电桩心跳请求: chargePointId={}", chargePointId);
        
        ChargePointId id = ChargePointId.of(chargePointId);
        ChargePoint chargePoint = chargePointApplicationService.updateChargePointHeartbeat(id, Instant.now());
        ChargePointResponse response = chargePointAssembler.toSimpleResponse(chargePoint);
        
        return ApiResponse.success(response);
    }

    /**
     * 删除充电桩
     * 
     * @param chargePointId 充电桩ID
     * @return 删除结果
     */
    @DeleteMapping("/charge-points/{chargePointId}")
    @Operation(summary = "删除充电桩", description = "删除指定的充电桩")
    public ApiResponse<Void> deleteChargePoint(
        @Parameter(description = "充电桩ID", required = true)
        @PathVariable String chargePointId) {
        
        log.info("收到删除充电桩请求: chargePointId={}", chargePointId);
        
        ChargePointId id = ChargePointId.of(chargePointId);
        chargePointApplicationService.deleteChargePoint(id);
        
        log.info("充电桩删除成功: chargePointId={}", chargePointId);
        
        return ApiResponse.success(null, "充电桩删除成功");
    }

    /**
     * 检查充电桩是否可以启动充电
     * 
     * @param chargePointId 充电桩ID
     * @return 检查结果
     */
    @GetMapping("/charge-points/{chargePointId}/can-start-charging")
    @Operation(summary = "检查是否可以启动充电", description = "检查充电桩是否可以启动充电")
    public ApiResponse<Boolean> canStartCharging(
        @Parameter(description = "充电桩ID", required = true)
        @PathVariable String chargePointId) {
        
        log.info("收到检查充电桩是否可以启动充电请求: chargePointId={}", chargePointId);
        
        ChargePointId id = ChargePointId.of(chargePointId);
        boolean canStart = chargePointApplicationService.canStartCharging(id);
        
        return ApiResponse.success(canStart);
    }

    /**
     * 批量标记离线充电桩
     * 
     * @param offlineThresholdMinutes 离线阈值（分钟）
     * @return 标记为离线的充电桩数量
     */
    @PostMapping("/charge-points/mark-offline")
    @Operation(summary = "批量标记离线充电桩", description = "批量标记超过指定时间没有心跳的充电桩为离线状态")
    public ApiResponse<Integer> markOfflineChargePoints(
        @Parameter(description = "离线阈值（分钟）")
        @RequestParam(defaultValue = "5") int offlineThresholdMinutes) {
        
        log.info("收到批量标记离线充电桩请求: offlineThresholdMinutes={}", offlineThresholdMinutes);
        
        int updatedCount = chargePointApplicationService.markOfflineChargePoints(offlineThresholdMinutes);
        
        log.info("批量标记离线充电桩完成: updatedCount={}", updatedCount);
        
        return ApiResponse.success(updatedCount, "批量标记离线充电桩完成");
    }
}
