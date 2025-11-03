package com.purpura.app.adapters.postgres;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.model.postgres.order.OrderItem;
import com.purpura.app.model.postgres.order.OrderResponse;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;
import com.purpura.app.ui.screens.QrCodePayment;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.VH> {

    private List<OrderResponse> orders;
    private final PostgresService service;
    private final String cnpj;
    Activity activity;
    private final MongoService mongoService;
    Methods methods = new Methods();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat numberFmt = (DecimalFormat) DecimalFormat.getNumberInstance(new Locale("pt", "BR"));

    public PurchaseAdapter(List<OrderResponse> orders, Activity activity, PostgresService service, String cnpj, MongoService mongoService) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.service = service;
        this.activity = activity;
        this.cnpj = cnpj;
        this.mongoService = mongoService;
        numberFmt.applyPattern("#,##0.00");
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_orders_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderResponse o = orders.get(position);
        h.payOrderButton.setEnabled(false);

        h.id.setText(String.valueOf(o.getIdPedido()));
        h.status.setText(o.getStatus() == null ? "-" : o.getStatus().toUpperCase());
        h.total.setText(o.getValorTotal() == null ? "-" : numberFmt.format(o.getValorTotal()));

        boolean pay = "APROVADO".equals(h.status.getText().toString());

        if(pay){
            h.payOrderButton.setEnabled(true);
        }

        h.payOrderButton.setOnClickListener(v -> {
            service.getOrderItems(Integer.valueOf(h.id.getText().toString())).enqueue(new Callback<List<OrderItem>>() {
                @Override
                public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                    if(response.isSuccessful()){
                        List<OrderItem> orders = response.body();
                        getPixKey(activity, String.valueOf(orders.get(0).getId()), "");
                    }
                }

                @Override
                public void onFailure(Call<List<OrderItem>> call, Throwable t) {

                }
            });
        });
        h.deleteButton.setOnClickListener(v -> {
            Call<Void> del = service.deleteOrderByOrderId(o.getIdPedido());
            del.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        int pos = h.getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            orders.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) { }
            });
        });

        h.items.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        h.items.setNestedScrollingEnabled(false);

        String sellerCnpj = o.getIdVendedor();
        if (sellerCnpj == null || sellerCnpj.isEmpty()) {
            sellerCnpj = this.cnpj;
        }

        OrderItemsAdapter a = new OrderItemsAdapter(new ArrayList<>(), sellerCnpj, mongoService);
        h.items.setAdapter(a);

        Call<List<OrderItem>> c = service.getOrderItems(o.getIdPedido());
        c.enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderItem>> call, @NonNull Response<List<OrderItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    a.updateList(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<OrderItem>> call, @NonNull Throwable t) { }
        });
    }

    public void getPixKey(Activity activity, String id, String cnpj){
        mongoService.getResidueById(cnpj, id).enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(Call<Residue> call, Response<Residue> response) {
                if(response.isSuccessful() && response.body() != null){
                    Bundle bundle = new Bundle();
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    System.out.println(response.body().getIdChavePix());
                    bundle.putString("pix", response.body().getIdChavePix());
                    methods.openScreenActivityWithBundle(activity, QrCodePayment.class, bundle);
                }
            }

            @Override
            public void onFailure(Call<Residue> call, Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return orders.get(position).getIdPedido();
    }

    public void updateList(List<OrderResponse> list) {
        this.orders = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        RecyclerView items;
        TextView id, total, status, date;
        Button payOrderButton;
        ImageView deleteButton;

        VH(@NonNull View itemView) {
            super(itemView);
            items = itemView.findViewById(R.id.myOrdersCardReciclerView);
            id = itemView.findViewById(R.id.myOrdersCardId);
            total = itemView.findViewById(R.id.myOrdersCardTotal);
            status = itemView.findViewById(R.id.myOrderCardPaymentStatus);
            date = itemView.findViewById(R.id.myOrderCardDate);
            payOrderButton = itemView.findViewById(R.id.payOrderButton);
            deleteButton = itemView.findViewById(R.id.deleteMyOrder);
            items.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            items.setNestedScrollingEnabled(false);
        }
    }
}
