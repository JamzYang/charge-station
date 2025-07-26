package com.charge.station.infrastructure.external.gateway;

import com.charge.station.domain.model.chargepoint.ChargePointId;

import com.charge.station.shared.constant.CacheKeys;
import com.charge.station.shared.constant.KafkaTopics;
import com.charge.station.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GatewayCommandRouter 单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("网关指令路由器测试")
class GatewayCommandRouterTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @Mock
    private SendResult<String, String> sendResult;
    
    private ObjectMapper objectMapper;
    private GatewayCommandRouter gatewayCommandRouter;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 支持Java 8时间类型
        gatewayCommandRouter = new GatewayCommandRouter(redisTemplate, kafkaTemplate, objectMapper);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    @DisplayName("成功发送指令 - 设备在线")
    void sendCommand_Success_WhenDeviceOnline() throws Exception {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        String commandType = "RemoteStartTransaction";
        String payload = "test-payload"; // 使用简单字符串避免序列化问题
        String gatewayPodId = "gateway-pod-1";
        
        String expectedRedisKey = CacheKeys.DEVICE_CONNECTION_PREFIX + chargePointId.value();
        when(valueOperations.get(expectedRedisKey)).thenReturn(gatewayPodId);
        
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), anyString())).thenReturn(future);
        
        // When
        String commandId = gatewayCommandRouter.sendCommand(chargePointId, commandType, payload);
        
        // Then
        assertThat(commandId).isNotNull().isNotEmpty();
        
        // 验证Redis查询
        verify(valueOperations).get(expectedRedisKey);
        
        // 验证Kafka发送
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> partitionCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(kafkaTemplate).send(topicCaptor.capture(), partitionCaptor.capture(), 
                                  keyCaptor.capture(), messageCaptor.capture());
        
        assertThat(topicCaptor.getValue()).isEqualTo(KafkaTopics.COMMANDS_DOWN);
        assertThat(partitionCaptor.getValue()).isBetween(0, 127); // 0-127分区
        assertThat(keyCaptor.getValue()).isEqualTo(chargePointId.value());
        
        // 验证消息内容
        String message = messageCaptor.getValue();
        assertThat(message).contains(commandId);
        assertThat(message).contains(commandType);
        assertThat(message).contains(chargePointId.value());
    }
    
    @Test
    @DisplayName("发送指令失败 - 设备离线")
    void sendCommand_ThrowsException_WhenDeviceOffline() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        String commandType = "RemoteStartTransaction";
        String payload = "test-payload";
        
        String expectedRedisKey = CacheKeys.DEVICE_CONNECTION_PREFIX + chargePointId.value();
        when(valueOperations.get(expectedRedisKey)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> gatewayCommandRouter.sendCommand(chargePointId, commandType, payload))
            .isInstanceOf(BusinessException.class)
            .hasMessage("设备不在线或连接信息不存在");
        
        verify(valueOperations).get(expectedRedisKey);
        verifyNoInteractions(kafkaTemplate);
    }
    
    @Test
    @DisplayName("发送指令失败 - 设备连接信息为空")
    void sendCommand_ThrowsException_WhenConnectionInfoEmpty() {
        // Given
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        String commandType = "RemoteStartTransaction";
        String payload = "test-payload";
        
        String expectedRedisKey = CacheKeys.DEVICE_CONNECTION_PREFIX + chargePointId.value();
        when(valueOperations.get(expectedRedisKey)).thenReturn("");
        
        // When & Then
        assertThatThrownBy(() -> gatewayCommandRouter.sendCommand(chargePointId, commandType, payload))
            .isInstanceOf(BusinessException.class)
            .hasMessage("设备不在线或连接信息不存在");
        
        verify(valueOperations).get(expectedRedisKey);
        verifyNoInteractions(kafkaTemplate);
    }
    
    @Test
    @DisplayName("分区计算一致性测试")
    void calculatePartition_ConsistentResults() {
        // Given
        String gatewayPodId = "gateway-pod-1";
        ChargePointId chargePointId = ChargePointId.of("CP12345");
        String commandType = "RemoteStartTransaction";
        String payload = "test-payload";
        
        String expectedRedisKey = CacheKeys.DEVICE_CONNECTION_PREFIX + chargePointId.value();
        when(valueOperations.get(expectedRedisKey)).thenReturn(gatewayPodId);
        
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyInt(), anyString(), anyString())).thenReturn(future);
        
        // When - 多次调用同一个gatewayPodId
        gatewayCommandRouter.sendCommand(chargePointId, commandType, payload);
        gatewayCommandRouter.sendCommand(chargePointId, commandType, payload);
        gatewayCommandRouter.sendCommand(chargePointId, commandType, payload);
        
        // Then - 验证分区计算结果一致
        ArgumentCaptor<Integer> partitionCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(kafkaTemplate, times(3)).send(anyString(), partitionCaptor.capture(), anyString(), anyString());
        
        var partitions = partitionCaptor.getAllValues();
        assertThat(partitions).hasSize(3);
        assertThat(partitions.get(0)).isEqualTo(partitions.get(1)).isEqualTo(partitions.get(2));
        assertThat(partitions.get(0)).isBetween(0, 127);
    }
}
