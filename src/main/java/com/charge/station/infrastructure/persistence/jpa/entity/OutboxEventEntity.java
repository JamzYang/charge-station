package com.charge.station.infrastructure.persistence.jpa.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * 发件箱事件实体
 * 
 * 用于实现事务性发件箱模式，确保领域事件与业务操作的原子性。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Entity
@Table(name = "outbox_events", indexes = {
    @Index(name = "idx_outbox_events_status_created_at", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;
    
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "payload", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    /**
     * 创建新的发件箱事件
     */
    public OutboxEventEntity(String aggregateType, String aggregateId, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxEventStatus.PENDING;
        this.createdAt = Instant.now();
    }
    
    /**
     * 标记为已发布
     */
    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
    }
    
    /**
     * 标记为失败
     */
    public void markAsFailed() {
        this.status = OutboxEventStatus.FAILED;
    }
    
    /**
     * 检查是否为待发布状态
     */
    public boolean isPending() {
        return this.status == OutboxEventStatus.PENDING;
    }
}
