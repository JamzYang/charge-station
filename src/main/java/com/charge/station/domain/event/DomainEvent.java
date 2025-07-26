package com.charge.station.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类
 * 
 * 定义所有领域事件的通用属性和行为。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public abstract class DomainEvent {
    
    private final UUID eventId;
    private final String eventType;
    private final Instant occurredOn;
    private final String aggregateId;

    protected DomainEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.occurredOn = Instant.now();
    }

    protected DomainEvent(String eventType, String aggregateId, Instant occurredOn) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.occurredOn = occurredOn;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", occurredOn=" + occurredOn +
                ", aggregateId='" + aggregateId + '\'' +
                '}';
    }
}
