/**
 * 基础设施层 (Infrastructure Layer)
 * 
 * 职责：
 * - 技术实现，数据持久化
 * - 外部集成（如分区路由）
 * - 消息队列、缓存等技术组件
 * 
 * 关键原则：
 * - 可替换的技术实现
 * - 实现领域层定义的接口
 * - 处理技术细节和外部依赖
 * 
 * 包含子包：
 * - persistence: 持久化实现
 * - messaging: 消息处理
 * - external: 外部服务集成
 */
package com.charge.station.infrastructure;
