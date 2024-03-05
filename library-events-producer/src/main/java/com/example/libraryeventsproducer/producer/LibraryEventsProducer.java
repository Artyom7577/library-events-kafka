package com.example.libraryeventsproducer.producer;

import com.example.libraryeventsproducer.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventsProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.topic}")
    public String topic;

    public LibraryEventsProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) {
        try {
            var key = libraryEvent.libraryEventId();
            var value = objectMapper.writeValueAsString(libraryEvent);

            ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);

            CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(producerRecord);

            return completableFuture
                    .whenComplete((sendResult, throwable) -> {
                        if (throwable != null) {
                            handleFailure(key, value, throwable);
                        } else {
                            handleSuccess(key, value, sendResult);
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new RuntimeException("T: Here is exception " + e);
        }
    }

    public SendResult<Integer, String> sendLibraryEventSync(LibraryEvent libraryEvent) {
        try {
            var key = libraryEvent.libraryEventId();
            var value = objectMapper.writeValueAsString(libraryEvent);

            SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, key, value)
                    .get(3, TimeUnit.SECONDS);

            handleSuccess(key, value, sendResult);
            return sendResult;

        } catch (JsonProcessingException | InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("T: Here is exception " + e);
        }
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {

        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message Sent Successfully for the key : {} and the value : {} , partition is {} ",
                key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending the message and the exception is {} ", throwable.getMessage(), throwable);
    }
}
