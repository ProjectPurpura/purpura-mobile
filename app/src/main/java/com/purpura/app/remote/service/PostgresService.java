package com.purpura.app.remote.service;

import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.model.postgres.News;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.model.postgres.Payment;
import com.purpura.app.remote.api.PostgresAPI;
import com.purpura.app.remote.util.RetrofitService;

import java.util.List;

import retrofit2.Call;

public class PostgresService {

    private PostgresAPI postgresAPI;
    private Methods methods;

    public PostgresService(){
        postgresAPI = new RetrofitService<>(PostgresAPI.class).getService();
    }

    //GET

    public Call<List<News>> getAllNotifications(){
        Call<List<News>> call = postgresAPI.getNoticias();
        return call;
    }

    public Call<List<Order>> getOrdersByClient(String id){
        Call<List<Order>> call = postgresAPI.getComprasByComprador(id);
        return call;
    }

    public Call<List<Order>> getOrdersBySeller(String id){
        Call<List<Order>> call = postgresAPI.getVendasByVendedor(id);
        return call;
    }

    public Call<Order> getOrderById(int id){
        Call<Order> call = postgresAPI.getPedidoById(id);
        return call;
    }

    public Call<List<Residue>> getAllResidues(Integer orderId){
        Call<List<Residue>> call = postgresAPI.getAllResiduos(orderId);
        return call;
    }

    public Call<List<Order>> getOrdersByClientAndSeller(String id){
        Call<List<Order>> call = postgresAPI.getComprasByComprador(id);
        return call;
    }

    public Call<Payment> getPaymentById(Integer id){
        Call<Payment> call = postgresAPI.getPagamentoById(id);
        return call;
    }

}
