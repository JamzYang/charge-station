package com.charge.station.application.service;

import com.charge.station.application.command.StartChargingCommand;
import com.charge.station.application.command.StopChargingCommand;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.chargepoint.Connector;
import com.charge.station.domain.model.chargepoint.ConnectorType;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.infrastructure.external.gateway.GatewayCommandRouter;
import com.charge.station.interfaces.dto.response.CommandAcceptedResponse;
import com.charge.station.shared.exception.BusinessException;
import com.charge.station.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeviceControlApplicationService 单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("设备控制应用服务测试")
class DeviceControlApplicationServiceTest {

    @Mock
    private GatewayCommandRouter gatewayCommandRouter;
    
    @Mock
    private ChargePointRepository chargePointRepository;
    
    private DeviceControlApplicationService deviceControlApplicationService;
    
    @BeforeEach
    void setUp() {
        deviceControlApplicationService = new DeviceControlApplicationService(
            gatewayCommandRouter, chargePointRepository
        );
    }
    
    @Test
    @DisplayName("启动充电成功 - 充电桩可用")
    void startCharging_Success_WhenChargePointAvailable() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        StartChargingCommand command = StartChargingCommand.of(chargePointId, 1, "user123");
        
        ChargePoint chargePoint = createAvailableChargePoint(chargePointId);
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.of(chargePoint));
        when(gatewayCommandRouter.sendCommand(eq(chargePointId), eq("RemoteStartTransaction"), any()))
            .thenReturn("cmd-123");
        
        // When
        CommandAcceptedResponse response = deviceControlApplicationService.startCharging(command);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.commandId()).isEqualTo("cmd-123");
        assertThat(response.status()).isEqualTo("ACCEPTED");
        
        verify(chargePointRepository).findById(chargePointId);
        verify(gatewayCommandRouter).sendCommand(eq(chargePointId), eq("RemoteStartTransaction"), any());
    }
    
    @Test
    @DisplayName("启动充电失败 - 充电桩不存在")
    void startCharging_ThrowsException_WhenChargePointNotFound() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        StartChargingCommand command = StartChargingCommand.of(chargePointId, 1, "user123");
        
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> deviceControlApplicationService.startCharging(command))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("充电桩不存在: CP12345");
        
        verify(chargePointRepository).findById(chargePointId);
        verifyNoInteractions(gatewayCommandRouter);
    }
    
    @Test
    @DisplayName("启动充电失败 - 充电桩不可用")
    void startCharging_ThrowsException_WhenChargePointUnavailable() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        StartChargingCommand command = StartChargingCommand.of(chargePointId, 1, "user123");
        
        ChargePoint chargePoint = createUnavailableChargePoint(chargePointId);
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.of(chargePoint));
        
        // When & Then
        assertThatThrownBy(() -> deviceControlApplicationService.startCharging(command))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("充电桩当前不可用于充电");
        
        verify(chargePointRepository).findById(chargePointId);
        verifyNoInteractions(gatewayCommandRouter);
    }
    
    @Test
    @DisplayName("启动充电失败 - 无效的连接器ID")
    void startCharging_ThrowsException_WhenInvalidConnectorId() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        StartChargingCommand command = StartChargingCommand.of(chargePointId, 5, "user123"); // 无效的连接器ID
        
        ChargePoint chargePoint = createAvailableChargePoint(chargePointId);
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.of(chargePoint));
        
        // When & Then
        assertThatThrownBy(() -> deviceControlApplicationService.startCharging(command))
            .isInstanceOf(BusinessException.class)
            .hasMessage("无效的连接器ID: 5");
        
        verify(chargePointRepository).findById(chargePointId);
        verifyNoInteractions(gatewayCommandRouter);
    }
    
    @Test
    @DisplayName("停止充电成功 - 充电桩在线")
    void stopCharging_Success_WhenChargePointOnline() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        StopChargingCommand command = StopChargingCommand.of(chargePointId, 123);
        
        ChargePoint chargePoint = createOnlineChargePoint(chargePointId);
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.of(chargePoint));
        when(gatewayCommandRouter.sendCommand(eq(chargePointId), eq("RemoteStopTransaction"), any()))
            .thenReturn("cmd-456");
        
        // When
        CommandAcceptedResponse response = deviceControlApplicationService.stopCharging(command);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.commandId()).isEqualTo("cmd-456");
        assertThat(response.status()).isEqualTo("ACCEPTED");
        
        verify(chargePointRepository).findById(chargePointId);
        verify(gatewayCommandRouter).sendCommand(eq(chargePointId), eq("RemoteStopTransaction"), any());
    }
    
    @Test
    @DisplayName("停止充电失败 - 充电桩离线")
    void stopCharging_ThrowsException_WhenChargePointOffline() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        StopChargingCommand command = StopChargingCommand.of(chargePointId, 123);
        
        ChargePoint chargePoint = createOfflineChargePoint(chargePointId);
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.of(chargePoint));
        
        // When & Then
        assertThatThrownBy(() -> deviceControlApplicationService.stopCharging(command))
            .isInstanceOf(BusinessException.class)
            .hasMessage("充电桩当前离线，无法发送停止指令");
        
        verify(chargePointRepository).findById(chargePointId);
        verifyNoInteractions(gatewayCommandRouter);
    }
    
    @Test
    @DisplayName("重置充电桩成功 - 软重置")
    void resetChargePoint_Success_SoftReset() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        String resetType = "Soft";
        
        ChargePoint chargePoint = createOnlineChargePoint(chargePointId);
        when(chargePointRepository.findById(chargePointId)).thenReturn(Optional.of(chargePoint));
        when(gatewayCommandRouter.sendCommand(eq(chargePointId), eq("Reset"), any()))
            .thenReturn("cmd-789");
        
        // When
        CommandAcceptedResponse response = deviceControlApplicationService.resetChargePoint(chargePointId, resetType);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.commandId()).isEqualTo("cmd-789");
        assertThat(response.status()).isEqualTo("ACCEPTED");
        
        verify(chargePointRepository).findById(chargePointId);
        verify(gatewayCommandRouter).sendCommand(eq(chargePointId), eq("Reset"), any());
    }
    
    @Test
    @DisplayName("重置充电桩失败 - 无效的重置类型")
    void resetChargePoint_ThrowsException_InvalidResetType() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        String resetType = "Invalid";
        
        // When & Then
        assertThatThrownBy(() -> deviceControlApplicationService.resetChargePoint(chargePointId, resetType))
            .isInstanceOf(BusinessException.class)
            .hasMessage("无效的重置类型，只支持Hard或Soft");
        
        verifyNoInteractions(chargePointRepository);
        verifyNoInteractions(gatewayCommandRouter);
    }
    
    // 辅助方法
    private ChargePoint createAvailableChargePoint(ChargePointId chargePointId) {
        return ChargePoint.reconstruct(
            chargePointId,
            StationId.of("ST001"),
            "测试充电桩",
            "Model-X",
            "Vendor-A",
            "SN123456",
            "1.0.0",
            DeviceStatus.AVAILABLE,
            Instant.now(),
            PowerSpecification.ofKilowatts(BigDecimal.valueOf(60.0)),
            List.of(createConnector(1), createConnector(2)),
            Instant.now().minusSeconds(3600),
            Instant.now(),
            1L
        );
    }
    
    private ChargePoint createUnavailableChargePoint(ChargePointId chargePointId) {
        return ChargePoint.reconstruct(
            chargePointId,
            StationId.of("ST001"),
            "测试充电桩",
            "Model-X",
            "Vendor-A",
            "SN123456",
            "1.0.0",
            DeviceStatus.UNAVAILABLE,
            Instant.now(),
            PowerSpecification.ofKilowatts(BigDecimal.valueOf(60.0)),
            List.of(createConnector(1), createConnector(2)),
            Instant.now().minusSeconds(3600),
            Instant.now(),
            1L
        );
    }
    
    private ChargePoint createOnlineChargePoint(ChargePointId chargePointId) {
        return ChargePoint.reconstruct(
            chargePointId,
            StationId.of("ST001"),
            "测试充电桩",
            "Model-X",
            "Vendor-A",
            "SN123456",
            "1.0.0",
            DeviceStatus.CHARGING,
            Instant.now(),
            PowerSpecification.ofKilowatts(BigDecimal.valueOf(60.0)),
            List.of(createConnector(1), createConnector(2)),
            Instant.now().minusSeconds(3600),
            Instant.now(),
            1L
        );
    }
    
    private ChargePoint createOfflineChargePoint(ChargePointId chargePointId) {
        return ChargePoint.reconstruct(
            chargePointId,
            StationId.of("ST001"),
            "测试充电桩",
            "Model-X",
            "Vendor-A",
            "SN123456",
            "1.0.0",
            DeviceStatus.OFFLINE,
            Instant.now().minusSeconds(300),
            PowerSpecification.ofKilowatts(BigDecimal.valueOf(60.0)),
            List.of(createConnector(1), createConnector(2)),
            Instant.now().minusSeconds(3600),
            Instant.now(),
            1L
        );
    }
    
    private Connector createConnector(int connectorId) {
        Connector connector = new Connector(connectorId, ConnectorType.CCS2, PowerSpecification.ofKilowatts(60.0));
        // 设置连接器为可用状态，以便充电桩可以用于充电
        connector.updateStatus(DeviceStatus.AVAILABLE, Instant.now());
        return connector;
    }
}
