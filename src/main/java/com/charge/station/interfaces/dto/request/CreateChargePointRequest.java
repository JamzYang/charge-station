package com.charge.station.interfaces.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建充电桩请求DTO
 *
 * @author 架构师团队
 * @version 1.0
 */
public record CreateChargePointRequest(
    @NotBlank(message = "充电桩名称不能为空")
    @Size(max = 100, message = "充电桩名称长度不能超过100字符")
    String name,

    @Size(max = 50, message = "型号长度不能超过50字符")
    String model,

    @Size(max = 50, message = "厂商长度不能超过50字符")
    String vendor,

    @Size(max = 100, message = "序列号长度不能超过100字符")
    String serialNumber,

    @NotNull(message = "最大功率不能为空")
    @DecimalMin(value = "0.1", message = "最大功率必须大于0")
    @DecimalMax(value = "500.0", message = "最大功率不能超过500kW")
    @Digits(integer = 3, fraction = 2, message = "最大功率格式不正确")
    BigDecimal maxPower,

    @NotEmpty(message = "充电枪配置不能为空")
    @Size(min = 1, max = 10, message = "充电枪数量必须在1-10个之间")
    @Valid
    List<ConnectorConfig> connectors
) {

    /**
     * 充电枪配置内嵌记录
     */
    public record ConnectorConfig(
        @NotBlank(message = "连接器类型不能为空")
        @Pattern(regexp = "^(GB_DC|GB_AC|TYPE2|CCS1|CCS2|CHADEMO|TESLA)$",
                message = "连接器类型必须是: GB_DC, GB_AC, TYPE2, CCS1, CCS2, CHADEMO, TESLA")
        String connectorType,

        @NotNull(message = "连接器功率不能为空")
        @DecimalMin(value = "0.1", message = "连接器功率必须大于0")
        @DecimalMax(value = "500.0", message = "连接器功率不能超过500kW")
        @Digits(integer = 3, fraction = 2, message = "连接器功率格式不正确")
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
     * 创建创建充电桩请求
     *
     * @param name 充电桩名称
     * @param model 型号
     * @param vendor 厂商
     * @param serialNumber 序列号
     * @param maxPower 最大功率
     * @param connectors 充电枪配置列表
     * @return CreateChargePointRequest实例
     */
    public static CreateChargePointRequest of(String name, String model, String vendor,
                                            String serialNumber, BigDecimal maxPower,
                                            List<ConnectorConfig> connectors) {
        return new CreateChargePointRequest(name, model, vendor, serialNumber, maxPower, connectors);
    }

    /**
     * 创建创建充电桩请求（简化版本）
     *
     * @param name 充电桩名称
     * @param serialNumber 序列号
     * @param maxPower 最大功率
     * @param connectors 充电枪配置列表
     * @return CreateChargePointRequest实例
     */
    public static CreateChargePointRequest of(String name, String serialNumber, BigDecimal maxPower,
                                            List<ConnectorConfig> connectors) {
        return new CreateChargePointRequest(name, null, null, serialNumber, maxPower, connectors);
    }

    /**
     * 创建双枪直流快充桩请求
     *
     * @param name 充电桩名称
     * @param serialNumber 序列号
     * @param totalPower 总功率
     * @return CreateChargePointRequest实例，包含2个国标直流快充枪
     */
    public static CreateChargePointRequest ofDualDcFastCharger(String name, String serialNumber,
                                                              BigDecimal totalPower) {
        // 双枪平分功率
        BigDecimal connectorPower = totalPower.divide(BigDecimal.valueOf(2));

        List<ConnectorConfig> connectors = List.of(
            ConnectorConfig.of("GB_DC", connectorPower),
            ConnectorConfig.of("GB_DC", connectorPower)
        );

        return new CreateChargePointRequest(name, null, null, serialNumber, totalPower, connectors);
    }

    /**
     * 创建双枪直流快充桩请求（double版本）
     *
     * @param name 充电桩名称
     * @param serialNumber 序列号
     * @param totalPower 总功率
     * @return CreateChargePointRequest实例，包含2个国标直流快充枪
     */
    public static CreateChargePointRequest ofDualDcFastCharger(String name, String serialNumber,
                                                              double totalPower) {
        return ofDualDcFastCharger(name, serialNumber, BigDecimal.valueOf(totalPower));
    }
}
