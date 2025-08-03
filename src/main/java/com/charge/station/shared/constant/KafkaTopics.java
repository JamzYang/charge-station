package com.charge.station.shared.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kafka主题配置
 *
 * 从配置文件中读取 Kafka Topic 名称，避免硬编码
 *
 * @author 架构师团队
 * @version 1.0
 */
@Component
public class KafkaTopics {

    /**
     * 上行事件主题 - 网关上报的设备事件
     */
    @Value("${app.kafka.topics.ocpp-events-up}")
    public String OCPP_EVENTS_UP;

    /**
     * 下行指令主题 - 向网关发送的控制指令
     */
    @Value("${app.kafka.topics.commands-down}")
    public String COMMANDS_DOWN;

    /**
     * 集成事件主题 - 场站服务对外发布的集成事件
     */
    @Value("${app.kafka.topics.integration-events:station-integration-events}")
    public String INTEGRATION_EVENTS;
}
