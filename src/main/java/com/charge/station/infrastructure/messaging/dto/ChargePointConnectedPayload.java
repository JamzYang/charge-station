package com.charge.station.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 充电桩连接事件载荷
 * 
 * 表示充电桩上线连接事件的具体载荷数据。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Data
@NoArgsConstructor
public class ChargePointConnectedPayload {
    
    /**
     * 充电桩型号
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * 厂商
     */
    @JsonProperty("vendor")
    private String vendor;
    
    /**
     * 固件版本
     */
    @JsonProperty("firmwareVersion")
    private String firmwareVersion;
    
    /**
     * 创建充电桩连接载荷
     * 
     * @param model 型号
     * @param vendor 厂商
     * @param firmwareVersion 固件版本
     */
    public ChargePointConnectedPayload(String model, String vendor, String firmwareVersion) {
        this.model = model;
        this.vendor = vendor;
        this.firmwareVersion = firmwareVersion;
    }
}
