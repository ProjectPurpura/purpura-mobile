package com.purpura.app.remote.api;

import com.purpura.app.configuration.EnvironmentVariables;
import com.purpura.app.model.mongo.Adress;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.util.Api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Api(value = EnvironmentVariables.MONGO_API)
public interface MongoAPI {

    //GET
    @GET("/empresa/{cnpj}")
    Call<Company> getCompanyByCNPJ(@Path("cnpj") String cnpj);
    @GET("/empresa/{cnpj}/residuo/{id}")
    Call<Residue> getResidueById(@Path("cnpj") String cnpj, @Path("id") String id);
    @GET("/empresa/{cnpj}/pix/{id}")
    Call<PixKey> getPixKeyById(@Path("cnpj") String cnpj, @Path("id") String id);
    @GET("/empresa/{cnpj}/residuo/viewmain")
    Call<List<Residue>> getAllResiduosMain(@Path("cnpj") String cnpj, @Query("limit") int limit, @Query("page") int page);
    @GET("/empresa/{cnpj}/endereco/{id}")
    Call<Adress> getAddressById(@Path("cnpj") String cnpj, @Path("id") String id);
    @GET("/empresa/{cnpj}/residuo/all")
    Call<List<Residue>> getAllResidues(@Path("cnpj") String cnpj);
    @GET("/empresa/{cnpj}/pix/all")
    Call<List<PixKey>> getAllPixKeys(@Path("cnpj") String cnpj);
    @GET("/empresa/{cnpj}/endereco/all")
    Call<List<Adress>> getAllAddress(@Path("cnpj") String cnpj);
    @GET("/empresa/search")
    Call<List<Company>> searchCompany(@Query("cnpj") String cnpj);
    @GET("/empresa/all")
    Call<List<Company>> getAllCompanies();

    //POST
    @POST("/empresa")
    Call<Company> createCompany(@Body Company company);
    @POST("/empresa/{cnpj}/residuo")
    Call<Residue> createResidue(@Path("cnpj") String cnpj, @Body Residue residue);
    @POST("/empresa/{cnpj}/pix")
    Call<PixKey> createPixKey(@Path("cnpj") String cnpj, @Body PixKey pixKey);
    @POST("/empresa/{cnpj}/endereco")
    Call<Adress> createAddress(@Path("cnpj") String cnpj, @Body Adress address);

    //PUT
    @PUT("/empresa/{cnpj}")
    Call<Company> updateCompany(@Path("cnpj") String cnpj, @Body Company company);
    @PUT("/empresa/{cnpj}/residuo/{id}")
    Call<Residue> updateResidue(@Path("cnpj") String cnpj, @Path("id") String id, @Body Residue residue);
    @PUT("/empresa/{cnpj}/pix/{id}")
    Call<PixKey> updatePixKey(@Path("cnpj") String cnpj, @Path("id") String id, @Body PixKey pixKey);
    @PUT("/empresa/{cnpj}/endereco/{id}")
    Call<Adress> updateAddress(@Path("cnpj") String cnpj, @Path("id") String id, @Body Adress address);

    //DELETE
    @DELETE("/empresa/{cnpj}")
    Call<Void> deleteCompany(@Path("cnpj") String cnpj);
    @DELETE("/empresa/{cnpj}/residuo/{id}")
    Call<Void> deleteResidue(@Path("cnpj") String cnpj, @Path("id") String id);
    @DELETE("/empresa/{cnpj}/pix/{id}")
    Call<Void> deletePixKey(@Path("cnpj") String cnpj, @Path("id") String id);
    @DELETE("/empresa/{cnpj}/endereco/{id}")
    Call<Void> deleteAddress(@Path("cnpj") String cnpj, @Path("id") String id);

}