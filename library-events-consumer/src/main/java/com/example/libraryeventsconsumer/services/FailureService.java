package com.example.libraryeventsconsumer.services;

import com.example.libraryeventsconsumer.entities.FailureRecord;
import com.example.libraryeventsconsumer.repositories.FailureRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FailureService {

    private final FailureRecordRepository failureRecordRepository;

    public void saveFailedRecord(ConsumerRecord<?, ?> record, Exception exception, String recordStatus) {

        FailureRecord failureRecord =
                new FailureRecord(
                        null,
                    record.topic(),
                        (Integer) record.key(),
                        (String) record.value(),
                    record.partition(),
                    record.offset(),
                    exception.getCause().getMessage(),
                    recordStatus
                );

        failureRecordRepository.save(failureRecord);

    }
}
