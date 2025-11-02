package com.purpura.app.remote.api;

import com.purpura.app.configuration.EnvironmentVariables;
import com.purpura.app.model.micro.QrRequest;
import com.purpura.app.remote.util.Api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;

@Api(EnvironmentVariables.MICRO_URL)
public interface MicroApi {
    @GET("cep/{cep}/is_valid")
    Call<Boolean> cep_is_valid(@Path("cep") String cep);

    @POST("qr/generate")
    Call<byte[]> generateQr(@Body QrRequest qrRequest);
}
