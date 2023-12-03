package com.cheng.linegroup.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cheng
 * @since 2023/12/3 5:55 PM
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogApi {
    String module() default "";

    String description() default "";
}

