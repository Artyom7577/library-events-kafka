package com.example.libraryeventsproducer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Slf4j
public class AutoCreateConfig {

    @Value("${spring.kafka.topic}")
    public String topic;

    @Bean
    public NewTopic libraryEvents() {
        NewTopic newTopic = TopicBuilder
                .name(topic)
                .partitions(3)
                .replicas(3)
                .build();

        log.info("Creating topic: {} with partitions: {} and replicas: {}", newTopic.name(), newTopic.numPartitions(), newTopic.replicationFactor());

        return newTopic;
    }
}
