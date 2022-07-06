package com.rogue.outbox.config;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface OutboxListener {
    String id() default "";
    String table() default "outbox_table";
    int concurrency() default 1;
}
