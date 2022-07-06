package com.rogue.outbox.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
public class OutboxMethodCallback implements ReflectionUtils.MethodCallback {

    ConfigurableListableBeanFactory configurableBeanFactory;

    private static int AUTOWIRE_MODE = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    public OutboxMethodCallback(ConfigurableListableBeanFactory configurableBeanFactory, Object bean) {
        this.configurableBeanFactory = configurableBeanFactory;
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        if (!method.isAnnotationPresent(OutboxListener.class)) {
            return;
        }
        ReflectionUtils.makeAccessible(method);
        OutboxListener declaredAnnotation = method.getDeclaredAnnotation(OutboxListener.class);
        String beanName = declaredAnnotation.id()+"-ContainerFactory";
        Object beanInstance = getBeanInstance(beanName, declaredAnnotation, method);
        log.info("Method found ={}",method.getName());
    }

    public Object getBeanInstance(String beanName, OutboxListener outboxListener, Method method) {
        Object concurrentOutboxListenerContainerInstance = null;
        if (!configurableBeanFactory.containsBean(beanName)) {
            log.info("Creating new concurrent container factory bean named '{}'.", beanName);

            Object toRegister = null;
            try {
                toRegister = new ConcurrentOutboxListenerContainer(outboxListener.concurrency(), method);
            } catch (Exception e) {
                log.error("ERROR_CREATE_INSTANCE, {}", ConcurrentOutboxListenerContainer.class.getSimpleName(), e);
                throw new RuntimeException(e);
            }

            concurrentOutboxListenerContainerInstance = configurableBeanFactory.initializeBean(toRegister, beanName);
            configurableBeanFactory.autowireBeanProperties(concurrentOutboxListenerContainerInstance, AUTOWIRE_MODE, true);
            configurableBeanFactory.registerSingleton(beanName, concurrentOutboxListenerContainerInstance);
            log.info("Bean named '{}' created successfully.", beanName);
        } else {
            concurrentOutboxListenerContainerInstance = configurableBeanFactory.getBean(beanName);
            log.info("Bean named '{}' already exists used as current bean reference", beanName);
        }
        return concurrentOutboxListenerContainerInstance;
    }
}
