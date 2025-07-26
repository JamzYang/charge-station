package com.charge.station.interfaces.controller;

import com.charge.station.application.command.StartChargingCommand;
import com.charge.station.application.command.StopChargingCommand;
import com.charge.station.application.service.DeviceControlApplicationService;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.interfaces.dto.request.StartChargingRequest;
import com.charge.station.interfaces.dto.request.StopChargingRequest;
import com.charge.station.interfaces.dto.response.CommandAcceptedResponse;
import com.charge.station.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 设备控制控制器
 * 
 * 提供充电桩设备控制相关的REST API接口，包括启动充电、停止充电等操作。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "设备控制", description = "充电桩设备控制操作，包括启动充电、停止充电、重置等")
public class DeviceControlController {

    private final DeviceControlApplicationService deviceControlApplicationService;

    /**
     * 启动充电
     * 
     * @param chargePointId 充电桩ID
     * @param request 启动充电请求
     * @return 指令接受响应
     */
    @PostMapping("/charge-points/{chargePointId}/commands/start-charging")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "启动充电",
        description = "向指定充电桩发送启动充电指令，操作是异步的，立即返回指令接受状态"
    )
    public ApiResponse<CommandAcceptedResponse> startCharging(
            @Parameter(description = "充电桩ID", required = true)
            @PathVariable String chargePointId,
            @Parameter(description = "启动充电请求", required = true)
            @Valid @RequestBody StartChargingRequest request) {
        
        log.info("接收到启动充电请求: chargePointId={}, connectorId={}, idTag={}", 
            chargePointId, request.connectorId(), request.idTag());
        
        StartChargingCommand command = StartChargingCommand.of(
            ChargePointId.of(chargePointId),
            request.connectorId(),
            request.idTag()
        );
        
        CommandAcceptedResponse response = deviceControlApplicationService.startCharging(command);
        
        log.info("启动充电指令已接受: chargePointId={}, commandId={}", 
            chargePointId, response.commandId());
        
        return ApiResponse.success(response);
    }

    /**
     * 停止充电
     * 
     * @param chargePointId 充电桩ID
     * @param request 停止充电请求
     * @return 指令接受响应
     */
    @PostMapping("/charge-points/{chargePointId}/commands/stop-charging")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "停止充电",
        description = "向指定充电桩发送停止充电指令，操作是异步的，立即返回指令接受状态"
    )
    public ApiResponse<CommandAcceptedResponse> stopCharging(
            @Parameter(description = "充电桩ID", required = true)
            @PathVariable String chargePointId,
            @Parameter(description = "停止充电请求", required = true)
            @Valid @RequestBody StopChargingRequest request) {
        
        log.info("接收到停止充电请求: chargePointId={}, transactionId={}", 
            chargePointId, request.transactionId());
        
        StopChargingCommand command = StopChargingCommand.of(
            ChargePointId.of(chargePointId),
            request.transactionId()
        );
        
        CommandAcceptedResponse response = deviceControlApplicationService.stopCharging(command);
        
        log.info("停止充电指令已接受: chargePointId={}, commandId={}", 
            chargePointId, response.commandId());
        
        return ApiResponse.success(response);
    }

    /**
     * 重置充电桩（软重置）
     * 
     * @param chargePointId 充电桩ID
     * @return 指令接受响应
     */
    @PostMapping("/charge-points/{chargePointId}/commands/soft-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "软重置充电桩",
        description = "向指定充电桩发送软重置指令，操作是异步的，立即返回指令接受状态"
    )
    public ApiResponse<CommandAcceptedResponse> softReset(
            @Parameter(description = "充电桩ID", required = true)
            @PathVariable String chargePointId) {
        
        log.info("接收到软重置请求: chargePointId={}", chargePointId);
        
        CommandAcceptedResponse response = deviceControlApplicationService.resetChargePoint(
            ChargePointId.of(chargePointId), "Soft"
        );
        
        log.info("软重置指令已接受: chargePointId={}, commandId={}", 
            chargePointId, response.commandId());
        
        return ApiResponse.success(response);
    }

    /**
     * 重置充电桩（硬重置）
     * 
     * @param chargePointId 充电桩ID
     * @return 指令接受响应
     */
    @PostMapping("/charge-points/{chargePointId}/commands/hard-reset")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "硬重置充电桩",
        description = "向指定充电桩发送硬重置指令，操作是异步的，立即返回指令接受状态"
    )
    public ApiResponse<CommandAcceptedResponse> hardReset(
            @Parameter(description = "充电桩ID", required = true)
            @PathVariable String chargePointId) {
        
        log.info("接收到硬重置请求: chargePointId={}", chargePointId);
        
        CommandAcceptedResponse response = deviceControlApplicationService.resetChargePoint(
            ChargePointId.of(chargePointId), "Hard"
        );
        
        log.info("硬重置指令已接受: chargePointId={}, commandId={}", 
            chargePointId, response.commandId());
        
        return ApiResponse.success(response);
    }
}
