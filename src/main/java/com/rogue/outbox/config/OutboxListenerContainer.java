package com.rogue.outbox.config;

import com.rogue.outbox.model.OutboxRecord;
import com.rogue.outbox.service.OutboxConsumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Getter
@Slf4j
@AllArgsConstructor
public class OutboxListenerContainer implements Runnable {
    private OutboxRecordPoller recordPoller;
    private boolean running=false;
    private boolean paused=false;
    private Method methodToInvoke;

    private int maxPollRecords=2;

    public void stop(){
        this.running=false;
    }

    public void resume(){
        this.paused=false;
    }

    public void pause(){
        this.paused=true;
    }

    public void start(){
        this.running=true;
    }

    @Override
    public void run() {
        while(isRunning()){
            if(!isPaused()) {
                List<OutboxRecord> poll = recordPoller.poll(maxPollRecords);
                try {
                    log.info("polling for {} records", maxPollRecords);
                    methodToInvoke.setAccessible(true);
                    methodToInvoke.invoke(new OutboxConsumer(),poll);
                    recordPoller.commit();
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (Exception ex){
                    log.error("Exception occurred in the listener thread", ex);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
