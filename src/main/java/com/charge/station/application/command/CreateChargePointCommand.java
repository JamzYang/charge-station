package com.charge.station.application.command;

import com.charge.station.domain.model.station.StationId;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建充电桩命令
 *
 * 封装创建充电桩所需的所有参数。
 *
 * @author 架构师团队
 * @version 1.0
 */
public record CreateChargePointCommand(
    StationId stationId,
    String name,
    String model,
    String vendor,
    String serialNumber,
    BigDecimal maxPower,
    List<ConnectorConfig> connectors
) {

    /**
     * 充电枪配置
     */
    public record ConnectorConfig(
        String connectorType,
        BigDecimal maxPower
    ) {

        /**
         * 创建充电枪配置
         *
         * @param connectorType 连接器类型
         * @param maxPower 最大功率
         * @return ConnectorConfig实例
         */
        public static ConnectorConfig of(String connectorType, BigDecimal maxPower) {
            return new ConnectorConfig(connectorType, maxPower);
        }

        /**
         * 创建充电枪配置（double版本）
         *
         * @param connectorType 连接器类型
         * @param maxPower 最大功率
         * @return ConnectorConfig实例
         */
        public static ConnectorConfig of(String connectorType, double maxPower) {
            return new ConnectorConfig(connectorType, BigDecimal.valueOf(maxPower));
        }
    }

    /**
     * 创建创建充电桩命令
     *
     * @param stationId 所属充电站ID
     * @param name 充电桩名称
     * @param model 型号
     * @param vendor 厂商
     * @param serialNumber 序列号
     * @param maxPower 最大功率
     * @param connectors 充电枪配置列表
     * @return CreateChargePointCommand实例
     */
    public static CreateChargePointCommand of(StationId stationId, String name, String model,
                                            String vendor, String serialNumber, BigDecimal maxPower,
                                            List<ConnectorConfig> connectors) {
        return new CreateChargePointCommand(stationId, name, model, vendor, serialNumber, maxPower, connectors);
    }

    /**
     * 创建创建充电桩命令（简化版本）
     *
     * @param stationId 所属充电站ID
     * @param name 充电桩名称
     * @param serialNumber 序列号
     * @param maxPower 最大功率
     * @param connectors 充电枪配置列表
     * @return CreateChargePointCommand实例
     */
    public static CreateChargePointCommand of(StationId stationId, String name,
                                            String serialNumber, BigDecimal maxPower,
                                            List<ConnectorConfig> connectors) {
        return new CreateChargePointCommand(stationId, name, null, null, serialNumber, maxPower, connectors);
    }
}
