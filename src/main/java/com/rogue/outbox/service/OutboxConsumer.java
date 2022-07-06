package com.rogue.outbox.service;

import com.rogue.outbox.config.OutboxListener;
import com.rogue.outbox.model.OutboxRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OutboxConsumer {

    @OutboxListener(id = "outbox-1", concurrency = 1, table = "users")
    public void listenMessages(List<OutboxRecord> records){
        log.info("listener triggered");
        records.forEach(System.out::println);
    }
}
