/**
 * 领域层 (Domain Layer)
 * 
 * 职责：
 * - 核心业务逻辑，领域规则
 * - 领域事件的定义和发布
 * - 聚合根和实体的业务行为
 * 
 * 关键原则：
 * - 与技术无关，纯业务逻辑
 * - 包含所有业务规则和约束
 * - 领域模型的完整性和一致性
 * 
 * 包含子包：
 * - model: 领域模型（聚合根、实体、值对象）
 * - event: 领域事件
 * - repository: 仓储接口
 * - service: 领域服务
 */
package com.charge.station.domain;
