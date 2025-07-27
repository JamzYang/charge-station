package com.charge.station.integration;

import com.charge.station.shared.constant.KafkaTopics;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Kafka Topic 验证测试
 */
@SpringBootTest
@ActiveProfiles("integration")
@DisplayName("Kafka Topic 验证测试")
class KafkaTopicVerificationTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Test
    @DisplayName("列出所有 Kafka Topics")
    void listAllTopics() throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);

        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> topicNames = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            
            System.out.println("=== 当前 Kafka 中的所有 Topics ===");
            topicNames.forEach(System.out::println);
            
            System.out.println("\n=== 检查必需的 Topics ===");
            checkTopic(adminClient, KafkaTopics.COMMANDS_DOWN);
            checkTopic(adminClient, KafkaTopics.OCPP_EVENTS_UP);
        }
    }

    private void checkTopic(AdminClient adminClient, String topicName) throws Exception {
        try {
            DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
            TopicDescription description = result.values().get(topicName).get(10, TimeUnit.SECONDS);
            
            System.out.println("✅ Topic: " + topicName);
            System.out.println("   分区数: " + description.partitions().size());
            System.out.println("   副本数: " + description.partitions().get(0).replicas().size());
        } catch (Exception e) {
            System.out.println("❌ Topic: " + topicName + " - " + e.getMessage());
        }
    }
}
