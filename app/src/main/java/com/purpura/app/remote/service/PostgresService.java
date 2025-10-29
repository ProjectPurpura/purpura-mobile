package com.purpura.app.remote.service;

import com.purpura.app.model.postgres.News;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.model.postgres.OrderItem;
import com.purpura.app.model.postgres.Payment;
import com.purpura.app.remote.api.PostgresAPI;
import com.purpura.app.remote.util.RetrofitService;

import java.util.List;

import retrofit2.Call;

public class PostgresService {

    private final PostgresAPI postgresAPI;

    public PostgresService(){
        postgresAPI = new RetrofitService<>(PostgresAPI.class).getService();
    }

    public Call<List<News>> getAllNotifications(){
        return postgresAPI.getNoticias();
    }

    public Call<List<Order>> getOrdersByClient(String id){
        return postgresAPI.getComprasByComprador(id);
    }

    public Call<List<Order>> getOrdersBySeller(String id){
        return postgresAPI.getVendasByVendedor(id);
    }

    public Call<Order> getOrderById(int id){
        return postgresAPI.getPedidoById(id);
    }

    public Call<List<OrderItem>> getOrderItems(Integer orderId){
        return postgresAPI.getOrderItems(orderId);
    }

    public Call<List<Order>> getOrdersByClientAndSeller(String id){
        return postgresAPI.getComprasByComprador(id);
    }

    public Call<Payment> getPaymentById(Integer id){
        return postgresAPI.getPagamentoById(id);
    }

    //POST
    public Call<Order> createOrder(Order order) {return postgresAPI.createPedido(order);}
    public Call<OrderItem> addItemOrder(OrderItem orderItem, Integer orderId) {return postgresAPI.createResiduo(orderId, orderItem);}
}
