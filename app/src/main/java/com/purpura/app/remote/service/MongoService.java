package com.purpura.app.remote.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.ChatRequest;
import com.purpura.app.model.mongo.ChatResponse;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.api.MongoAPI;
import com.purpura.app.remote.util.RetrofitService;
import com.purpura.app.ui.screens.MainActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MongoService {

    private MongoAPI mongoAPI;
    private Methods methods;

    public MongoService() {
        mongoAPI = new RetrofitService<>(MongoAPI.class).getService();
    }

    // GET

    public Call<List<Residue>> getAllResiduosMain(String cnpj, int limit, int page) {
        Call<List<Residue>> call = mongoAPI.getAllResiduosMain(cnpj, limit, page);
        return call;
    }

    public Call<List<Address>> getAllAddress(String cnpj) {
        Call<List<Address>> call = mongoAPI.getAllAddress(cnpj);
        return call;
    }

    public Call<List<Company>> getAllCompanies() {
        Call<List<Company>> call = mongoAPI.getAllCompanies();
        return call;
    }

    public Call<List<PixKey>> getAllPixKeys(String cnpj) {
        Call<List<PixKey>> call = mongoAPI.getAllPixKeys(cnpj);
        return call;
    }

    public Call<List<Residue>> getAllResidues(String cnpj) {
        Call<List<Residue>> call = mongoAPI.getAllResidues(cnpj);
        return call;
    }

    public Call<List<Company>> searchCompany(String cnpj) {
        Call<List<Company>> call = mongoAPI.searchCompany(cnpj);
        return call;
    }

    public Call<Residue> getResidueById(String cnpj, String id) {
        Call<Residue> call = mongoAPI.getResidueById(cnpj, id);
        return call;
    }

    public Call<PixKey> getPixKeyById(String cnpj, String id) {
        Call<PixKey> call = mongoAPI.getPixKeyById(cnpj, id);
        return call;
    }

    public Call<Address> getAdressById(String cnpj, String id) {
        Call<Address> call = mongoAPI.getAddressById(cnpj, id);
        return call;
    }

    public Call<Company> getCompanyByCnpj(String cnpj) {
        Call<Company> call = mongoAPI.getCompanyByCNPJ(cnpj);
        return call;
    }

    // CREATE - POST

    public Call<ChatResponse> createChat(ChatRequest request){
        Call<ChatResponse> call = mongoAPI.createChat(request);
        return call;
    }
    public Call<Address> createAdress(String cnpj, Address address, Context context) {
        Call<Address> call = mongoAPI.createAddress(cnpj, address);
        return call;
    }

    public void createPixKey(String cnpj, PixKey pixKey, Context context) {
        Call<PixKey> call = mongoAPI.createPixKey(cnpj, pixKey);
        call.enqueue(new Callback<PixKey>() {
            @Override
            public void onResponse(Call<PixKey> call, Response<PixKey> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Chave criada com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<PixKey> call, Throwable t) {
                Toast.makeText(context, "Erro ao criar chave", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createCompany(Company company, Context context) {
        Log.e("Entrou antes da requisição MongoService", "Entrou antes da requisição MongoService");
        Call<Company> call = mongoAPI.createCompany(company);
        call.enqueue(new Callback<Company>() {
            @Override
            public void onResponse(Call<Company> call, Response<Company> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "Empresa criada: " + new Gson().toJson(response.body()));
                    Toast.makeText(context, "Empresa criada com sucesso", Toast.LENGTH_SHORT).show();
                    methods.openActivityToMongoService(context, MainActivity.class);
                } else {
                    Log.e("API", "Erro: " + response.code() + " - " + response.message());
                    Toast.makeText(context, "Erro ao criar empresa: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Company> call, Throwable t) {
                Toast.makeText(context, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createResidue(String cnpj, Residue residue, Context context) {
        Call<Residue> call = mongoAPI.createResidue(cnpj, residue);
        call.enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(Call<Residue> call, Response<Residue> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Resíduo criado com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Residue> call, Throwable t) {
                Toast.makeText(context, "Erro ao criar resíduo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // CREATE - CALL Retornando call.enqueue
    public Call<Company> createCompanyCall(Company company) {
        return mongoAPI.createCompany(company);
    }

    public Call<Residue> createResidueCall(String cnpj, Residue residue) {
        return mongoAPI.createResidue(cnpj, residue);
    }

    public Call<Address> createAddressCall(String cnpj, Address address) {
        return mongoAPI.createAddress(cnpj, address);
    }

    public Call<PixKey> createPixKeyCall(String cnpj, PixKey pixKey) {
        return mongoAPI.createPixKey(cnpj, pixKey);
    }





    // UPDATE - PUT

    public void updateCompany(String cnpj, Company company, Context context) {
        Call<Void> call = mongoAPI.updateCompany(cnpj, company);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Empresa atualizada com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Erro ao atualizar empresa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Call<Void> updateCompanyCall(String cnpj, Company company) {
        return mongoAPI.updateCompany(cnpj, company);
    }

    public void updateAdress(String cnpj, String id, Address address, Context context) {
        Call<Void> call = mongoAPI.updateAddress(cnpj, id, address);
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                } else {
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    public void updatePixKey(String cnpj, String id, PixKey pixKey, Context context) {
        Call<PixKey> call = mongoAPI.updatePixKey(cnpj, id, pixKey);
        call.enqueue(new Callback<PixKey>() {
            @Override
            public void onResponse(Call<PixKey> call, Response<PixKey> response) {
                if (response.isSuccessful()) {
                }
            }
            @Override
            public void onFailure(Call<PixKey> call, Throwable t) {
            }
        });
    }

    public void updateResidue(String cnpj, String id, Residue residue, Context context) {
        Call<Residue> call = mongoAPI.updateResidue(cnpj, id, residue);
        call.enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(Call<Residue> call, Response<Residue> response) {
                if (response.isSuccessful()) {
                }
            }
            @Override
            public void onFailure(Call<Residue> call, Throwable t) {
            }
        });
    }

    // DELETE

    public void deleteAddress(String cnpj, String id, Context context) {
        Call<Void> call = mongoAPI.deleteAddress(cnpj, id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Endereço deletado com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Erro ao deletar endereço", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteCompany(String cnpj, Context context) {
        Call<Void> call = mongoAPI.deleteCompany(cnpj);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Empresa deletada com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Erro ao deletar empresa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deletePixKey(String cnpj, String id, Context context) {
        Call<Void> call = mongoAPI.deletePixKey(cnpj, id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Chave deletada com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Erro ao deletar chave: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Erro ao deletar chave: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteResidue(String cnpj, String id, Context context) {
        Call<Void> call = mongoAPI.deleteResidue(cnpj, id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Resíduo deletado com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Erro ao deletar resíduo", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
