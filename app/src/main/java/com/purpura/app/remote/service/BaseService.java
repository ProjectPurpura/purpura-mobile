package com.purpura.app.remote.service;

import com.purpura.app.remote.util.RetrofitService;

public abstract class BaseService<T> {
    private T api;

    public BaseService(Class<T> apiClass) {
        this.api = new RetrofitService<>(apiClass).getService();
    }
}
