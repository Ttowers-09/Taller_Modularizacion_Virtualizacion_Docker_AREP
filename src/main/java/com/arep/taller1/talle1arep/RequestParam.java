package com.arep.taller1.talle1arep;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String value();
    String defaultValue() default "";
}
