package com.purpura.app.remote.service;

import com.purpura.app.remote.api.MicroApi;
import com.purpura.app.remote.util.RetrofitService;

public class MicroService {
    private MicroApi microApi;

    public MicroService() {
        this.microApi = new RetrofitService<>(MicroApi.class).getService();
    }
}
