package com.purpura.app.remote.service;

import com.purpura.app.model.micro.CepResponse;
import com.purpura.app.model.micro.PurpuraQrRequest;
import com.purpura.app.remote.api.MicroApi;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MicroService extends BaseService<MicroApi> {
    public MicroService() {
        super(MicroApi.class);
    }

    public void getCepDetails(String cep, Consumer<CepResponse> cepResponseConsumer, Consumer<Integer> onError) {
        this.api
                .getCep(cep)
                .enqueue(new Callback<>() {

                    @Override
                    public void onResponse(Call<CepResponse> call, Response<CepResponse> response) {
                        if (response.isSuccessful()) {
                            cepResponseConsumer.accept(response.body());
                        } else {
                            onError.accept(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<CepResponse> call, Throwable t) {
                        onError.accept(-1);
                    }
                });
    }


    public void generateQr(String pixKey, Consumer<byte[]> onImageCreated, Consumer<Integer> onError) {
        this.api
                .generateQr(PurpuraQrRequest.from(pixKey))
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
