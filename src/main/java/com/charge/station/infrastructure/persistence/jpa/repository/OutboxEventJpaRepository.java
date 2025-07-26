package com.charge.station.infrastructure.persistence.jpa.repository;

import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import com.charge.station.infrastructure.persistence.jpa.entity.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 发件箱事件JPA仓储
 *
 * 提供发件箱事件的数据访问操作。
 *
 * @author 架构师团队
 * @version 1.0
 */
@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
    
    /**
     * 查询待发布的事件（带悲观锁）
     * 
     * @param pageable 分页参数
     * @return 待发布的事件列表
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEventEntity e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<OutboxEventEntity> findPendingEventsWithLock(@Param("status") OutboxEventStatus status, Pageable pageable);
    
    /**
     * 查询待发布的事件（不带锁）
     * 
     * @param status 事件状态
     * @param pageable 分页参数
     * @return 待发布的事件列表
     */
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status, Pageable pageable);
    
    /**
     * 统计指定状态的事件数量
     * 
     * @param status 事件状态
     * @return 事件数量
     */
    long countByStatus(OutboxEventStatus status);
    
    /**
     * 删除指定时间之前的已发布事件
     * 
     * @param status 事件状态
     * @param createdBefore 创建时间之前
     * @return 删除的记录数
     */
    long deleteByStatusAndCreatedAtBefore(OutboxEventStatus status, Instant createdBefore);
    
    /**
     * 查询指定聚合的事件
     * 
     * @param aggregateType 聚合类型
     * @param aggregateId 聚合ID
     * @return 事件列表
     */
    List<OutboxEventEntity> findByAggregateTypeAndAggregateIdOrderByCreatedAtAsc(
        String aggregateType, String aggregateId);
}
