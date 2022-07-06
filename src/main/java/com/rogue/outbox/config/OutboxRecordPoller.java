package com.rogue.outbox.config;

import com.rogue.outbox.model.OutboxRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Slf4j
public class OutboxRecordPoller {
    private int startRange;
    private int endRange;

    public List<OutboxRecord> poll(int n){
        List<OutboxRecord> list = new ArrayList<>(n);
        for(int i=0; i<n ;i++){
            Random random = new Random();
            int hashValue = random.nextInt(10 - 1 + 1) + 1;
            OutboxRecord record = OutboxRecord.builder().id(UUID.randomUUID()).hashValue(hashValue).payload("random-string").build();
            list.add(record);
        }
        return list;
    }

    public void commit(){
        log.info("Committing record status");
    }
}
