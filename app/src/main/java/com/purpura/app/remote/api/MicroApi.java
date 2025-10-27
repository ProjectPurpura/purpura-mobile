package com.purpura.app.remote.api;

import com.purpura.app.configuration.EnvironmentVariables;
import com.purpura.app.model.micro.CEP;
import com.purpura.app.model.micro.QrCode;
import com.purpura.app.remote.util.Api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

@Api(EnvironmentVariables.MICRO_URL)
public interface MicroApi {
    @GET("/cep/{cep}")
    Call<CEP> getCep(@Path("cep") String cep);

    @GET("/qr/generate")
    Call<byte[]> generateQr(@Body QrCode qrRequest);
}
