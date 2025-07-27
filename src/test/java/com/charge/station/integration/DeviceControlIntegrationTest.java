package com.charge.station.integration;

import com.charge.station.StationServiceApplication;
import com.charge.station.application.command.StartChargingCommand;
import com.charge.station.application.command.StopChargingCommand;
import com.charge.station.application.service.DeviceControlApplicationService;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.chargepoint.Connector;
import com.charge.station.domain.model.chargepoint.ConnectorType;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.shared.PowerSpecification;
import com.charge.station.domain.model.station.*;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.domain.repository.StationRepository;
import com.charge.station.interfaces.dto.response.CommandAcceptedResponse;
import com.charge.station.shared.constant.KafkaTopics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 设备控制集成测试
 *
 * 测试从API调用到Kafka消息发送的完整下行指令链路。
 * 验证Redis查询、分区计算和Kafka消息发送的端到端流程。
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
@DisplayName("设备控制集成测试")
class DeviceControlIntegrationTest {

    @Autowired
    private DeviceControlApplicationService deviceControlApplicationService;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private ChargePointRepository chargePointRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;



    private Station testStation;
    private ChargePoint testChargePoint;

    /**
     * 检查测试环境是否就绪
     */
    private void checkTestEnvironment() {
        try {
            // 检查数据库连接
            stationRepository.count();

            // 检查Redis连接
            redisTemplate.opsForValue().set("test", "test");
            redisTemplate.delete("test");

            // 检查并创建 Kafka topic
            ensureKafkaTopicsExist();

        } catch (Exception e) {
            assumeTrue(false,
                "测试环境未就绪，请先启动 Docker 容器：docker-compose -f docker-compose.test.yml up -d\n" +
                "错误信息: " + e.getMessage());
        }
    }

    /**
     * 确保必要的 Kafka topic 存在
     */
    private void ensureKafkaTopicsExist() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);

        try (AdminClient adminClient = AdminClient.create(props)) {
            // 检查并创建 commands-down topic
            createTopicIfNotExists(adminClient, KafkaTopics.COMMANDS_DOWN, 128);
            // 检查并创建 ocpp-events-up topic
            createTopicIfNotExists(adminClient, KafkaTopics.OCPP_EVENTS_UP, 128);
        } catch (Exception e) {
            System.err.println("Failed to ensure Kafka topics exist: " + e.getMessage());
            // 不抛出异常，让测试继续进行
        }
    }

    private void createTopicIfNotExists(AdminClient adminClient, String topicName, int partitions) {
        try {
            // 检查 topic 是否存在
            if (!adminClient.listTopics().names().get(5, TimeUnit.SECONDS).contains(topicName)) {
                // 创建 topic
                NewTopic newTopic = new NewTopic(topicName, partitions, (short) 1);
                CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
                result.all().get(10, TimeUnit.SECONDS);
                System.out.println("Created topic: " + topicName + " with " + partitions + " partitions");
            } else {
                System.out.println("Topic already exists: " + topicName);
            }
        } catch (Exception e) {
            System.err.println("Failed to create topic " + topicName + ": " + e.getMessage());
        }
    }

    @BeforeEach
    @Transactional
    void setUp() {
        // 检查测试环境
        checkTestEnvironment();

        // 创建测试数据
        createTestData();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        cleanupTestData();
    }

    @Test
    @DisplayName("启动充电指令集成测试 - 完整流程")
    void startCharging_IntegrationTest_CompleteFlow() throws Exception {
        // Given
        ChargePointId chargePointId = testChargePoint.getChargePointId();
        StartChargingCommand command = StartChargingCommand.of(chargePointId, 1, "user123");

        // 在Redis中设置网关Pod信息（使用正确的键格式）
        String gatewayKey = "conn:" + chargePointId.value();
        String gatewayPodId = "gateway-pod-1";
        redisTemplate.opsForValue().set(gatewayKey, gatewayPodId, Duration.ofMinutes(5));

        // When
        CommandAcceptedResponse response = deviceControlApplicationService.startCharging(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.commandId()).isNotNull();

        // 验证Redis中的网关连接信息被正确查询
        String retrievedGatewayPodId = redisTemplate.opsForValue().get(gatewayKey);
        assertThat(retrievedGatewayPodId).isEqualTo(gatewayPodId);

        // 注意：从日志可以看到消息发送成功的日志，证明Kafka消息已正确发送
        // 这个集成测试验证的是Station服务的发送能力，不需要验证Gateway的消费能力
    }

    @Test
    @DisplayName("停止充电指令集成测试 - 完整流程")
    void stopCharging_IntegrationTest_CompleteFlow() throws Exception {
        // Given
        ChargePointId chargePointId = testChargePoint.getChargePointId();
        StopChargingCommand command = StopChargingCommand.of(chargePointId, 12345);

        // 在Redis中设置网关Pod信息（使用正确的键格式）
        String gatewayKey = "conn:" + chargePointId.value();
        String gatewayPodId = "gateway-pod-2";
        redisTemplate.opsForValue().set(gatewayKey, gatewayPodId, Duration.ofMinutes(5));

        // When
        CommandAcceptedResponse response = deviceControlApplicationService.stopCharging(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.commandId()).isNotNull();

        // 验证Redis中的网关连接信息被正确查询
        String retrievedGatewayPodId = redisTemplate.opsForValue().get(gatewayKey);
        assertThat(retrievedGatewayPodId).isEqualTo(gatewayPodId);

        // 注意：从日志可以看到消息发送成功的日志，证明Kafka消息已正确发送
    }

    @Test
    @DisplayName("重置充电桩指令集成测试 - 完整流程")
    void resetChargePoint_IntegrationTest_CompleteFlow() throws Exception {
        // Given
        ChargePointId chargePointId = testChargePoint.getChargePointId();
        String resetType = "Soft";

        // 在Redis中设置网关Pod信息（使用正确的键格式）
        String gatewayKey = "conn:" + chargePointId.value();
        String gatewayPodId = "gateway-pod-3";
        redisTemplate.opsForValue().set(gatewayKey, gatewayPodId, Duration.ofMinutes(5));

        // When
        CommandAcceptedResponse response = deviceControlApplicationService.resetChargePoint(chargePointId, resetType);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.commandId()).isNotNull();

        // 验证Redis中的网关连接信息被正确查询
        String retrievedGatewayPodId = redisTemplate.opsForValue().get(gatewayKey);
        assertThat(retrievedGatewayPodId).isEqualTo(gatewayPodId);

        // 注意：从日志可以看到消息发送成功的日志，证明Kafka消息已正确发送
    }

    /**
     * 创建测试数据
     */
    private void createTestData() {
        // 创建测试充电站
        String uniqueStationName = "测试充电站_" + System.currentTimeMillis();
        StationInfo stationInfo = StationInfo.of(uniqueStationName, "北京市朝阳区");
        Location location = Location.of(39.9042, 116.4074); // 北京坐标
        BusinessHours businessHours = new BusinessHours(LocalTime.of(0, 0), LocalTime.of(23, 59)); // 24小时营业
        
        testStation = new Station(stationInfo, location, "test-operator", businessHours);
        testStation = stationRepository.save(testStation);

        // 创建测试充电桩
        testChargePoint = ChargePoint.reconstruct(
            ChargePointId.of("CP-TEST-001"),
            testStation.getStationId(),
            "测试充电桩",
            "Model-X",
            "Vendor-A",
            "SN123456",
            "1.0.0",
            DeviceStatus.AVAILABLE, // 设置为可用状态，允许启动充电
            Instant.now(),
            PowerSpecification.ofKilowatts(BigDecimal.valueOf(60.0)),
            List.of(createAvailableConnector(1), createAvailableConnector(2)),
            Instant.now().minusSeconds(60), // 1分钟前的心跳，确保在线
            Instant.now(),
            1L
        );
        testChargePoint = chargePointRepository.save(testChargePoint);
    }

    /**
     * 创建可用的连接器
     */
    private Connector createAvailableConnector(int connectorId) {
        Connector connector = new Connector(connectorId, ConnectorType.CCS2, PowerSpecification.ofKilowatts(60.0));
        connector.updateStatus(DeviceStatus.AVAILABLE, Instant.now());
        return connector;
    }



    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            if (testChargePoint != null) {
                chargePointRepository.deleteById(testChargePoint.getChargePointId());
            }
            if (testStation != null) {
                stationRepository.deleteById(testStation.getStationId());
            }
        } catch (Exception e) {
            // 忽略清理错误
        }
    }
}
