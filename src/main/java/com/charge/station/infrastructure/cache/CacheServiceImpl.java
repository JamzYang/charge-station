package com.charge.station.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 缓存服务实现
 * 
 * 基于Spring Cache的缓存服务实现。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> valueType) {
        Objects.requireNonNull(cacheName, "缓存名称不能为空");
        Objects.requireNonNull(key, "缓存键不能为空");
        Objects.requireNonNull(valueType, "值类型不能为空");

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                log.warn("缓存不存在: {}", cacheName);
                return Optional.empty();
            }

            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper == null) {
                log.debug("缓存未命中: cacheName={}, key={}", cacheName, key);
                return Optional.empty();
            }

            Object value = wrapper.get();
            if (value == null) {
                log.debug("缓存值为null: cacheName={}, key={}", cacheName, key);
                return Optional.empty();
            }

            if (valueType.isInstance(value)) {
                log.debug("缓存命中: cacheName={}, key={}", cacheName, key);
                return Optional.of(valueType.cast(value));
            } else {
                log.warn("缓存值类型不匹配: cacheName={}, key={}, expected={}, actual={}", 
                    cacheName, key, valueType.getName(), value.getClass().getName());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取缓存值失败: cacheName={}, key={}", cacheName, key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String cacheName, String key, Object value) {
        Objects.requireNonNull(cacheName, "缓存名称不能为空");
        Objects.requireNonNull(key, "缓存键不能为空");

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                log.warn("缓存不存在: {}", cacheName);
                return;
            }

            cache.put(key, value);
            log.debug("缓存值已设置: cacheName={}, key={}", cacheName, key);
        } catch (Exception e) {
            log.error("设置缓存值失败: cacheName={}, key={}", cacheName, key, e);
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        Objects.requireNonNull(cacheName, "缓存名称不能为空");
        Objects.requireNonNull(key, "缓存键不能为空");

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                log.warn("缓存不存在: {}", cacheName);
                return;
            }

            cache.evict(key);
            log.debug("缓存值已删除: cacheName={}, key={}", cacheName, key);
        } catch (Exception e) {
            log.error("删除缓存值失败: cacheName={}, key={}", cacheName, key, e);
        }
    }

    @Override
    public void clear(String cacheName) {
        Objects.requireNonNull(cacheName, "缓存名称不能为空");

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                log.warn("缓存不存在: {}", cacheName);
                return;
            }

            cache.clear();
            log.debug("缓存已清空: cacheName={}", cacheName);
        } catch (Exception e) {
            log.error("清空缓存失败: cacheName={}", cacheName, e);
        }
    }

    @Override
    public boolean exists(String cacheName, String key) {
        Objects.requireNonNull(cacheName, "缓存名称不能为空");
        Objects.requireNonNull(key, "缓存键不能为空");

        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return false;
            }

            return cache.get(key) != null;
        } catch (Exception e) {
            log.error("检查缓存存在性失败: cacheName={}, key={}", cacheName, key, e);
            return false;
        }
    }

    @Override
    public <T> T getOrSet(String cacheName, String key, Class<T> valueType, Supplier<T> supplier) {
        Objects.requireNonNull(cacheName, "缓存名称不能为空");
        Objects.requireNonNull(key, "缓存键不能为空");
        Objects.requireNonNull(valueType, "值类型不能为空");
        Objects.requireNonNull(supplier, "值提供者不能为空");

        // 先尝试从缓存获取
        Optional<T> cachedValue = get(cacheName, key, valueType);
        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        // 缓存未命中，执行supplier获取值
        try {
            T value = supplier.get();
            if (value != null) {
                put(cacheName, key, value);
            }
            return value;
        } catch (Exception e) {
            log.error("执行值提供者失败: cacheName={}, key={}", cacheName, key, e);
            throw e;
        }
    }
}
