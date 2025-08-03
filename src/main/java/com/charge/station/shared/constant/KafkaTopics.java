package com.charge.station.shared.constant;

/**
 * Kafka主题常量
 * 
 * @author 架构师团队
 * @version 1.0
 */
public final class KafkaTopics {
    
    /**
     * 上行事件主题 - 网关上报的设备事件
     */
    public static final String OCPP_EVENTS_UP = "ocpp-events-up-local";
    
    /**
     * 下行指令主题 - 向网关发送的控制指令
     */
    public static final String COMMANDS_DOWN = "commands-down-local";

    /**
     * 集成事件主题 - 场站服务对外发布的集成事件
     */
    public static final String INTEGRATION_EVENTS = "station-integration-events";

    private KafkaTopics() {
        // 工具类，禁止实例化
    }
}
