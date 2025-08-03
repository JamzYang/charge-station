package com.charge.station.infrastructure.external.gateway;

import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.infrastructure.messaging.dto.GatewayCommandDTO;
import com.charge.station.shared.constant.CacheKeys;
import com.charge.station.shared.constant.KafkaTopics;
import com.charge.station.shared.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 网关指令路由器
 * 
 * 负责向网关发送指令的复杂逻辑，包括连接查询、分区计算和精确发送，
 * 严格遵循《网关集成手册》规范。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayCommandRouter {
    
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 与网关约定的总分区数
     */
    @Value("${app.kafka.partitions.total}")
    private int TOTAL_PARTITIONS;
    
    /**
     * 发送指令到网关
     * 
     * @param chargePointId 充电桩ID
     * @param commandType 指令类型
     * @param payload 指令载荷
     * @return 指令ID
     * @throws BusinessException 当设备不在线或发送失败时
     */
    public String sendCommand(ChargePointId chargePointId, String commandType, Object payload) {
        return sendCommand(chargePointId, commandType, payload, null);
    }
    
    /**
     * 发送指令到网关（带追踪ID）
     * 
     * @param chargePointId 充电桩ID
     * @param commandType 指令类型
     * @param payload 指令载荷
     * @param traceId 追踪ID
     * @return 指令ID
     * @throws BusinessException 当设备不在线或发送失败时
     */
    public String sendCommand(ChargePointId chargePointId, String commandType, Object payload, String traceId) {
        try {
            // 步骤1: 查询连接映射
            String redisKey = CacheKeys.DEVICE_CONNECTION_PREFIX + chargePointId.value();
            String gatewayPodId = redisTemplate.opsForValue().get(redisKey);
            
            if (gatewayPodId == null || gatewayPodId.isEmpty()) {
                log.warn("设备不在线或连接信息不存在: chargePointId={}", chargePointId.value());
                throw new BusinessException("设备不在线或连接信息不存在");
            }
            
            // 步骤2: 计算目标分区
            int partitionId = calculatePartition(gatewayPodId);
            
            // 步骤3: 构建指令并发送到指定分区
            String commandId = UUID.randomUUID().toString();
            GatewayCommandDTO commandDTO = GatewayCommandDTO.of(
                commandId, commandType, chargePointId.value(), payload, traceId
            );
            
            String message = objectMapper.writeValueAsString(commandDTO);
            
            // 精确发送到目标分区
            kafkaTemplate.send(KafkaTopics.COMMANDS_DOWN, partitionId, chargePointId.value(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("指令发送失败: commandId={}, chargePointId={}, partition={}",
                            commandId, chargePointId.value(), partitionId, ex);
                    } else {
                        log.info("指令发送成功: commandId={}, chargePointId={}, partition={}, offset={}",
                            commandId, chargePointId.value(), partitionId,
                            result.getRecordMetadata().offset());
                    }
                });
            
            log.info("指令已发送: commandId={}, chargePointId={}, commandType={}, partition={}",
                commandId, chargePointId.value(), commandType, partitionId);
            
            return commandId;
            
        } catch (BusinessException e) {
            // 直接重新抛出业务异常，不包装
            throw e;
        } catch (JsonProcessingException e) {
            log.error("指令序列化失败: chargePointId={}, commandType={}",
                chargePointId.value(), commandType, e);
            throw new BusinessException("指令序列化失败", e);
        } catch (Exception e) {
            log.error("指令发送异常: chargePointId={}, commandType={}",
                chargePointId.value(), commandType, e);
            throw new BusinessException("指令发送失败", e);
        }
    }
    
    /**
     * 计算分区ID
     * 
     * 使用与网关团队约定的稳定哈希算法（FNV-1a）
     * 
     * @param key 分区键（网关Pod ID）
     * @return 分区ID
     */
    private int calculatePartition(String key) {
        // 使用FNV-1a哈希算法
        long hash = fnv1aHash64(key.getBytes(StandardCharsets.UTF_8));
        return (int) (Math.abs(hash) % TOTAL_PARTITIONS);
    }
    
    /**
     * FNV-1a 64位哈希算法实现
     * 
     * @param data 待哈希的数据
     * @return 哈希值
     */
    private long fnv1aHash64(byte[] data) {
        final long FNV_64_INIT = 0xcbf29ce484222325L;
        final long FNV_64_PRIME = 0x100000001b3L;
        
        long hash = FNV_64_INIT;
        for (byte b : data) {
            hash ^= (b & 0xff);
            hash *= FNV_64_PRIME;
        }
        return hash;
    }
}
