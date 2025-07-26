package com.charge.station.interfaces.assembler;

import com.charge.station.application.command.CreateChargePointCommand;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.Connector;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.interfaces.dto.request.CreateChargePointRequest;
import com.charge.station.interfaces.dto.response.ChargePointResponse;
import com.charge.station.interfaces.dto.response.ConnectorResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 充电桩转换器
 * 
 * 负责在DTO和领域对象之间进行转换。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class ChargePointAssembler {

    /**
     * 将创建请求转换为创建命令
     * 
     * @param stationId 充电站ID
     * @param request 创建请求
     * @return 创建命令
     */
    public CreateChargePointCommand toCreateCommand(StationId stationId, CreateChargePointRequest request) {
        Objects.requireNonNull(stationId, "充电站ID不能为空");
        Objects.requireNonNull(request, "创建请求不能为空");
        
        return CreateChargePointCommand.of(
            stationId,
            request.name(),
            request.model(),
            request.vendor(),
            request.serialNumber(),
            request.maxPower()
        );
    }

    /**
     * 将充电桩领域对象转换为响应DTO
     * 
     * @param chargePoint 充电桩领域对象
     * @return 响应DTO
     */
    public ChargePointResponse toResponse(ChargePoint chargePoint) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        List<ConnectorResponse> connectorResponses = chargePoint.getConnectors().stream()
            .map(this::toConnectorResponse)
            .toList();
        
        return ChargePointResponse.of(
            chargePoint.getChargePointId().value(),
            chargePoint.getStationId().value(),
            chargePoint.getName(),
            chargePoint.getModel(),
            chargePoint.getVendor(),
            chargePoint.getSerialNumber(),
            chargePoint.getFirmwareVersion(),
            chargePoint.getStatus().getOcppStatus(),
            getStatusDescription(chargePoint.getStatus().getOcppStatus()),
            chargePoint.getLastHeartbeat(),
            chargePoint.getPowerSpecification().maxPower(),
            chargePoint.getPowerSpecification().getFormattedPower(),
            chargePoint.isOnline(),
            chargePoint.isAvailableForCharging(),
            chargePoint.hasAvailableConnector(),
            chargePoint.getAvailableConnectorCount(),
            connectorResponses,
            chargePoint.getCreatedAt(),
            chargePoint.getUpdatedAt()
        );
    }

    /**
     * 将充电桩领域对象转换为简化响应DTO
     * 
     * @param chargePoint 充电桩领域对象
     * @return 简化响应DTO
     */
    public ChargePointResponse toSimpleResponse(ChargePoint chargePoint) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        return ChargePointResponse.simple(
            chargePoint.getChargePointId().value(),
            chargePoint.getStationId().value(),
            chargePoint.getName(),
            chargePoint.getStatus().getOcppStatus(),
            getStatusDescription(chargePoint.getStatus().getOcppStatus()),
            chargePoint.isOnline(),
            chargePoint.isAvailableForCharging(),
            chargePoint.getAvailableConnectorCount()
        );
    }

    /**
     * 将连接器领域对象转换为响应DTO
     * 
     * @param connector 连接器领域对象
     * @return 连接器响应DTO
     */
    public ConnectorResponse toConnectorResponse(Connector connector) {
        Objects.requireNonNull(connector, "连接器不能为空");
        
        return ConnectorResponse.of(
            connector.getConnectorId(),
            connector.getConnectorType().getCode(),
            connector.getConnectorType().getDescription(),
            connector.getStatus().getOcppStatus(),
            getStatusDescription(connector.getStatus().getOcppStatus()),
            connector.getPowerSpecification().maxPower(),
            connector.getPowerSpecification().getFormattedPower(),
            connector.isAvailableForCharging(),
            connector.isCharging(),
            connector.isOffline(),
            connector.isFastCharging(),
            connector.isSuperCharging(),
            connector.getDisplayName(),
            connector.getCreatedAt(),
            connector.getUpdatedAt()
        );
    }

    /**
     * 批量转换充电桩列表为响应DTO列表
     * 
     * @param chargePoints 充电桩列表
     * @return 响应DTO列表
     */
    public List<ChargePointResponse> toResponseList(List<ChargePoint> chargePoints) {
        Objects.requireNonNull(chargePoints, "充电桩列表不能为空");
        
        return chargePoints.stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * 批量转换充电桩列表为简化响应DTO列表
     * 
     * @param chargePoints 充电桩列表
     * @return 简化响应DTO列表
     */
    public List<ChargePointResponse> toSimpleResponseList(List<ChargePoint> chargePoints) {
        Objects.requireNonNull(chargePoints, "充电桩列表不能为空");
        
        return chargePoints.stream()
            .map(this::toSimpleResponse)
            .toList();
    }

    /**
     * 获取状态描述
     * 
     * @param statusCode 状态代码
     * @return 状态描述
     */
    private String getStatusDescription(String statusCode) {
        return switch (statusCode) {
            case "Available" -> "可用";
            case "Preparing" -> "准备中";
            case "Charging" -> "充电中";
            case "SuspendedEVSE" -> "设备暂停";
            case "SuspendedEV" -> "车辆暂停";
            case "Finishing" -> "结束中";
            case "Reserved" -> "已预约";
            case "Unavailable" -> "不可用";
            case "Faulted" -> "故障";
            case "Offline" -> "离线";
            default -> "未知状态";
        };
    }
}
