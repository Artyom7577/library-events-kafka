package com.example.libraryeventsconsumer.scheduler;

import com.example.libraryeventsconsumer.config.LibraryEventsConsumerConfig;
import com.example.libraryeventsconsumer.entities.FailureRecord;
import com.example.libraryeventsconsumer.repositories.FailureRecordRepository;
import com.example.libraryeventsconsumer.services.LibraryEventsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryScheduler {

    private final FailureRecordRepository failureRecordRepository;
    private final LibraryEventsService libraryEventsService;

    @Scheduled(fixedRate = 10000L)
    public void retryFailedRecords(){

        log.info("Retrying Failed Records Started!");
        failureRecordRepository.findAllByStatus(LibraryEventsConsumerConfig.RETRY)
                .forEach(failureRecord -> {
                    ConsumerRecord<Integer, String> consumerRecord = buildConsumerRecord(failureRecord);
                    try {
                        libraryEventsService.processLibraryEvent(consumerRecord);
                        failureRecord.setStatus(LibraryEventsConsumerConfig.SUCCESS);
                    } catch (JsonProcessingException e) {
                        log.error("Exception in retryFailedRecords : ", e);
                    }
                });
        log.info("Retrying Failed Records Completed!");
    }

    private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
        return new ConsumerRecord<>(
                failureRecord.getTopic(),
                failureRecord.getPartition(),
                failureRecord.getOffset_value(),
                failureRecord.getKey_value(),
                failureRecord.getErrorRecord()
        );
    }
}
