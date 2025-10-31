package com.purpura.app.adapters.postgres;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.purpura.app.R;
import com.purpura.app.model.postgres.order.OrderResponse;
import com.purpura.app.model.postgres.order.OrderItem;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.VH> {

    private List<OrderResponse> orders;
    private final PostgresService service;
    private final String cnpj;
    private final MongoService mongoService;

    public SalesAdapter(List<OrderResponse> orders, PostgresService service, String cnpj, MongoService mongoService) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.service = service;
        this.cnpj = cnpj;
        this.mongoService = mongoService;
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
        h.id.setText(String.valueOf(o.getIdPedido()));

        String dataBr;
        Object raw = o.getData();
        try {
            if (raw instanceof Number) {
                long v = ((Number) raw).longValue();
                Instant inst = (v < 10_000_000_000L) ? Instant.ofEpochSecond(v) : Instant.ofEpochMilli(v);
                dataBr = inst.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } else {
                String s = String.valueOf(raw).trim();
                try {
                    dataBr = Instant.parse(s).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } catch (Exception e1) {
                    try {
                        dataBr = OffsetDateTime.parse(s).atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    } catch (Exception e2) {
                        LocalDateTime ldt = LocalDateTime.parse(s);
                        dataBr = ldt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    }
                }
            }
        } catch (Exception e) {
            dataBr = "-";
        }

        h.date.setText(dataBr);
        h.status.setText(o.getStatus());
        h.total.setText(String.valueOf(o.getValorTotal()));
        h.obs.setText(o.getObservacoes());

        if (h.items.getLayoutManager() == null) {
            h.items.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        }

        OrderItemsAdapter a = new OrderItemsAdapter(new ArrayList<>(), cnpj, mongoService);
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
            public void onFailure(@NonNull Call<List<OrderItem>> call, @NonNull Throwable t) {}
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateList(List<OrderResponse> list) {
        this.orders = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        RecyclerView items;
        TextView id, total, status, date, obs;

        VH(@NonNull View itemView) {
            super(itemView);
            items = itemView.findViewById(R.id.myOrdersCardReciclerView);
            id = itemView.findViewById(R.id.myOrdersCardId);
            total = itemView.findViewById(R.id.myOrdersCardTotal);
            status = itemView.findViewById(R.id.myOrderCardPaymentStatus);
            date = itemView.findViewById(R.id.myOrderCardDate);
            obs = itemView.findViewById(R.id.myOrderCardObservations);
        }
    }
}
