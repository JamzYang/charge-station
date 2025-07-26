package com.charge.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 充电站运营平台主应用类
 * 
 * @author 架构师团队
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class StationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationServiceApplication.class, args);
    }
}
