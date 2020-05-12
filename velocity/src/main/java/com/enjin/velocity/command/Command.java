package com.enjin.velocity.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Command {

    String value();

    String[] aliases() default {};

    String description() default "";

    boolean requireValidKey() default true;
}
