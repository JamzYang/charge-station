package com.charge.station.integration;

import com.charge.station.shared.constant.KafkaTopics;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Kafka 连接性测试
 * 
 * 验证测试环境中的 Kafka 服务是否正常工作
 */
@SpringBootTest
@ActiveProfiles("integration")
@DisplayName("Kafka 连接性测试")
class KafkaConnectivityTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final int PARTITION_COUNT = 128;
    private static final short REPLICATION_FACTOR = 1;

    @BeforeEach
    void setUp() {
        // 确保必要的 topic 存在
        createTopicIfNotExists(KafkaTopics.COMMANDS_DOWN);
        createTopicIfNotExists(KafkaTopics.OCPP_EVENTS_UP);
    }

    @Test
    @DisplayName("测试 Kafka 生产者连接")
    void testKafkaProducerConnection() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 15000);

        assertDoesNotThrow(() -> {
            try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    KafkaTopics.COMMANDS_DOWN, 
                    "test-key", 
                    "test-message"
                );
                producer.send(record).get(10, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    @DisplayName("测试指定分区发送")
    void testSendToSpecificPartition() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 15000);

        assertDoesNotThrow(() -> {
            try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
                // 测试发送到分区0
                ProducerRecord<String, String> record = new ProducerRecord<>(
                    KafkaTopics.COMMANDS_DOWN, 
                    0, // 指定分区
                    "test-key", 
                    "test-message"
                );
                producer.send(record).get(10, TimeUnit.SECONDS);
            }
        });
    }

    private void createTopicIfNotExists(String topicName) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);

        try (AdminClient adminClient = AdminClient.create(props)) {
            // 检查 topic 是否存在
            if (!adminClient.listTopics().names().get(5, TimeUnit.SECONDS).contains(topicName)) {
                // 创建 topic
                NewTopic newTopic = new NewTopic(topicName, PARTITION_COUNT, REPLICATION_FACTOR);
                CreateTopicsResult result = adminClient.createTopics(Collections.singletonList(newTopic));
                result.all().get(10, TimeUnit.SECONDS);
                System.out.println("Created topic: " + topicName + " with " + PARTITION_COUNT + " partitions");
            } else {
                System.out.println("Topic already exists: " + topicName);
            }
        } catch (Exception e) {
            System.err.println("Failed to create topic " + topicName + ": " + e.getMessage());
            // 不抛出异常，让测试继续进行
        }
    }
}
