package com.purpura.app.remote.service;

import com.purpura.app.remote.api.MicroApi;

public class MicroService extends BaseService<MicroApi> {
    private MicroApi microApi;

    public MicroService() {
        super(MicroApi.class);
    }
}
