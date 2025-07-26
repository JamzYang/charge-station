package com.charge.station.infrastructure.cache;

import java.util.Optional;

/**
 * 缓存服务接口
 * 
 * 定义缓存的基本操作。
 * 
 * @author 架构师团队
 * @version 1.0
 */
public interface CacheService {

    /**
     * 从缓存中获取值
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param valueType 值类型
     * @param <T> 值类型
     * @return 缓存值，如果不存在则返回空
     */
    <T> Optional<T> get(String cacheName, String key, Class<T> valueType);

    /**
     * 将值放入缓存
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param value 缓存值
     */
    void put(String cacheName, String key, Object value);

    /**
     * 从缓存中删除值
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     */
    void evict(String cacheName, String key);

    /**
     * 清空指定缓存
     * 
     * @param cacheName 缓存名称
     */
    void clear(String cacheName);

    /**
     * 检查缓存中是否存在指定键
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @return true如果存在
     */
    boolean exists(String cacheName, String key);

    /**
     * 获取或设置缓存值
     * 如果缓存中存在则返回缓存值，否则执行supplier并将结果放入缓存
     * 
     * @param cacheName 缓存名称
     * @param key 缓存键
     * @param valueType 值类型
     * @param supplier 值提供者
     * @param <T> 值类型
     * @return 缓存值或新计算的值
     */
    <T> T getOrSet(String cacheName, String key, Class<T> valueType, java.util.function.Supplier<T> supplier);
}
