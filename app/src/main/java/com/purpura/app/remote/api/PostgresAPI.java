package com.purpura.app.remote.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import com.purpura.app.model.mongo.Residue;
import com.purpura.app.model.postgres.News;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.model.postgres.Payment;
import com.purpura.app.remote.util.Api;

import java.util.List;

@Api("https://api-pg-purpura.onrender.com/")
public interface PostgresAPI {

    //GET
    @GET("pedido/all")
    Call<ResponseBody> getAllPedidos();

    @GET("pedido/compras/{compradorId}")
    Call<List<Order>> getComprasByComprador(
            @Path("compradorId") String compradorId
    );

    @GET("pedido/vendas/{vendedorId}")
    Call<List<Order>> getVendasByVendedor(
            @Path("vendedorId") String vendedorId
    );

    @GET("pedido/{id}")
    Call<Order> getPedidoById(
            @Path("id") Integer id
    );

    @GET("pedido/{pedidoId}/residuo/all")
    Call<List<Residue>> getAllResiduos(
            @Path("pedidoId") Integer pedidoId
    );

    @GET("pagamento/pedido/{pedidoId}/all")
    Call<ResponseBody> getPagamentosByPedido(
            @Path("pedidoId") Integer pedidoId
    );

    @GET("pagamento/{pagamentoId}")
    Call<Payment> getPagamentoById(
            @Path("pagamentoId") Integer pagamentoId
    );

    @GET("noticia")
    Call<List<News>> getNoticias();


    //POST
    @POST("pedido")
    Call<Order> createPedido(@Body Order order);

    @POST("pedido/{pedidoId}/residuo")
    Call<ResponseBody> createResiduo(
            @Path("pedidoId") Integer pedidoId
    );

    @POST("pagamento")
    Call<Payment> createPagamento(@Body Payment payment);

    //PUT

    @PUT("pedido/{id}")
    Call<ResponseBody> updateOrder(
            @Path("id") Integer id,
            @Body Order order
    );

    @PUT("pedido/{pedidoId}/residuo/{residuoId}")
    Call<ResponseBody> updateResidue(
            @Path("pedidoId") int pedidoId,
            @Path("residuoId") int residuoId,
            @Body Residue residue
    );

    //DELETE

    @DELETE("pedido/{id}")
        Call<ResponseBody> deletePedido(
            @Path("id") Integer id
    );

    @DELETE("pedido/{pedidoId}/residuo/{residuoId}")
    Call<ResponseBody> deleteResiduo(
            @Path("pedidoId") int pedidoId,
            @Path("residuoId") int residuoId
    );

    //PATCH

    @PATCH("pedido/{id}/cancelar")
    Call<ResponseBody> cancelarPedido(
            @Path("id") Integer id
    );

    @PATCH("pedido/{id}/aprovar")
    Call<ResponseBody> aprovarPedido(
            @Path("id") Integer id
    );

    @PATCH("pedido/{id}/concluir")
    Call<ResponseBody> concluirPedido(
            @Path("id") Integer id
    );

    @PATCH("pagamento/{pagamentoId}/cancelar")
    Call<ResponseBody> cancelarPagamento(
            @Path("pagamentoId") Integer pagamentoId
    );

    @PATCH("pagamento/{pagamentoId}/concluir")
    Call<ResponseBody> concluirPagamento(
            @Path("pagamentoId") Integer pagamentoId
    );


}
