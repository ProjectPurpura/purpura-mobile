package com.purpura.app.remote.service;

import com.purpura.app.model.micro.PurpuraQrRequest;
import com.purpura.app.remote.api.MicroApi;

import java.util.function.Consumer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MicroService extends BaseService<MicroApi> {
    public MicroService() {
        super(MicroApi.class);
    }

    public Call<Boolean> callCepIsValid(String cep){
        return this.api.cep_is_valid(cep);
    }

    public void generateQr(String pixKey, Consumer<byte[]> onImageCreated, Consumer<Integer> onError) {
        this.api.generateQr(PurpuraQrRequest.from(pixKey))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                onImageCreated.accept(response.body().bytes());
                            } catch (Exception e) {
                                onError.accept(-2);
                            }
                        } else {
                            onError.accept(response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        onError.accept(-1);
                    }
                });
    }
}
