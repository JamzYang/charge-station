package com.charge.station.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态变更事件载荷
 * 
 * 表示充电枪状态变更事件的具体载荷数据。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Data
@NoArgsConstructor
public class StatusChangedPayload {
    
    /**
     * 连接器ID
     */
    @JsonProperty("connectorId")
    private Integer connectorId;
    
    /**
     * 当前状态
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 之前状态
     */
    @JsonProperty("previousStatus")
    private String previousStatus;
    
    /**
     * 错误代码
     */
    @JsonProperty("errorCode")
    private String errorCode;
    
    /**
     * 创建状态变更载荷
     * 
     * @param connectorId 连接器ID
     * @param status 当前状态
     * @param previousStatus 之前状态
     * @param errorCode 错误代码
     */
    public StatusChangedPayload(Integer connectorId, String status, String previousStatus, String errorCode) {
        this.connectorId = connectorId;
        this.status = status;
        this.previousStatus = previousStatus;
        this.errorCode = errorCode;
    }
}
