package com.charge.station.interfaces.dto.response;

/**
 * 指令接受响应DTO
 * 
 * 用于异步指令发送的响应，表示指令已被接受并正在处理。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public record CommandAcceptedResponse(
    String commandId,
    String status,
    String message
) {
    
    /**
     * 创建指令接受响应
     * 
     * @param commandId 指令ID
     * @param status 状态
     * @param message 消息
     * @return CommandAcceptedResponse实例
     */
    public static CommandAcceptedResponse of(String commandId, String status, String message) {
        return new CommandAcceptedResponse(commandId, status, message);
    }
    
    /**
     * 创建成功接受的响应
     * 
     * @param commandId 指令ID
     * @return CommandAcceptedResponse实例
     */
    public static CommandAcceptedResponse accepted(String commandId) {
        return new CommandAcceptedResponse(commandId, "ACCEPTED", "指令已接受，正在处理中");
    }
}
