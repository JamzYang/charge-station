package com.charge.station.domain.model.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeviceStatus单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
class DeviceStatusTest {

    @Test
    void fromOcppStatus_应该正确转换有效的OCPP状态() {
        // Given & When & Then
        assertThat(DeviceStatus.fromOcppStatus("Available")).isEqualTo(DeviceStatus.AVAILABLE);
        assertThat(DeviceStatus.fromOcppStatus("Preparing")).isEqualTo(DeviceStatus.PREPARING);
        assertThat(DeviceStatus.fromOcppStatus("Charging")).isEqualTo(DeviceStatus.CHARGING);
        assertThat(DeviceStatus.fromOcppStatus("SuspendedEVSE")).isEqualTo(DeviceStatus.SUSPENDED_EVSE);
        assertThat(DeviceStatus.fromOcppStatus("SuspendedEV")).isEqualTo(DeviceStatus.SUSPENDED_EV);
        assertThat(DeviceStatus.fromOcppStatus("Finishing")).isEqualTo(DeviceStatus.FINISHING);
        assertThat(DeviceStatus.fromOcppStatus("Reserved")).isEqualTo(DeviceStatus.RESERVED);
        assertThat(DeviceStatus.fromOcppStatus("Unavailable")).isEqualTo(DeviceStatus.UNAVAILABLE);
        assertThat(DeviceStatus.fromOcppStatus("Faulted")).isEqualTo(DeviceStatus.FAULTED);
        assertThat(DeviceStatus.fromOcppStatus("Offline")).isEqualTo(DeviceStatus.OFFLINE);
    }

    @Test
    void fromOcppStatus_应该忽略大小写() {
        // Given & When & Then
        assertThat(DeviceStatus.fromOcppStatus("available")).isEqualTo(DeviceStatus.AVAILABLE);
        assertThat(DeviceStatus.fromOcppStatus("CHARGING")).isEqualTo(DeviceStatus.CHARGING);
        assertThat(DeviceStatus.fromOcppStatus("Unavailable")).isEqualTo(DeviceStatus.UNAVAILABLE);
    }

    @Test
    void fromOcppStatus_对于null值应该返回UNAVAILABLE() {
        // Given & When
        DeviceStatus result = DeviceStatus.fromOcppStatus(null);
        
        // Then
        assertThat(result).isEqualTo(DeviceStatus.UNAVAILABLE);
    }

    @Test
    void fromOcppStatus_对于未知状态应该返回UNAVAILABLE() {
        // Given & When
        DeviceStatus result = DeviceStatus.fromOcppStatus("UnknownStatus");
        
        // Then
        assertThat(result).isEqualTo(DeviceStatus.UNAVAILABLE);
    }

    @Test
    void isAvailableForCharging_应该正确判断是否可用于充电() {
        // Given & When & Then
        assertThat(DeviceStatus.AVAILABLE.isAvailableForCharging()).isTrue();
        assertThat(DeviceStatus.PREPARING.isAvailableForCharging()).isFalse();
        assertThat(DeviceStatus.CHARGING.isAvailableForCharging()).isFalse();
        assertThat(DeviceStatus.UNAVAILABLE.isAvailableForCharging()).isFalse();
        assertThat(DeviceStatus.OFFLINE.isAvailableForCharging()).isFalse();
    }

    @Test
    void isCharging_应该正确判断是否正在充电() {
        // Given & When & Then
        assertThat(DeviceStatus.CHARGING.isCharging()).isTrue();
        assertThat(DeviceStatus.PREPARING.isCharging()).isTrue();
        assertThat(DeviceStatus.FINISHING.isCharging()).isTrue();
        assertThat(DeviceStatus.AVAILABLE.isCharging()).isFalse();
        assertThat(DeviceStatus.UNAVAILABLE.isCharging()).isFalse();
        assertThat(DeviceStatus.SUSPENDED_EVSE.isCharging()).isFalse();
    }

    @Test
    void isOffline_应该正确判断是否离线() {
        // Given & When & Then
        assertThat(DeviceStatus.OFFLINE.isOffline()).isTrue();
        assertThat(DeviceStatus.UNAVAILABLE.isOffline()).isTrue();
        assertThat(DeviceStatus.AVAILABLE.isOffline()).isFalse();
        assertThat(DeviceStatus.CHARGING.isOffline()).isFalse();
        assertThat(DeviceStatus.FAULTED.isOffline()).isFalse();
    }

    @Test
    void getOcppStatus_应该返回正确的OCPP状态字符串() {
        // Given & When & Then
        assertThat(DeviceStatus.AVAILABLE.getOcppStatus()).isEqualTo("Available");
        assertThat(DeviceStatus.CHARGING.getOcppStatus()).isEqualTo("Charging");
        assertThat(DeviceStatus.SUSPENDED_EVSE.getOcppStatus()).isEqualTo("SuspendedEVSE");
        assertThat(DeviceStatus.OFFLINE.getOcppStatus()).isEqualTo("Offline");
    }
}
