package com.example.test_runner_worker.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Our custom annotation to mark and tag test methods.
 */
@Retention(RetentionPolicy.RUNTIME) // Makes it readable at runtime
@Target(ElementType.METHOD)         // We can only put this on methods
public @interface Test {
    String name();
    String description() default "";
    String[] tags() default {};
}