package com.charge.station.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类
 * 
 * 配置JSON序列化和反序列化，特别是Java 8时间类型的处理
 * 支持中国常用的时间格式：yyyy-MM-dd HH:mm:ss
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * 中国时区
     */
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    
    /**
     * 时间格式：HH:mm
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * 日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 自定义ObjectMapper配置
     * 
     * @return ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());
        
        // 创建自定义时间序列化模块
        SimpleModule timeModule = new SimpleModule("ChineseTimeModule");
        
        // LocalTime 序列化为 "HH:mm" 格式
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        
        // Instant 序列化为 "yyyy-MM-dd HH:mm:ss" 格式（中国时区）
        timeModule.addSerializer(Instant.class, new InstantSerializer());
        timeModule.addDeserializer(Instant.class, new InstantDeserializer());
        
        objectMapper.registerModule(timeModule);
        
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 禁用将时间写为数组
        objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        
        return objectMapper;
    }

    /**
     * LocalTime 自定义序列化器
     */
    public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(TIME_FORMATTER));
        }
    }

    /**
     * LocalTime 自定义反序列化器
     */
    public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalTime.parse(p.getValueAsString(), TIME_FORMATTER);
        }
    }

    /**
     * Instant 自定义序列化器（转换为中国时区）
     */
    public static class InstantSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String formatted = value.atZone(CHINA_ZONE).format(DATETIME_FORMATTER);
            gen.writeString(formatted);
        }
    }

    /**
     * Instant 自定义反序列化器（支持毫秒级时间戳）
     */
    public static class InstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                // 处理数字类型的时间戳
                long timestamp = p.getValueAsLong();

                // 判断是秒级还是毫秒级时间戳
                // 毫秒级时间戳通常大于 10^12 (1000000000000)
                if (timestamp > 1_000_000_000_000L) {
                    // 毫秒级时间戳
                    return Instant.ofEpochMilli(timestamp);
                } else {
                    // 秒级时间戳
                    return Instant.ofEpochSecond(timestamp);
                }
            } else if (p.hasToken(JsonToken.VALUE_STRING)) {
                // 处理字符串类型的时间戳或ISO格式
                String value = p.getValueAsString();
                try {
                    // 尝试解析为数字时间戳
                    long timestamp = Long.parseLong(value);
                    if (timestamp > 1_000_000_000_000L) {
                        return Instant.ofEpochMilli(timestamp);
                    } else {
                        return Instant.ofEpochSecond(timestamp);
                    }
                } catch (NumberFormatException e) {
                    // 如果不是数字，尝试解析为ISO格式
                    return Instant.parse(value);
                }
            }

            throw new IOException("无法解析时间戳: " + p.getText());
        }
    }
}
