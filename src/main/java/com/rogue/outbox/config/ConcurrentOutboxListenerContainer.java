package com.rogue.outbox.config;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ConcurrentOutboxListenerContainer implements InitializingBean {
    private List<OutboxListenerContainer> containers = new ArrayList<>();
    private List<AsyncListenableTaskExecutor> executors = new ArrayList<>();
    private List<ListenableFuture<?>> futures = new ArrayList<>();
    private int concurrency;
    private Method method;

    public ConcurrentOutboxListenerContainer(int concurrency, Method method){
        this.concurrency = concurrency;
        this.method = method;
    }

    public void doStart(){
        for(int i=0; i< concurrency; i++){
            OutboxRecordPoller outboxRecordPoller = new OutboxRecordPoller(1,1);
            OutboxListenerContainer container = new OutboxListenerContainer(outboxRecordPoller, true, false, method, 2);
            AsyncListenableTaskExecutor simpleAsyncTaskExecutor = null;
            if ((this.executors.size() > i)) {
                 simpleAsyncTaskExecutor = this.executors.get(i);
            } else {
                simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor("concurrentOutboxListenerContainer-c");
                executors.add(simpleAsyncTaskExecutor);
            }
            ListenableFuture<?> listenableFuture = simpleAsyncTaskExecutor.submitListenable(container);
            futures.add(i, listenableFuture);
            containers.add(i, container);
        }
    }

    public void doStop(){
        for(int i=0; i< concurrency; i++){
            OutboxListenerContainer container = containers.get(i);
            container.stop();
            ListenableFuture<?> listenableFuture = futures.get(i);
            listenableFuture.cancel(true);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        doStart();
    }
}
