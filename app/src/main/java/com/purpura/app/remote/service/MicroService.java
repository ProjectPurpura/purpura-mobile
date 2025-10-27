package com.purpura.app.remote.service;

import com.purpura.app.configuration.Methods;
import com.purpura.app.model.micro.CEP;
import com.purpura.app.remote.api.MicroApi;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MicroService extends BaseService<MicroApi> {
    Methods methods = new Methods();
    public MicroService() {
        super(MicroApi.class);
    }

    public void getCepDetails(String cep, Consumer<CEP> cepResponseConsumer, Consumer<Integer> onError) {
        this.api
                .getCep(cep)
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(Call<CEP> call, Response<CEP> response) {
                        if (response.isSuccessful()) {
                            cepResponseConsumer.accept(response.body());
                        } else {
                            onError.accept(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<CEP> call, Throwable t) {
                        onError.accept(-1);
                    }
                });
    }


    public void generateQr(String pixKey, Consumer<byte[]> onImageCreated, Consumer<Integer> onError) {
        this.api
                .generateQr(methods.generateQR(pixKey))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<byte[]> call, Response<byte[]> response) {
                        if (response.isSuccessful()) {
                            onImageCreated.accept(response.body());
                        } else {
                            onError.accept(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<byte[]> call, Throwable t) {
                        onError.accept(-1);
                    }
                });
    }
}
