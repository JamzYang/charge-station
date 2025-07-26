package com.charge.station.infrastructure.persistence.jpa.converter;

import com.charge.station.domain.model.chargepoint.*;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.infrastructure.persistence.jpa.entity.ChargePointEntity;
import com.charge.station.infrastructure.persistence.jpa.entity.ConnectorEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 充电桩实体转换器
 * 
 * 负责充电桩领域对象与JPA实体之间的转换。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class ChargePointEntityConverter {
    
    /**
     * 将充电桩领域对象转换为JPA实体
     * 
     * @param chargePoint 充电桩领域对象
     * @return JPA实体
     */
    public ChargePointEntity toEntity(ChargePoint chargePoint) {
        Objects.requireNonNull(chargePoint, "充电桩不能为空");
        
        ChargePointEntity entity = new ChargePointEntity(
            chargePoint.getChargePointId().value(),
            chargePoint.getStationId().value(),
            chargePoint.getName(),
            chargePoint.getModel(),
            chargePoint.getVendor(),
            chargePoint.getSerialNumber(),
            chargePoint.getFirmwareVersion(),
            chargePoint.getStatus().getOcppStatus(),
            chargePoint.getPowerSpecification() != null ? 
                chargePoint.getPowerSpecification().maxPower() : null,
            chargePoint.getConnectors().size()
        );
        
        // 设置时间戳和版本号
        entity.setLastHeartbeat(chargePoint.getLastHeartbeat());
        entity.setCreatedAt(chargePoint.getCreatedAt());
        entity.setUpdatedAt(chargePoint.getUpdatedAt());
        entity.setVersion(chargePoint.getVersion());
        
        // 转换充电枪
        List<ConnectorEntity> connectorEntities = chargePoint.getConnectors().stream()
            .map(connector -> toConnectorEntity(connector, chargePoint.getChargePointId().value()))
            .collect(Collectors.toList());
        
        connectorEntities.forEach(entity::addConnector);
        
        return entity;
    }
    
    /**
     * 将JPA实体转换为充电桩领域对象
     *
     * @param entity JPA实体
     * @return 充电桩领域对象
     */
    public ChargePoint toDomain(ChargePointEntity entity) {
        Objects.requireNonNull(entity, "充电桩实体不能为空");

        ChargePointId chargePointId = ChargePointId.of(entity.getId());
        StationId stationId = StationId.of(entity.getStationId());
        DeviceStatus status = DeviceStatus.fromOcppStatus(entity.getStatus());
        PowerSpecification powerSpec = entity.getPowerOutput() != null ?
            PowerSpecification.ofKilowatts(entity.getPowerOutput()) : null;

        // 转换充电枪
        List<Connector> connectors = entity.getConnectors().stream()
            .map(this::toConnectorDomain)
            .collect(Collectors.toList());

        // 使用重建方法创建充电桩对象，避免发布领域事件
        return ChargePoint.reconstruct(
            chargePointId,
            stationId,
            entity.getName(),
            entity.getModel(),
            entity.getVendor(),
            entity.getSerialNumber(),
            entity.getFirmwareVersion(),
            status,
            entity.getLastHeartbeat(),
            powerSpec,
            connectors,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }
    
    /**
     * 将充电枪领域对象转换为JPA实体
     * 
     * @param connector 充电枪领域对象
     * @param chargePointId 充电桩ID
     * @return 充电枪JPA实体
     */
    private ConnectorEntity toConnectorEntity(Connector connector, String chargePointId) {
        ConnectorEntity entity = new ConnectorEntity(
            chargePointId,
            connector.getConnectorId(),
            connector.getConnectorType().getCode(),
            connector.getStatus().getOcppStatus(),
            connector.getPowerSpecification().maxPower()
        );
        
        // 设置ID和时间戳（如果存在）
        if (connector.getId() != null) {
            entity.setId(connector.getId());
        }
        entity.setCreatedAt(connector.getCreatedAt());
        entity.setUpdatedAt(connector.getUpdatedAt());
        
        return entity;
    }
    
    /**
     * 将充电枪JPA实体转换为领域对象
     * 
     * @param entity 充电枪JPA实体
     * @return 充电枪领域对象
     */
    private Connector toConnectorDomain(ConnectorEntity entity) {
        ConnectorType connectorType = ConnectorType.fromCode(entity.getConnectorType());
        PowerSpecification powerSpec = PowerSpecification.ofKilowatts(entity.getMaxPower());
        DeviceStatus status = DeviceStatus.fromOcppStatus(entity.getStatus());
        
        // 暂时返回一个简化的连接器对象，避免反射问题
        // TODO: 需要修改Connector类的构造函数或添加Builder模式
        Connector connector = new Connector(
            entity.getConnectorId(),
            connectorType,
            powerSpec
        );

        return connector;
    }
}
