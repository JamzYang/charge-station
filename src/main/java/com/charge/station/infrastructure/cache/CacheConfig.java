package com.charge.station.infrastructure.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 缓存配置
 * 
 * 配置缓存管理器和缓存策略。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String STATION_CACHE = "stations";
    public static final String CHARGE_POINT_CACHE = "chargePoints";
    public static final String STATION_BY_NAME_CACHE = "stationsByName";
    public static final String CHARGE_POINT_BY_SERIAL_CACHE = "chargePointsBySerial";
    public static final String NEARBY_STATIONS_CACHE = "nearbyStations";

    /**
     * 开发环境使用内存缓存管理器
     * 
     * @return CacheManager
     */
    @Bean
    @Profile({"dev", "test"})
    public CacheManager devCacheManager() {
        return new ConcurrentMapCacheManager(
            STATION_CACHE,
            CHARGE_POINT_CACHE,
            STATION_BY_NAME_CACHE,
            CHARGE_POINT_BY_SERIAL_CACHE,
            NEARBY_STATIONS_CACHE
        );
    }

    // TODO: 生产环境可以配置Redis缓存管理器
    // @Bean
    // @Profile("prod")
    // public CacheManager redisCacheManager() {
    //     // Redis缓存配置
    // }
}
