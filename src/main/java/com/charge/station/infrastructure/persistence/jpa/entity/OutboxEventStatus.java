package com.charge.station.infrastructure.persistence.jpa.entity;

/**
 * 发件箱事件状态枚举
 * 
 * 定义发件箱事件的生命周期状态。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public enum OutboxEventStatus {
    
    /**
     * 待发布状态
     * 事件已创建但尚未发布到消息队列
     */
    PENDING,
    
    /**
     * 已发布状态
     * 事件已成功发布到消息队列
     */
    PUBLISHED,
    
    /**
     * 发布失败状态
     * 事件发布到消息队列时失败
     */
    FAILED
}
