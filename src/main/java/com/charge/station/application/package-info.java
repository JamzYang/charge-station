/**
 * 应用层 (Application Layer)
 * 
 * 职责：
 * - 编排业务流程，事务管理
 * - 调用领域服务，处理应用事件
 * - 协调多个聚合根的操作
 * 
 * 关键原则：
 * - 无状态，协调者角色
 * - 不包含业务规则，只负责流程编排
 * - 管理事务边界
 * 
 * 包含子包：
 * - service: 应用服务
 * - command: 命令对象
 * - handler: 事件处理器
 */
package com.charge.station.application;
