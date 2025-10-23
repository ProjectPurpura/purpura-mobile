package com.purpura.app.remote.util;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Api {
    String value(); // URL base da api
}
