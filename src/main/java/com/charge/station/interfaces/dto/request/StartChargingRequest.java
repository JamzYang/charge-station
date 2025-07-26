package com.charge.station.interfaces.dto.request;

import jakarta.validation.constraints.*;

/**
 * 启动充电请求DTO
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record StartChargingRequest(
    @NotNull(message = "连接器ID不能为空")
    @Min(value = 1, message = "连接器ID必须大于0")
    @Max(value = 10, message = "连接器ID不能超过10")
    Integer connectorId,
    
    @NotBlank(message = "用户标识不能为空")
    @Size(max = 20, message = "用户标识长度不能超过20字符")
    String idTag
) {
    
    /**
     * 创建启动充电请求
     * 
     * @param connectorId 连接器ID
     * @param idTag 用户标识
     * @return StartChargingRequest实例
     */
    public static StartChargingRequest of(Integer connectorId, String idTag) {
        return new StartChargingRequest(connectorId, idTag);
    }
}
