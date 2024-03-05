package com.example.libraryeventsconsumer.config;

import com.example.libraryeventsconsumer.services.FailureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LibraryEventsConsumerConfig {

    public static final String RETRY = "RETRY";
    public static final String SUCCESS = "SUCCESS";
    public static final String DEAD = "DEAD";

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final FailureService failureService;
    ConsumerRecordRecoverer consumerRecordRecoverer = (record, exception) -> {
        log.error("Exception is : {} Failed Record : {} ", exception, record);
        if (exception.getCause() instanceof RecoverableDataAccessException) {
            log.info("Inside the recoverable logic");
//            failureService.saveFailedRecord(record, exception, RETRY);
        } else {
            log.info("Inside the non recoverable logic and skipping the record : {}", record);
        }
    };

    @Value("${topics.retry}")
    private String retryTopic;

    @Value("${topics.dlt}")
    private String deadLetterTopic;

    public DeadLetterPublishingRecoverer publishingRecoverer() {

        return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {

            log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);

            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(retryTopic, r.partition());
            } else {
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        }
        );
    }

    public DefaultErrorHandler errorHandler() {

        var exceptionToIgnoreList = List.of(IllegalArgumentException.class);

        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2.0);
        expBackOff.setMaxInterval(2_000L);

        var fixedBackOff = new FixedBackOff(1000L, 2L);

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
//                consumerRecordRecoverer,
                publishingRecoverer(),
                fixedBackOff
        );

        exceptionToIgnoreList.forEach(defaultErrorHandler::addNotRetryableExceptions);


        defaultErrorHandler
                .setRetryListeners(
                        (record, ex, deliveryAttempt) -> log.info("Failed Record in Retry Listener, Exception : {} , deliveryAttempt : {} ",
                                ex.getMessage(), deliveryAttempt)
                );

        return defaultErrorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }
}
