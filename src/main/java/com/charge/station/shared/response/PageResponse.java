package com.charge.station.shared.response;

import java.util.List;

/**
 * 分页响应格式
 * 
 * @param <T> 数据类型
 * @author 架构师团队
 * @version 1.0
 */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious
) {
    
    /**
     * 创建分页响应
     * 
     * @param content 数据内容
     * @param page 当前页码（从0开始）
     * @param size 每页大小
     * @param totalElements 总元素数
     * @param <T> 数据类型
     * @return 分页响应
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1;
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        return new PageResponse<>(
            content, page, size, totalElements, totalPages,
            first, last, hasNext, hasPrevious
        );
    }

    /**
     * 创建空的分页响应
     * 
     * @param page 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 空的分页响应
     */
    public static <T> PageResponse<T> empty(int page, int size) {
        return of(List.of(), page, size, 0);
    }
}
