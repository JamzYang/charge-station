package com.charge.station.domain.model.station;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * StationId单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
class StationIdTest {

    @Test
    void 构造函数_应该创建有效的充电站ID() {
        // Given
        String validId = "ST12345678901234567890";
        
        // When
        StationId stationId = new StationId(validId);
        
        // Then
        assertThat(stationId.value()).isEqualTo(validId);
    }

    @Test
    void 构造函数_当ID为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new StationId(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("充电站ID不能为空");
    }

    @Test
    void 构造函数_当ID为空字符串时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new StationId(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("充电站ID不能为空字符串");
            
        assertThatThrownBy(() -> new StationId("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("充电站ID不能为空字符串");
    }

    @Test
    void 构造函数_当ID长度超过限制时应该抛出异常() {
        // Given
        String tooLongId = "ST" + "a".repeat(31); // 33个字符，超过32个字符限制
        
        // When & Then
        assertThatThrownBy(() -> new StationId(tooLongId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("充电站ID长度不能超过32个字符");
    }

    @Test
    void 构造函数_当ID不以ST开头时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new StationId("CP12345678901234567890"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("充电站ID必须以ST开头");
            
        assertThatThrownBy(() -> new StationId("12345678901234567890"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("充电站ID必须以ST开头");
    }

    @Test
    void generate_应该生成有效的充电站ID() {
        // Given & When
        StationId stationId = StationId.generate();
        
        // Then
        assertThat(stationId.value()).isNotNull();
        assertThat(stationId.value()).startsWith("ST");
        assertThat(stationId.value().length()).isEqualTo(22); // ST + 20个字符
    }

    @Test
    void generate_应该生成不同的ID() {
        // Given & When
        StationId id1 = StationId.generate();
        StationId id2 = StationId.generate();
        
        // Then
        assertThat(id1.value()).isNotEqualTo(id2.value());
    }

    @Test
    void of_应该创建有效的充电站ID() {
        // Given
        String validId = "ST12345678901234567890";
        
        // When
        StationId stationId = StationId.of(validId);
        
        // Then
        assertThat(stationId.value()).isEqualTo(validId);
    }

    @Test
    void toString_应该返回ID值() {
        // Given
        String idValue = "ST12345678901234567890";
        StationId stationId = new StationId(idValue);
        
        // When
        String result = stationId.toString();
        
        // Then
        assertThat(result).isEqualTo(idValue);
    }

    @Test
    void equals_应该正确比较相等性() {
        // Given
        String idValue = "ST12345678901234567890";
        StationId id1 = new StationId(idValue);
        StationId id2 = new StationId(idValue);
        StationId id3 = new StationId("ST98765432109876543210");
        
        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
