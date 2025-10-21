package com.purpura.app.remote.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService<T> {
    private final Retrofit retrofit;
    private final Class<T> serviceClass;

    public RetrofitService(Class<T> serviceClass) {
        this.serviceClass = serviceClass;

        Api apiAnnotation = serviceClass.getAnnotation(Api.class);
        if (apiAnnotation == null) {
            throw new IllegalArgumentException(
                    "Service class must be annotated with @Api: " + serviceClass.getName()
            );
        }

        this.retrofit = new Retrofit.Builder()
                .baseUrl(apiAnnotation.value())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public T getService() {
        return retrofit.create(serviceClass);
    }
}