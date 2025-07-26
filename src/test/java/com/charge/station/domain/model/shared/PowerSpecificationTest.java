package com.charge.station.domain.model.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PowerSpecification单元测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
class PowerSpecificationTest {

    @Test
    void 构造函数_应该创建有效的功率规格() {
        // Given
        BigDecimal maxPower = new BigDecimal("120.00");
        String unit = "kW";
        
        // When
        PowerSpecification spec = new PowerSpecification(maxPower, unit);
        
        // Then
        assertThat(spec.maxPower()).isEqualTo(maxPower);
        assertThat(spec.unit()).isEqualTo(unit);
    }

    @Test
    void 构造函数_当最大功率为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new PowerSpecification(null, "kW"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("最大功率不能为空");
    }

    @Test
    void 构造函数_当单位为null时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new PowerSpecification(new BigDecimal("120.00"), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("功率单位不能为空");
    }

    @Test
    void 构造函数_当最大功率小于等于0时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new PowerSpecification(BigDecimal.ZERO, "kW"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("最大功率必须大于0");
            
        assertThatThrownBy(() -> new PowerSpecification(new BigDecimal("-10"), "kW"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("最大功率必须大于0");
    }

    @Test
    void 构造函数_当最大功率超过限制时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new PowerSpecification(new BigDecimal("600.00"), "kW"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("最大功率不能超过500.00kW");
    }

    @Test
    void 构造函数_当单位为空字符串时应该抛出异常() {
        // Given & When & Then
        assertThatThrownBy(() -> new PowerSpecification(new BigDecimal("120.00"), ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("功率单位不能为空字符串");
            
        assertThatThrownBy(() -> new PowerSpecification(new BigDecimal("120.00"), "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("功率单位不能为空字符串");
    }

    @Test
    void ofKilowatts_BigDecimal_应该创建默认单位的功率规格() {
        // Given
        BigDecimal maxPower = new BigDecimal("120.00");
        
        // When
        PowerSpecification spec = PowerSpecification.ofKilowatts(maxPower);
        
        // Then
        assertThat(spec.maxPower()).isEqualTo(maxPower);
        assertThat(spec.unit()).isEqualTo("kW");
    }

    @Test
    void ofKilowatts_double_应该创建默认单位的功率规格() {
        // Given
        double maxPower = 120.0;
        
        // When
        PowerSpecification spec = PowerSpecification.ofKilowatts(maxPower);
        
        // Then
        assertThat(spec.maxPower()).isEqualTo(BigDecimal.valueOf(maxPower));
        assertThat(spec.unit()).isEqualTo("kW");
    }

    @Test
    void isFastCharging_应该正确判断是否为快充() {
        // Given & When & Then
        assertThat(PowerSpecification.ofKilowatts(50.0).isFastCharging()).isTrue();
        assertThat(PowerSpecification.ofKilowatts(120.0).isFastCharging()).isTrue();
        assertThat(PowerSpecification.ofKilowatts(49.9).isFastCharging()).isFalse();
        assertThat(PowerSpecification.ofKilowatts(7.0).isFastCharging()).isFalse();

        // 非kW单位不算快充
        PowerSpecification nonKwSpec = new PowerSpecification(new BigDecimal("50"), "W");
        assertThat(nonKwSpec.isFastCharging()).isFalse();
    }

    @Test
    void isSuperCharging_应该正确判断是否为超充() {
        // Given & When & Then
        assertThat(PowerSpecification.ofKilowatts(150.0).isSuperCharging()).isTrue();
        assertThat(PowerSpecification.ofKilowatts(350.0).isSuperCharging()).isTrue();
        assertThat(PowerSpecification.ofKilowatts(149.9).isSuperCharging()).isFalse();
        assertThat(PowerSpecification.ofKilowatts(120.0).isSuperCharging()).isFalse();

        // 非kW单位不算超充
        PowerSpecification nonKwSpec = new PowerSpecification(new BigDecimal("150"), "W");
        assertThat(nonKwSpec.isSuperCharging()).isFalse();
    }

    @Test
    void getFormattedPower_应该返回格式化的功率字符串() {
        // Given
        PowerSpecification spec1 = PowerSpecification.ofKilowatts(120.0);
        PowerSpecification spec2 = new PowerSpecification(new BigDecimal("7.00"), "kW");
        PowerSpecification spec3 = new PowerSpecification(new BigDecimal("50"), "W");

        // When & Then
        assertThat(spec1.getFormattedPower()).isEqualTo("120.0kW");
        assertThat(spec2.getFormattedPower()).isEqualTo("7.00kW");
        assertThat(spec3.getFormattedPower()).isEqualTo("50W");
    }
}
