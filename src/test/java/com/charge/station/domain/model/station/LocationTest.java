package com.charge.station.domain.model.station;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Location单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
class LocationTest {

    @Test
    void 构造函数_应该创建有效的地理位置() {
        // Given
        BigDecimal latitude = new BigDecimal("39.904200");
        BigDecimal longitude = new BigDecimal("116.407400");
        
        // When
        Location location = new Location(latitude, longitude);
        
        // Then
        assertThat(location.latitude()).isEqualTo(latitude);
        assertThat(location.longitude()).isEqualTo(longitude);
    }

    @Test
    void 构造函数_当纬度为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new Location(null, new BigDecimal("116.407400")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("纬度不能为空");
    }

    @Test
    void 构造函数_当经度为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new Location(new BigDecimal("39.904200"), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("经度不能为空");
    }

    @Test
    void 构造函数_当纬度超出范围时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new Location(new BigDecimal("91"), new BigDecimal("116.407400")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("纬度必须在-90到90之间");
            
        assertThatThrownBy(() -> new Location(new BigDecimal("-91"), new BigDecimal("116.407400")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("纬度必须在-90到90之间");
    }

    @Test
    void 构造函数_当经度超出范围时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new Location(new BigDecimal("39.904200"), new BigDecimal("181")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("经度必须在-180到180之间");
            
        assertThatThrownBy(() -> new Location(new BigDecimal("39.904200"), new BigDecimal("-181")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("经度必须在-180到180之间");
    }

    @Test
    void 构造函数_应该标准化坐标精度() {
        // Given
        BigDecimal latitude = new BigDecimal("39.9042001234567");
        BigDecimal longitude = new BigDecimal("116.4074001234567");
        
        // When
        Location location = new Location(latitude, longitude);
        
        // Then
        assertThat(location.latitude()).isEqualTo(new BigDecimal("39.904200"));
        assertThat(location.longitude()).isEqualTo(new BigDecimal("116.407400"));
    }

    @Test
    void of_应该创建有效的地理位置() {
        // Given
        double latitude = 39.904200;
        double longitude = 116.407400;
        
        // When
        Location location = Location.of(latitude, longitude);
        
        // Then
        assertThat(location.getLatitudeAsDouble()).isEqualTo(latitude);
        assertThat(location.getLongitudeAsDouble()).isEqualTo(longitude);
    }

    @Test
    void distanceTo_应该正确计算两点间距离() {
        // Given - 北京天安门和故宫的大概位置
        Location tiananmen = Location.of(39.904200, 116.407400);
        Location forbiddenCity = Location.of(39.916900, 116.397200);
        
        // When
        double distance = tiananmen.distanceTo(forbiddenCity);
        
        // Then - 两地距离大约1.5公里
        assertThat(distance).isCloseTo(1500, within(200.0));
    }

    @Test
    void distanceTo_当目标位置为null时应该抛出异常() {
        // Given
        Location location = Location.of(39.904200, 116.407400);
        
        // When & Then
        assertThatThrownBy(() -> location.distanceTo(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("目标位置不能为空");
    }

    @Test
    void distanceTo_相同位置距离应该为0() {
        // Given
        Location location1 = Location.of(39.904200, 116.407400);
        Location location2 = Location.of(39.904200, 116.407400);
        
        // When
        double distance = location1.distanceTo(location2);
        
        // Then
        assertThat(distance).isCloseTo(0, within(1.0));
    }

    @Test
    void isWithinRadius_应该正确判断是否在半径范围内() {
        // Given
        Location center = Location.of(39.904200, 116.407400);
        Location nearby = Location.of(39.914200, 116.417400); // 大约1.5公里
        Location faraway = Location.of(40.004200, 116.507400); // 大约15公里
        
        // When & Then
        assertThat(center.isWithinRadius(nearby, 2000)).isTrue();  // 2公里内
        assertThat(center.isWithinRadius(nearby, 1000)).isFalse(); // 1公里内
        assertThat(center.isWithinRadius(faraway, 20000)).isTrue(); // 20公里内
        assertThat(center.isWithinRadius(faraway, 10000)).isFalse(); // 10公里内
    }

    @Test
    void getFormattedCoordinates_应该返回格式化的坐标字符串() {
        // Given
        Location location = Location.of(39.904200, 116.407400);
        
        // When
        String formatted = location.getFormattedCoordinates();
        
        // Then
        assertThat(formatted).isEqualTo("39.904200,116.407400");
    }

    @Test
    void getLatitudeAsDouble_应该返回纬度的double值() {
        // Given
        Location location = Location.of(39.904200, 116.407400);
        
        // When
        double latitude = location.getLatitudeAsDouble();
        
        // Then
        assertThat(latitude).isEqualTo(39.904200);
    }

    @Test
    void getLongitudeAsDouble_应该返回经度的double值() {
        // Given
        Location location = Location.of(39.904200, 116.407400);
        
        // When
        double longitude = location.getLongitudeAsDouble();
        
        // Then
        assertThat(longitude).isEqualTo(116.407400);
    }

    @Test
    void equals_应该正确比较相等性() {
        // Given
        Location location1 = Location.of(39.904200, 116.407400);
        Location location2 = Location.of(39.904200, 116.407400);
        Location location3 = Location.of(39.914200, 116.417400);
        
        // When & Then
        assertThat(location1).isEqualTo(location2);
        assertThat(location1).isNotEqualTo(location3);
        assertThat(location1.hashCode()).isEqualTo(location2.hashCode());
    }
}
