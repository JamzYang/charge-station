package com.charge.station.application.service;

import com.charge.station.domain.event.DomainEvent;
import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.station.Station;
import com.charge.station.infrastructure.messaging.publisher.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 领域事件发布服务
 *
 * 负责从聚合根中收集领域事件并发布到发件箱。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Service
public class DomainEventPublishingService {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublishingService.class);

    private final DomainEventPublisher domainEventPublisher;

    public DomainEventPublishingService(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }
    
    /**
     * 发布充电站的领域事件
     * 
     * @param station 充电站聚合根
     */
    @Transactional
    public void publishDomainEvents(Station station) {
        if (station == null || !station.hasDomainEvents()) {
            return;
        }
        
        List<Object> domainEvents = station.getDomainEventsAndClear();
        
        for (Object event : domainEvents) {
            if (event instanceof DomainEvent domainEvent) {
                domainEventPublisher.publish(domainEvent);
                log.debug("发布充电站领域事件: eventType={}, aggregateId={}", 
                    domainEvent.getEventType(), domainEvent.getAggregateId());
            } else {
                log.warn("发现非DomainEvent类型的事件: {}", event.getClass().getName());
            }
        }
    }
    
    /**
     * 发布充电桩的领域事件
     * 
     * @param chargePoint 充电桩聚合根
     */
    @Transactional
    public void publishDomainEvents(ChargePoint chargePoint) {
        if (chargePoint == null || !chargePoint.hasDomainEvents()) {
            return;
        }
        
        List<Object> domainEvents = chargePoint.getDomainEventsAndClear();
        
        for (Object event : domainEvents) {
            if (event instanceof DomainEvent domainEvent) {
                domainEventPublisher.publish(domainEvent);
                log.debug("发布充电桩领域事件: eventType={}, aggregateId={}", 
                    domainEvent.getEventType(), domainEvent.getAggregateId());
            } else {
                log.warn("发现非DomainEvent类型的事件: {}", event.getClass().getName());
            }
        }
    }
    
    /**
     * 批量发布充电桩的领域事件
     * 
     * @param chargePoints 充电桩列表
     */
    @Transactional
    public void publishDomainEvents(List<ChargePoint> chargePoints) {
        if (chargePoints == null || chargePoints.isEmpty()) {
            return;
        }
        
        for (ChargePoint chargePoint : chargePoints) {
            publishDomainEvents(chargePoint);
        }
    }
}
