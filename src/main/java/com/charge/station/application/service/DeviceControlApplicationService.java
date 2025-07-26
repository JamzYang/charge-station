package com.charge.station.application.service;

import com.charge.station.application.command.StartChargingCommand;
import com.charge.station.application.command.StopChargingCommand;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.infrastructure.external.gateway.GatewayCommandRouter;
import com.charge.station.infrastructure.messaging.dto.RemoteStartTransactionInstruction;
import com.charge.station.infrastructure.messaging.dto.RemoteStopTransactionInstruction;
import com.charge.station.interfaces.dto.response.CommandAcceptedResponse;
import com.charge.station.shared.exception.BusinessException;
import com.charge.station.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 设备控制应用服务
 * 
 * 编排设备控制流程，调用基础设施层的路由服务发送指令。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceControlApplicationService {
    
    private final GatewayCommandRouter gatewayCommandRouter;
    private final ChargePointRepository chargePointRepository;
    
    /**
     * 启动充电
     * 
     * @param command 启动充电命令
     * @return 指令接受响应
     * @throws BusinessException 当充电桩不可用时
     * @throws ResourceNotFoundException 当充电桩不存在时
     */
    @Transactional(readOnly = true)
    public CommandAcceptedResponse startCharging(StartChargingCommand command) {
        Objects.requireNonNull(command, "启动充电命令不能为空");
        Objects.requireNonNull(command.chargePointId(), "充电桩ID不能为空");
        Objects.requireNonNull(command.connectorId(), "连接器ID不能为空");
        Objects.requireNonNull(command.idTag(), "用户标识不能为空");
        
        log.info("开始处理启动充电指令: chargePointId={}, connectorId={}, idTag={}",
            command.chargePointId().value(), command.connectorId(), command.idTag());
        
        // 步骤1: 查找充电桩聚合
        ChargePoint chargePoint = chargePointRepository.findById(command.chargePointId())
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + command.chargePointId().value()));
        
        // 步骤2: 检查充电桩状态
        if (!chargePoint.isAvailableForCharging()) {
            log.warn("充电桩当前不可用于充电: chargePointId={}, status={}",
                command.chargePointId().value(), chargePoint.getStatus());
            throw new BusinessException("充电桩当前不可用于充电，状态: " + chargePoint.getStatus().getOcppStatus());
        }
        
        // 步骤3: 验证连接器ID
        if (command.connectorId() <= 0 || command.connectorId() > chargePoint.getConnectors().size()) {
            log.warn("无效的连接器ID: chargePointId={}, connectorId={}, maxConnectors={}",
                command.chargePointId().value(), command.connectorId(), chargePoint.getConnectors().size());
            throw new BusinessException("无效的连接器ID: " + command.connectorId());
        }
        
        // 步骤4: 创建远程启动指令
        RemoteStartTransactionInstruction instruction = RemoteStartTransactionInstruction.of(
            command.connectorId(),
            command.idTag()
        );
        
        // 步骤5: 调用网关指令路由器发送指令
        String commandId = gatewayCommandRouter.sendCommand(
            command.chargePointId(),
            "RemoteStartTransaction",
            instruction
        );
        
        log.info("启动充电指令已发送: commandId={}, chargePointId={}, connectorId={}",
            commandId, command.chargePointId().value(), command.connectorId());
        
        return CommandAcceptedResponse.accepted(commandId);
    }
    
    /**
     * 停止充电
     * 
     * @param command 停止充电命令
     * @return 指令接受响应
     * @throws BusinessException 当充电桩不在线时
     * @throws ResourceNotFoundException 当充电桩不存在时
     */
    @Transactional(readOnly = true)
    public CommandAcceptedResponse stopCharging(StopChargingCommand command) {
        Objects.requireNonNull(command, "停止充电命令不能为空");
        Objects.requireNonNull(command.chargePointId(), "充电桩ID不能为空");
        Objects.requireNonNull(command.transactionId(), "交易ID不能为空");
        
        log.info("开始处理停止充电指令: chargePointId={}, transactionId={}",
            command.chargePointId().value(), command.transactionId());
        
        // 步骤1: 查找充电桩聚合
        ChargePoint chargePoint = chargePointRepository.findById(command.chargePointId())
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + command.chargePointId().value()));
        
        // 步骤2: 检查充电桩是否在线
        if (!chargePoint.isOnline()) {
            log.warn("充电桩当前离线: chargePointId={}, status={}",
                command.chargePointId().value(), chargePoint.getStatus());
            throw new BusinessException("充电桩当前离线，无法发送停止指令");
        }
        
        // 步骤3: 创建远程停止指令
        RemoteStopTransactionInstruction instruction = RemoteStopTransactionInstruction.of(
            command.transactionId()
        );
        
        // 步骤4: 调用网关指令路由器发送指令
        String commandId = gatewayCommandRouter.sendCommand(
            command.chargePointId(),
            "RemoteStopTransaction",
            instruction
        );
        
        log.info("停止充电指令已发送: commandId={}, chargePointId={}, transactionId={}",
            commandId, command.chargePointId().value(), command.transactionId());
        
        return CommandAcceptedResponse.accepted(commandId);
    }
    
    /**
     * 重置充电桩
     * 
     * @param chargePointId 充电桩ID
     * @param resetType 重置类型（Hard/Soft）
     * @return 指令接受响应
     * @throws BusinessException 当充电桩不在线时
     * @throws ResourceNotFoundException 当充电桩不存在时
     */
    @Transactional(readOnly = true)
    public CommandAcceptedResponse resetChargePoint(ChargePointId chargePointId, String resetType) {
        Objects.requireNonNull(chargePointId, "充电桩ID不能为空");
        Objects.requireNonNull(resetType, "重置类型不能为空");
        
        if (!"Hard".equals(resetType) && !"Soft".equals(resetType)) {
            throw new BusinessException("无效的重置类型，只支持Hard或Soft");
        }
        
        log.info("开始处理重置充电桩指令: chargePointId={}, resetType={}",
            chargePointId.value(), resetType);
        
        // 步骤1: 查找充电桩聚合
        ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
            .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId.value()));
        
        // 步骤2: 检查充电桩是否在线
        if (!chargePoint.isOnline()) {
            log.warn("充电桩当前离线: chargePointId={}, status={}",
                chargePointId.value(), chargePoint.getStatus());
            throw new BusinessException("充电桩当前离线，无法发送重置指令");
        }
        
        // 步骤3: 创建重置指令载荷
        var resetPayload = new Object() {
            public final String type = resetType;
        };
        
        // 步骤4: 调用网关指令路由器发送指令
        String commandId = gatewayCommandRouter.sendCommand(
            chargePointId,
            "Reset",
            resetPayload
        );
        
        log.info("重置充电桩指令已发送: commandId={}, chargePointId={}, resetType={}",
            commandId, chargePointId.value(), resetType);
        
        return CommandAcceptedResponse.accepted(commandId);
    }
}
