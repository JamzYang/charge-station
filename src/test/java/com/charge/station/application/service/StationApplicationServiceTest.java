package com.charge.station.application.service;

import com.charge.station.application.command.CreateStationCommand;
import com.charge.station.domain.model.station.*;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.domain.service.StationDomainService;
import com.charge.station.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * StationApplicationService单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class StationApplicationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationDomainService stationDomainService;

    @InjectMocks
    private StationApplicationService stationApplicationService;

    private CreateStationCommand createStationCommand;
    private Station station;
    private StationId stationId;

    @BeforeEach
    void setUp() {
        stationId = StationId.of("ST12345678901234567890");
        
        createStationCommand = CreateStationCommand.of(
            "万达广场充电站",
            "北京市朝阳区建国路93号",
            "商场内充电站",
            new BigDecimal("39.904200"),
            new BigDecimal("116.407400"),
            "OP001",
            LocalTime.of(8, 0),
            LocalTime.of(22, 0)
        );

        StationInfo stationInfo = StationInfo.of("万达广场充电站", "北京市朝阳区建国路93号", "商场内充电站");
        Location location = Location.of(39.904200, 116.407400);
        BusinessHours businessHours = BusinessHours.of(8, 0, 22, 0);
        
        station = new Station(stationInfo, location, "OP001", businessHours);
    }

    @Test
    void createStation_应该成功创建充电站() {
        // Given
        doNothing().when(stationDomainService).validateStationNameUniqueness(createStationCommand.name());
        doNothing().when(stationDomainService).validateStationLocation(any(Location.class), eq(100.0));
        when(stationRepository.save(any(Station.class))).thenReturn(station);

        // When
        Station result = stationApplicationService.createStation(createStationCommand);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStationInfo().name()).isEqualTo("万达广场充电站");
        assertThat(result.getStationInfo().address()).isEqualTo("北京市朝阳区建国路93号");
        assertThat(result.getOperatorId()).isEqualTo("OP001");

        verify(stationDomainService).validateStationNameUniqueness(createStationCommand.name());
        verify(stationDomainService).validateStationLocation(any(Location.class), eq(100.0));
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void createStation_当命令为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> stationApplicationService.createStation(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("创建充电站命令不能为空");
    }

    @Test
    void createStation_当名称已存在时应该抛出异常() {
        // Given
        doThrow(new IllegalArgumentException("充电站名称已存在"))
            .when(stationDomainService).validateStationNameUniqueness(createStationCommand.name());

        // When & Then
        assertThatThrownBy(() -> stationApplicationService.createStation(createStationCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("充电站名称已存在");

        verify(stationDomainService).validateStationNameUniqueness(createStationCommand.name());
        verify(stationRepository, never()).save(any(Station.class));
    }

    @Test
    void findStationById_应该成功查找充电站() {
        // Given
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        // When
        Station result = stationApplicationService.findStationById(stationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(station);

        verify(stationRepository).findById(stationId);
    }

    @Test
    void findStationById_当充电站不存在时应该抛出异常() {
        // Given
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stationApplicationService.findStationById(stationId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("充电站不存在: " + stationId);

        verify(stationRepository).findById(stationId);
    }

    @Test
    void findStationById_当ID为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> stationApplicationService.findStationById(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("充电站ID不能为空");
    }

    @Test
    void activateStation_应该成功激活充电站() {
        // Given
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(stationRepository.save(station)).thenReturn(station);

        // When
        Station result = stationApplicationService.activateStation(stationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StationStatus.ACTIVE);

        verify(stationRepository).findById(stationId);
        verify(stationRepository).save(station);
    }

    @Test
    void deactivateStation_应该成功停用充电站() {
        // Given
        // 先激活充电站
        station.activate();
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(stationRepository.save(station)).thenReturn(station);

        // When
        Station result = stationApplicationService.deactivateStation(stationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StationStatus.INACTIVE);

        verify(stationRepository).findById(stationId);
        verify(stationRepository).save(station);
    }

    @Test
    void setStationMaintenance_应该成功设置维护状态() {
        // Given
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(stationRepository.save(station)).thenReturn(station);

        // When
        Station result = stationApplicationService.setStationMaintenance(stationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StationStatus.MAINTENANCE);

        verify(stationRepository).findById(stationId);
        verify(stationRepository).save(station);
    }

    @Test
    void deleteStation_应该成功删除充电站() {
        // Given
        when(stationRepository.existsById(stationId)).thenReturn(true);
        doNothing().when(stationRepository).deleteById(stationId);

        // When
        stationApplicationService.deleteStation(stationId);

        // Then
        verify(stationRepository).existsById(stationId);
        verify(stationRepository).deleteById(stationId);
    }

    @Test
    void deleteStation_当充电站不存在时应该抛出异常() {
        // Given
        when(stationRepository.existsById(stationId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> stationApplicationService.deleteStation(stationId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("充电站不存在: " + stationId);

        verify(stationRepository).existsById(stationId);
        verify(stationRepository, never()).deleteById(any());
    }

    @Test
    void countStations_应该返回充电站总数() {
        // Given
        when(stationRepository.count()).thenReturn(100L);

        // When
        long result = stationApplicationService.countStations();

        // Then
        assertThat(result).isEqualTo(100L);

        verify(stationRepository).count();
    }
}
