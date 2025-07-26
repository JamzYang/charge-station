package com.charge.station.application.handler;

import com.charge.station.application.service.DomainEventPublishingService;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.infrastructure.cache.CacheService;
import com.charge.station.infrastructure.messaging.dto.ChargePointConnectedPayload;
import com.charge.station.infrastructure.messaging.dto.GatewayEventDTO;
import com.charge.station.infrastructure.messaging.dto.StatusChangedPayload;
import com.charge.station.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 设备状态事件处理器
 *
 * 处理来自充电桩网关的设备状态相关事件，更新设备状态并发布领域事件。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class DeviceStatusEventHandler {

    private static final Logger log = LoggerFactory.getLogger(DeviceStatusEventHandler.class);

    private final ChargePointRepository chargePointRepository;
    private final DomainEventPublishingService domainEventPublishingService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    public DeviceStatusEventHandler(ChargePointRepository chargePointRepository,
                                   DomainEventPublishingService domainEventPublishingService,
                                   CacheService cacheService,
                                   ObjectMapper objectMapper) {
        this.chargePointRepository = chargePointRepository;
        this.domainEventPublishingService = domainEventPublishingService;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 处理充电枪状态变更事件
     * 
     * @param event 网关事件
     */
    @Transactional
    public void handleStatusNotification(GatewayEventDTO<?> event) {
        try {
            log.info("处理状态变更事件: eventId={}, chargePointId={}", event.getEventId(), event.getChargePointId());
            
            // 1. 事件数据校验
            if (event.getChargePointId() == null || event.getPayload() == null) {
                log.warn("接收到无效的状态通知事件: eventId={}", event.getEventId());
                return;
            }
            
            // 2. 解析载荷
            StatusChangedPayload payload = parsePayload(event.getPayload(), StatusChangedPayload.class);
            if (payload == null) {
                log.warn("解析状态变更载荷失败: eventId={}", event.getEventId());
                return;
            }
            
            // 3. 查找充电桩聚合
            ChargePointId chargePointId = ChargePointId.of(event.getChargePointId());
            ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
                .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
            
            // 4. 更新充电桩状态
            DeviceStatus oldStatus = chargePoint.getStatus();
            DeviceStatus newStatus = DeviceStatus.fromOcppStatus(payload.getStatus());
            
            log.info("更新充电桩状态: chargePointId={}, oldStatus={}, newStatus={}", 
                chargePointId, oldStatus, newStatus);
            
            chargePoint.updateStatus(newStatus, event.getTimestamp());
            
            // 5. 持久化状态变更
            chargePointRepository.save(chargePoint);
            
            // 6. 发布领域事件
            domainEventPublishingService.publishDomainEvents(chargePoint);
            
            // 7. 清除缓存
            evictChargePointCache(chargePointId);
            
            log.info("状态变更事件处理完成: chargePointId={}, newStatus={}", chargePointId, newStatus);
            
        } catch (Exception e) {
            log.error("处理状态变更事件失败: eventId={}, chargePointId={}", 
                event.getEventId(), event.getChargePointId(), e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
    
    /**
     * 处理充电桩连接事件
     * 
     * @param event 网关事件
     */
    @Transactional
    public void handleChargePointConnected(GatewayEventDTO<?> event) {
        try {
            log.info("处理充电桩连接事件: eventId={}, chargePointId={}", event.getEventId(), event.getChargePointId());
            
            // 1. 事件数据校验
            if (event.getChargePointId() == null || event.getPayload() == null) {
                log.warn("接收到无效的充电桩连接事件: eventId={}", event.getEventId());
                return;
            }
            
            // 2. 解析载荷
            ChargePointConnectedPayload payload = parsePayload(event.getPayload(), ChargePointConnectedPayload.class);
            if (payload == null) {
                log.warn("解析充电桩连接载荷失败: eventId={}", event.getEventId());
                return;
            }
            
            // 3. 查找充电桩聚合
            ChargePointId chargePointId = ChargePointId.of(event.getChargePointId());
            ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
                .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
            
            // 4. 更新充电桩信息
            if (payload.getFirmwareVersion() != null) {
                chargePoint.updateFirmwareVersion(payload.getFirmwareVersion());
            }
            
            // 5. 更新心跳时间
            chargePoint.updateHeartbeat(event.getTimestamp());
            
            // 6. 如果充电桩之前是离线状态，更新为可用状态
            if (chargePoint.getStatus() == DeviceStatus.OFFLINE) {
                chargePoint.updateStatus(DeviceStatus.AVAILABLE, event.getTimestamp());
            }
            
            // 7. 持久化变更
            chargePointRepository.save(chargePoint);
            
            // 8. 发布领域事件
            domainEventPublishingService.publishDomainEvents(chargePoint);
            
            // 9. 清除缓存
            evictChargePointCache(chargePointId);
            
            log.info("充电桩连接事件处理完成: chargePointId={}", chargePointId);
            
        } catch (Exception e) {
            log.error("处理充电桩连接事件失败: eventId={}, chargePointId={}", 
                event.getEventId(), event.getChargePointId(), e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
    
    /**
     * 处理充电桩断开连接事件
     * 
     * @param event 网关事件
     */
    @Transactional
    public void handleChargePointDisconnected(GatewayEventDTO<?> event) {
        try {
            log.info("处理充电桩断开连接事件: eventId={}, chargePointId={}", event.getEventId(), event.getChargePointId());
            
            // 1. 事件数据校验
            if (event.getChargePointId() == null) {
                log.warn("接收到无效的充电桩断开连接事件: eventId={}", event.getEventId());
                return;
            }
            
            // 2. 查找充电桩聚合
            ChargePointId chargePointId = ChargePointId.of(event.getChargePointId());
            ChargePoint chargePoint = chargePointRepository.findById(chargePointId)
                .orElseThrow(() -> new ResourceNotFoundException("充电桩不存在: " + chargePointId));
            
            // 3. 更新充电桩状态为离线
            chargePoint.updateStatus(DeviceStatus.OFFLINE, event.getTimestamp());
            
            // 4. 持久化状态变更
            chargePointRepository.save(chargePoint);
            
            // 5. 发布领域事件
            domainEventPublishingService.publishDomainEvents(chargePoint);
            
            // 6. 清除缓存
            evictChargePointCache(chargePointId);
            
            log.info("充电桩断开连接事件处理完成: chargePointId={}", chargePointId);
            
        } catch (Exception e) {
            log.error("处理充电桩断开连接事件失败: eventId={}, chargePointId={}", 
                event.getEventId(), event.getChargePointId(), e);
            throw e; // 重新抛出异常，让事务回滚
        }
    }
    
    /**
     * 解析事件载荷
     * 
     * @param payload 原始载荷对象
     * @param targetClass 目标类型
     * @return 解析后的载荷对象
     */
    private <T> T parsePayload(Object payload, Class<T> targetClass) {
        try {
            if (payload == null) {
                return null;
            }
            
            // 如果已经是目标类型，直接返回
            if (targetClass.isInstance(payload)) {
                return targetClass.cast(payload);
            }
            
            // 否则通过JSON序列化/反序列化转换
            String json = objectMapper.writeValueAsString(payload);
            return objectMapper.readValue(json, targetClass);
            
        } catch (Exception e) {
            log.error("解析事件载荷失败: targetClass={}", targetClass.getSimpleName(), e);
            return null;
        }
    }
    
    /**
     * 清除充电桩缓存
     * 
     * @param chargePointId 充电桩ID
     */
    private void evictChargePointCache(ChargePointId chargePointId) {
        try {
            // 这里需要根据实际的缓存键格式来清除缓存
            // 暂时使用简单的实现
            log.debug("清除充电桩缓存: chargePointId={}", chargePointId);
            // TODO: 实现具体的缓存清除逻辑
        } catch (Exception e) {
            log.warn("清除充电桩缓存失败: chargePointId={}", chargePointId, e);
            // 缓存清除失败不应该影响主要业务逻辑
        }
    }
}
