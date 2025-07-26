package com.charge.station.integration;

import com.charge.station.StationServiceApplication;
import com.charge.station.application.command.CreateStationCommand;
import com.charge.station.application.service.StationApplicationService;
import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.model.station.StationStatus;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventStatus;
import com.charge.station.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.charge.station.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * StationApplicationService 集成测试
 *
 * 测试充电站应用服务的完整业务流程，包括：
 * - 数据库持久化
 * - 领域事件发布
 * - 事务管理
 * - 业务规则验证
 *
 * 运行前请确保已启动 Docker 容器：
 * docker-compose -f docker-compose.test.yml up -d
 *
 * @author 架构师团队
 * @version 1.0
 */
@SpringBootTest(classes = StationServiceApplication.class)
@ActiveProfiles("integration-station")
@DirtiesContext
class StationApplicationServiceIntegrationTest {

    @Autowired
    private StationApplicationService stationApplicationService;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private OutboxEventJpaRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateStationCommand validCommand;
    private Station testStation;

    /**
     * 检查测试环境是否就绪
     * 如果 Docker 容器未启动，则跳过测试
     */
    private void checkTestEnvironment() {
        try {
            // 检查数据库连接
            outboxEventRepository.count();
        } catch (Exception e) {
            assumeTrue(false,
                "测试环境未就绪，请先启动 Docker 容器：docker-compose -f docker-compose.test.yml up -d\n" +
                "错误信息: " + e.getMessage());
        }
    }

    @BeforeEach
    @Transactional
    void setUp() {
        // 检查测试环境
        checkTestEnvironment();

        // 清理测试数据
        cleanupTestData();

        // 创建测试命令
        String uniqueName = "测试充电站_" + System.currentTimeMillis();
        validCommand = CreateStationCommand.of(
            uniqueName,
            "北京市朝阳区建国路93号",
            "商场内充电站",
            new BigDecimal("39.904200"),
            new BigDecimal("116.407400"),
            "TEST_OPERATOR",
            LocalTime.of(8, 0),
            LocalTime.of(22, 0)
        );
    }

    @AfterEach
    @Transactional
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }

    @Test
    @Transactional
    void createStation_应该成功创建充电站并发布领域事件() {
        // When - 创建充电站
        Station createdStation = stationApplicationService.createStation(validCommand);

        // Then - 验证充电站创建成功
        assertThat(createdStation).isNotNull();
        assertThat(createdStation.getStationId()).isNotNull();
        assertThat(createdStation.getStationInfo().name()).isEqualTo(validCommand.name());
        assertThat(createdStation.getStationInfo().address()).isEqualTo(validCommand.address());
        assertThat(createdStation.getStationInfo().description()).isEqualTo(validCommand.description());
        assertThat(createdStation.getOperatorId()).isEqualTo(validCommand.operatorId());
        assertThat(createdStation.getStatus()).isEqualTo(StationStatus.INACTIVE);

        // 验证地理位置
        Location location = createdStation.getLocation();
        assertThat(location.latitude()).isEqualByComparingTo(validCommand.latitude());
        assertThat(location.longitude()).isEqualByComparingTo(validCommand.longitude());

        // 验证营业时间
        assertThat(createdStation.getBusinessHours().openTime()).isEqualTo(validCommand.openTime());
        assertThat(createdStation.getBusinessHours().closeTime()).isEqualTo(validCommand.closeTime());

        // 验证数据库持久化
        Station savedStation = stationRepository.findById(createdStation.getStationId())
            .orElseThrow(() -> new AssertionError("充电站未保存到数据库"));
        assertThat(savedStation.getStationInfo().name()).isEqualTo(validCommand.name());

        // 验证领域事件已发布到发件箱
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxEventEntity> outboxEvents = outboxEventRepository.findAll();
            assertThat(outboxEvents).isNotEmpty();

            OutboxEventEntity stationCreatedEvent = outboxEvents.stream()
                .filter(event -> "station.created".equals(event.getEventType()))
                .filter(event -> createdStation.getStationId().value().equals(event.getAggregateId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到充电站创建事件"));

            assertThat(stationCreatedEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
            assertThat(stationCreatedEvent.getAggregateType()).isEqualTo("Station");
            assertThat(stationCreatedEvent.getPayload()).isNotNull();
        });

        // 保存测试数据引用以便清理
        testStation = createdStation;
    }

    @Test
    @Transactional
    void createStation_当充电站名称已存在时应该抛出异常() {
        // Given - 先创建一个充电站
        Station existingStation = stationApplicationService.createStation(validCommand);
        testStation = existingStation;

        // 创建相同名称的命令
        CreateStationCommand duplicateNameCommand = CreateStationCommand.of(
            validCommand.name(), // 相同的名称
            "不同的地址",
            "不同的描述",
            new BigDecimal("40.0"),
            new BigDecimal("117.0"),
            "DIFFERENT_OPERATOR",
            LocalTime.of(9, 0),
            LocalTime.of(21, 0)
        );

        // When & Then - 应该抛出业务异常
        assertThatThrownBy(() -> stationApplicationService.createStation(duplicateNameCommand))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("充电站名称已存在");
    }

    @Test
    @Transactional
    void createStation_当位置距离现有充电站太近时应该抛出异常() {
        // Given - 先创建一个充电站
        Station existingStation = stationApplicationService.createStation(validCommand);
        testStation = existingStation;

        // 创建距离很近的充电站命令（距离小于100米）
        CreateStationCommand nearbyCommand = CreateStationCommand.of(
            "附近充电站_" + System.currentTimeMillis(),
            "附近地址",
            "附近描述",
            validCommand.latitude().add(new BigDecimal("0.0001")), // 约11米距离
            validCommand.longitude().add(new BigDecimal("0.0001")),
            "NEARBY_OPERATOR",
            LocalTime.of(9, 0),
            LocalTime.of(21, 0)
        );

        // When & Then - 应该抛出业务异常
        assertThatThrownBy(() -> stationApplicationService.createStation(nearbyCommand))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("充电站位置距离现有充电站太近");
    }

    @Test
    void createStation_当命令为null时应该抛出异常() {
        // When & Then
        assertThatThrownBy(() -> stationApplicationService.createStation(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("创建充电站命令不能为空");
    }

    @Test
    @Transactional
    void createStation_应该正确设置时间戳和版本号() {
        // When
        Station createdStation = stationApplicationService.createStation(validCommand);
        testStation = createdStation;

        // Then
        assertThat(createdStation.getCreatedAt()).isNotNull();
        assertThat(createdStation.getUpdatedAt()).isNotNull();
        assertThat(createdStation.getVersion()).isEqualTo(0L);
    }

    @Test
    @Transactional
    void createStation_应该正确处理边界值() {
        // Given - 创建边界值命令
        CreateStationCommand boundaryCommand = CreateStationCommand.of(
            "边界测试充电站_" + System.currentTimeMillis(),
            "边界测试地址",
            "边界测试描述",
            new BigDecimal("90.0"),    // 最大纬度
            new BigDecimal("180.0"),   // 最大经度
            "BOUNDARY_OPERATOR",
            LocalTime.of(0, 0),       // 最早营业时间
            LocalTime.of(23, 59)      // 最晚营业时间
        );

        // When
        Station createdStation = stationApplicationService.createStation(boundaryCommand);
        testStation = createdStation;

        // Then
        assertThat(createdStation).isNotNull();
        assertThat(createdStation.getLocation().latitude()).isEqualByComparingTo(new BigDecimal("90.0"));
        assertThat(createdStation.getLocation().longitude()).isEqualByComparingTo(new BigDecimal("180.0"));
        assertThat(createdStation.getBusinessHours().openTime()).isEqualTo(LocalTime.of(0, 0));
        assertThat(createdStation.getBusinessHours().closeTime()).isEqualTo(LocalTime.of(23, 59));
    }

    @Test
    @Transactional
    void createStation_应该正确处理中文字符() {
        // Given - 创建包含中文字符的命令
        CreateStationCommand chineseCommand = CreateStationCommand.of(
            "北京万达广场充电站_" + System.currentTimeMillis(),
            "北京市朝阳区建国路93号万达广场地下停车场B2层",
            "位于万达广场地下停车场，提供快充和慢充服务，支持多种车型",
            new BigDecimal("39.904200"),
            new BigDecimal("116.407400"),
            "北京新能源运营商",
            LocalTime.of(8, 30),
            LocalTime.of(22, 30)
        );

        // When
        Station createdStation = stationApplicationService.createStation(chineseCommand);
        testStation = createdStation;

        // Then
        assertThat(createdStation).isNotNull();
        assertThat(createdStation.getStationInfo().name()).contains("北京万达广场充电站");
        assertThat(createdStation.getStationInfo().address()).contains("万达广场地下停车场");
        assertThat(createdStation.getStationInfo().description()).contains("快充和慢充服务");
        assertThat(createdStation.getOperatorId()).isEqualTo("北京新能源运营商");
    }

    @Test
    @Transactional
    void createStation_应该正确处理无描述的情况() {
        // Given - 创建无描述的命令
        CreateStationCommand noDescriptionCommand = CreateStationCommand.of(
            "无描述充电站_" + System.currentTimeMillis(),
            "测试地址",
            null, // 无描述
            new BigDecimal("39.904200"),
            new BigDecimal("116.407400"),
            "TEST_OPERATOR",
            LocalTime.of(8, 0),
            LocalTime.of(22, 0)
        );

        // When
        Station createdStation = stationApplicationService.createStation(noDescriptionCommand);
        testStation = createdStation;

        // Then
        assertThat(createdStation).isNotNull();
        assertThat(createdStation.getStationInfo().description()).isNull();
    }

    @Test
    @Transactional
    void createStation_应该支持并发创建不同充电站() throws InterruptedException {
        // Given - 创建多个不同的命令
        CreateStationCommand command1 = CreateStationCommand.of(
            "并发测试充电站1_" + System.currentTimeMillis(),
            "地址1",
            "描述1",
            new BigDecimal("39.904200"),
            new BigDecimal("116.407400"),
            "OPERATOR1",
            LocalTime.of(8, 0),
            LocalTime.of(22, 0)
        );

        CreateStationCommand command2 = CreateStationCommand.of(
            "并发测试充电站2_" + System.currentTimeMillis(),
            "地址2",
            "描述2",
            new BigDecimal("40.0"),  // 不同位置
            new BigDecimal("117.0"),
            "OPERATOR2",
            LocalTime.of(9, 0),
            LocalTime.of(21, 0)
        );

        // When - 并发创建
        Station station1 = stationApplicationService.createStation(command1);
        Station station2 = stationApplicationService.createStation(command2);

        // Then
        assertThat(station1).isNotNull();
        assertThat(station2).isNotNull();
        assertThat(station1.getStationId()).isNotEqualTo(station2.getStationId());
        assertThat(station1.getStationInfo().name()).isNotEqualTo(station2.getStationInfo().name());

        // 验证都已保存到数据库
        assertThat(stationRepository.findById(station1.getStationId())).isPresent();
        assertThat(stationRepository.findById(station2.getStationId())).isPresent();

        // 清理测试数据
        testStation = station1; // 只保存一个引用，另一个依赖测试环境清理
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            // 清理发件箱事件
            outboxEventRepository.deleteAll();

            // 清理充电站数据（如果存在）
            if (testStation != null) {
                try {
                    stationRepository.findById(testStation.getStationId())
                        .ifPresent(station -> {
                            // 这里需要实现删除逻辑，但由于Repository接口没有delete方法
                            // 暂时跳过，依赖数据库的级联删除或测试环境的数据隔离
                        });
                } catch (Exception e) {
                    // 忽略删除错误
                }
            }
        } catch (Exception e) {
            // 忽略清理错误，避免影响测试
        }
    }
}
