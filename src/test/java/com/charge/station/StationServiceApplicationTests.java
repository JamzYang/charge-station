package com.charge.station;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用启动测试
 * 
 * @author 架构师团队
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class StationServiceApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }
}
