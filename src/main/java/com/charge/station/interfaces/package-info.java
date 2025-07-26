/**
 * 接口层 (Interfaces Layer)
 * 
 * 职责：
 * - 处理HTTP请求，参数校验，DTO转换
 * - 消费上行事件
 * - 提供REST API接口
 * 
 * 关键原则：
 * - 薄薄一层，不包含业务逻辑
 * - 只负责协议转换和数据校验
 * - 调用应用层服务处理业务逻辑
 * 
 * 包含子包：
 * - rest: REST控制器
 * - dto: 数据传输对象
 * - assembler: DTO转换器
 */
package com.charge.station.interfaces;
