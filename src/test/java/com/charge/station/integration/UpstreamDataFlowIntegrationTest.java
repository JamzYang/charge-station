package com.charge.station.integration;

import com.charge.station.StationServiceApplication;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import com.charge.station.domain.model.station.BusinessHours;
import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationInfo;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventStatus;
import com.charge.station.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import com.charge.station.shared.constant.KafkaTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 上行数据链路集成测试
 *
 * 使用Docker容器环境，测试从Kafka消息消费到数据库状态更新和发件箱事件生成的完整流程。
 *
 * 运行前请确保已启动Docker容器：
 * docker-compose -f docker-compose.test.yml up -d
 *
 * @author 架构师团队
 * @version 1.0
 */
@SpringBootTest(classes = StationServiceApplication.class)
@ActiveProfiles("integration")
@DirtiesContext
class UpstreamDataFlowIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargePointRepository chargePointRepository;

    @Autowired
    private OutboxEventJpaRepository outboxEventRepository;

    private Station testStation;
    private ChargePoint testChargePoint;

    /**
     * 检查测试环境是否就绪
     * 如果 Docker 容器未启动，则跳过测试
     */
    private void checkTestEnvironment() {
        try {
            // 检查数据库连接
            outboxEventRepository.count();

            // 检查 Kafka 连接
            kafkaTemplate.send(KafkaTopics.OCPP_EVENTS_UP, "test", "test").get(5, TimeUnit.SECONDS);

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

        // 清理数据
        cleanupTestData();

        // 创建测试充电站
        String uniqueStationName = "测试充电站_" + System.currentTimeMillis();
        StationInfo stationInfo = StationInfo.of(uniqueStationName, "测试地址", "测试描述");
        Location location = Location.of(39.9042, 116.4074);
        BusinessHours businessHours = new BusinessHours(LocalTime.of(0, 0), LocalTime.of(23, 59));

        testStation = new Station(stationInfo, location, "TEST_OPERATOR", businessHours);
        testStation = stationRepository.save(testStation);

        // 创建测试充电桩
        PowerSpecification powerSpec = PowerSpecification.ofKilowatts(60.0);
        String uniqueSerialNumber = "SN123456_" + System.currentTimeMillis();
        testChargePoint = new ChargePoint(
            testStation.getStationId(),
            "测试充电桩",
            "Model-X",
            "Vendor-A",
            uniqueSerialNumber,
            powerSpec
        );
        testChargePoint = chargePointRepository.save(testChargePoint);

        // 清除创建时产生的事件
        outboxEventRepository.deleteAll();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            // 清理发件箱事件
            outboxEventRepository.deleteAll();

            // 清理充电桩数据（如果存在）
            if (testChargePoint != null) {
                try {
                    chargePointRepository.findById(testChargePoint.getChargePointId())
                        .ifPresent(cp -> {
                            // 这里需要实现删除逻辑，但由于Repository接口没有delete方法
                            // 暂时跳过，依赖数据库的级联删除或测试环境的数据隔离
                        });
                } catch (Exception e) {
                    // 忽略删除错误
                }
            }

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
    
    @Test
    void 应该正确处理充电桩状态变更事件() throws Exception {
        // Given
        String chargePointId = testChargePoint.getChargePointId().value();
        String eventMessage = createStatusChangedEventMessage(chargePointId, "Available", "Unavailable");
        
        // When - 发送Kafka消息
        kafkaTemplate.send(KafkaTopics.OCPP_EVENTS_UP, chargePointId, eventMessage);
        
        // Then - 等待消息处理完成并验证结果
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 1. 验证充电桩状态已更新
            ChargePoint updatedChargePoint = chargePointRepository.findById(testChargePoint.getChargePointId())
                .orElseThrow();
            assertThat(updatedChargePoint.getStatus()).isEqualTo(DeviceStatus.AVAILABLE);
            
            // 2. 验证发件箱事件已生成
            List<OutboxEventEntity> outboxEvents = outboxEventRepository.findAll();
            assertThat(outboxEvents).isNotEmpty();
            
            OutboxEventEntity statusChangedEvent = outboxEvents.stream()
                .filter(event -> "chargepoint.status_changed".equals(event.getEventType()))
                .findFirst()
                .orElseThrow();
            
            assertThat(statusChangedEvent.getAggregateType()).isEqualTo("ChargePoint");
            assertThat(statusChangedEvent.getAggregateId()).isEqualTo(chargePointId);
            assertThat(statusChangedEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        });
    }
    
    @Test
    void 应该正确处理充电桩连接事件() throws Exception {
        // Given
        String chargePointId = testChargePoint.getChargePointId().value();
        String eventMessage = createChargePointConnectedEventMessage(chargePointId, "Model-Y", "Vendor-B", "v2.0.1");
        
        // When - 发送Kafka消息
        kafkaTemplate.send(KafkaTopics.OCPP_EVENTS_UP, chargePointId, eventMessage);
        
        // Then - 等待消息处理完成并验证结果
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 1. 验证充电桩固件版本已更新
            ChargePoint updatedChargePoint = chargePointRepository.findById(testChargePoint.getChargePointId())
                .orElseThrow();
            assertThat(updatedChargePoint.getFirmwareVersion()).isEqualTo("v2.0.1");
            assertThat(updatedChargePoint.getLastHeartbeat()).isNotNull();
            
            // 2. 如果之前是离线状态，应该更新为可用状态
            if (testChargePoint.getStatus() == DeviceStatus.OFFLINE) {
                assertThat(updatedChargePoint.getStatus()).isEqualTo(DeviceStatus.AVAILABLE);
            }
        });
    }
    
    @Test
    void 应该正确处理充电桩断开连接事件() throws Exception {
        // Given
        String chargePointId = testChargePoint.getChargePointId().value();
        String eventMessage = createChargePointDisconnectedEventMessage(chargePointId, "tcp_connection_closed");
        
        // When - 发送Kafka消息
        kafkaTemplate.send(KafkaTopics.OCPP_EVENTS_UP, chargePointId, eventMessage);
        
        // Then - 等待消息处理完成并验证结果
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 1. 验证充电桩状态已更新为离线
            ChargePoint updatedChargePoint = chargePointRepository.findById(testChargePoint.getChargePointId())
                .orElseThrow();
            assertThat(updatedChargePoint.getStatus()).isEqualTo(DeviceStatus.OFFLINE);
            
            // 2. 验证发件箱事件已生成
            List<OutboxEventEntity> outboxEvents = outboxEventRepository.findAll();
            assertThat(outboxEvents).isNotEmpty();
            
            OutboxEventEntity statusChangedEvent = outboxEvents.stream()
                .filter(event -> "chargepoint.status_changed".equals(event.getEventType()))
                .findFirst()
                .orElseThrow();
            
            assertThat(statusChangedEvent.getAggregateType()).isEqualTo("ChargePoint");
            assertThat(statusChangedEvent.getAggregateId()).isEqualTo(chargePointId);
            assertThat(statusChangedEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        });
    }
    
    @Test
    void 应该忽略无效的事件消息() throws Exception {
        // Given - 无效的JSON消息
        String invalidMessage = "{ invalid json }";
        
        // When - 发送无效消息
        kafkaTemplate.send(KafkaTopics.OCPP_EVENTS_UP, "invalid-key", invalidMessage);
        
        // Then - 等待一段时间，确保没有产生异常或副作用
        Thread.sleep(2000);
        
        // 验证没有产生发件箱事件
        List<OutboxEventEntity> outboxEvents = outboxEventRepository.findAll();
        assertThat(outboxEvents).isEmpty();
    }
    
    private String createStatusChangedEventMessage(String chargePointId, String newStatus, String previousStatus) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "eventId", "test-event-" + System.currentTimeMillis(),
            "eventType", "connector.status_changed",
            "chargePointId", chargePointId,
            "gatewayId", "test-gateway",
            "timestamp", Instant.now().toString(),
            "payload", Map.of(
                "connectorId", 1,
                "status", newStatus,
                "previousStatus", previousStatus,
                "errorCode", "NoError"
            )
        ));
    }
    
    private String createChargePointConnectedEventMessage(String chargePointId, String model, String vendor, String firmwareVersion) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "eventId", "test-event-" + System.currentTimeMillis(),
            "eventType", "charge_point.connected",
            "chargePointId", chargePointId,
            "gatewayId", "test-gateway",
            "timestamp", Instant.now().toString(),
            "payload", Map.of(
                "model", model,
                "vendor", vendor,
                "firmwareVersion", firmwareVersion
            )
        ));
    }
    
    private String createChargePointDisconnectedEventMessage(String chargePointId, String reason) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "eventId", "test-event-" + System.currentTimeMillis(),
            "eventType", "charge_point.disconnected",
            "chargePointId", chargePointId,
            "gatewayId", "test-gateway",
            "timestamp", Instant.now().toString(),
            "payload", Map.of(
                "reason", reason
            )
        ));
    }
}
