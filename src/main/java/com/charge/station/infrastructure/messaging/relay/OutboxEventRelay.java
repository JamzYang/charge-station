package com.charge.station.infrastructure.messaging.relay;

import com.charge.station.infrastructure.messaging.event.IntegrationEvent;
import com.charge.station.infrastructure.messaging.kafka.producer.IntegrationEventProducer;
import com.charge.station.infrastructure.messaging.mapper.IntegrationEventMapper;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventStatus;
import com.charge.station.infrastructure.persistence.jpa.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 发件箱事件中继器
 * 
 * 负责可靠地将发件箱中的事件发布到Kafka。
 * 实现事务性发件箱模式的核心组件。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventRelay {
    
    private final OutboxEventJpaRepository outboxRepository;
    private final IntegrationEventProducer kafkaProducer;
    private final IntegrationEventMapper eventMapper;
    
    private static final int BATCH_SIZE = 100;
    private static final int CLEANUP_DAYS = 7; // 清理7天前的已发布事件
    
    /**
     * 定期轮询并发布待发布的事件
     * 
     * 每5秒执行一次，查询待发布的事件并发送到Kafka。
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relayEvents() {
        try {
            // 查询待发布的事件，使用悲观锁防止多实例并发处理
            List<OutboxEventEntity> pendingEvents = outboxRepository.findPendingEventsWithLock(
                OutboxEventStatus.PENDING, 
                PageRequest.of(0, BATCH_SIZE)
            );
            
            if (pendingEvents.isEmpty()) {
                return;
            }
            
            log.info("发现 {} 条待发布事件，开始处理...", pendingEvents.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (OutboxEventEntity eventEntity : pendingEvents) {
                try {
                    // 转换为集成事件并发布
                    IntegrationEvent integrationEvent = eventMapper.fromOutboxEvent(eventEntity);
                    kafkaProducer.send(integrationEvent);
                    
                    // 更新事件状态为已发布
                    eventEntity.markAsPublished();
                    outboxRepository.save(eventEntity);
                    
                    successCount++;
                    
                    log.debug("事件发布成功: eventId={}, eventType={}", 
                        eventEntity.getId(), eventEntity.getEventType());
                        
                } catch (Exception e) {
                    log.error("发布事件失败: eventId={}, eventType={}", 
                        eventEntity.getId(), eventEntity.getEventType(), e);
                    
                    // 标记为失败状态
                    eventEntity.markAsFailed();
                    outboxRepository.save(eventEntity);
                    
                    failureCount++;
                }
            }
            
            log.info("事件发布完成: 成功={}, 失败={}", successCount, failureCount);
            
        } catch (Exception e) {
            log.error("事件中继器执行失败", e);
        }
    }
    
    /**
     * 定期清理已发布的事件
     * 
     * 每天凌晨2点执行，清理7天前的已发布事件。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupPublishedEvents() {
        try {
            Instant cutoffTime = Instant.now().minusSeconds(CLEANUP_DAYS * 24 * 60 * 60);
            
            long deletedCount = outboxRepository.deleteByStatusAndCreatedAtBefore(
                OutboxEventStatus.PUBLISHED, 
                cutoffTime
            );
            
            if (deletedCount > 0) {
                log.info("清理已发布事件完成: 删除了 {} 条记录", deletedCount);
            }
            
        } catch (Exception e) {
            log.error("清理已发布事件失败", e);
        }
    }
    
    /**
     * 获取待发布事件统计
     * 
     * @return 待发布事件数量
     */
    public long getPendingEventCount() {
        return outboxRepository.countByStatus(OutboxEventStatus.PENDING);
    }
    
    /**
     * 获取失败事件统计
     * 
     * @return 失败事件数量
     */
    public long getFailedEventCount() {
        return outboxRepository.countByStatus(OutboxEventStatus.FAILED);
    }
}
