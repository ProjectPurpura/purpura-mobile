package com.purpura.app.remote.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import com.purpura.app.configuration.EnvironmentVariables;
import com.purpura.app.model.postgres.News;
import com.purpura.app.model.postgres.order.OrderRequest;
import com.purpura.app.model.postgres.order.OrderResponse;
import com.purpura.app.model.postgres.order.OrderItem;
import com.purpura.app.model.postgres.Payment;
import com.purpura.app.remote.util.Api;

import java.util.List;

@Api(value = EnvironmentVariables.POSTGRES_URL)
public interface PostgresAPI {

    @GET("pedido/all")
    Call<ResponseBody> getAllPedidos();

    @GET("pedido/compras/{compradorId}")
    Call<List<OrderResponse>> getComprasByComprador(
            @Path("compradorId") String compradorId
    );

    @GET("pedido/vendas/{vendedorId}")
    Call<List<OrderResponse>> getVendasByVendedor(
            @Path("vendedorId") String vendedorId
    );

    @GET("pedido/{id}")
    Call<OrderResponse> getPedidoById(
            @Path("id") Integer id
    );

    @GET("pedido/{pedidoId}/residuo/all")
    Call<List<OrderItem>> getOrderItems(
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

    @POST("pedido")
    Call<OrderResponse> createPedido(@Body OrderRequest order);

    @POST("pedido/{pedidoId}/residuo")
    Call<OrderItem> createResiduo(
            @Path("pedidoId") Integer pedidoId,
            @Body OrderItem orderItem
    );

    @POST("pagamento")
    Call<Payment> createPagamento(@Body Payment payment);

    @PUT("pedido/{id}")
    Call<ResponseBody> updateOrder(
            @Path("id") Integer id,
            @Body OrderResponse order
    );

    @PUT("pedido/{pedidoId}/residuo/{residuoId}")
    Call<ResponseBody> updateResidue(
            @Path("pedidoId") int pedidoId,
            @Path("residuoId") int residuoId,
            @Body OrderItem residue
    );

    @DELETE("pedido/{id}")
    Call<Void> deletePedido(
            @Path("id") Integer id
    );

    @DELETE("pedido/{pedidoId}/residuo/{residuoId}")
    Call<ResponseBody> deleteResiduo(
            @Path("pedidoId") int pedidoId,
            @Path("residuoId") int residuoId
    );

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
