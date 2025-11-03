package com.purpura.app.adapters.postgres;

import android.app.Activity;
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
        if (pay) {
            h.payOrderButton.setEnabled(true);
        }

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

        h.payOrderButton.setOnClickListener(v -> {
            service.getOrderItems(Integer.parseInt(h.id.getText().toString())).enqueue(new Callback<List<OrderItem>>() {
                @Override
                public void onResponse(@NonNull Call<List<OrderItem>> call, @NonNull Response<List<OrderItem>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        List<OrderItem> orderItems = response.body();
                        String residueId = orderItems.get(0).getIdResiduo();
                        String sellerCnpj = o.getIdVendedor() == null || o.getIdVendedor().isEmpty() ? PurchaseAdapter.this.cnpj : o.getIdVendedor();
                        Double total = o.getValorTotal();
                        getPixKey(activity, residueId, sellerCnpj, total == null ? 0d : total, o.getIdPedido());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<OrderItem>> call, @NonNull Throwable t) { }
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

    public void getPixKey(Activity activity, String residueId, String cnpj, double total, int orderId) {
        mongoService.getResidueById(cnpj, residueId).enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(@NonNull Call<Residue> call, @NonNull Response<Residue> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("pix", response.body().getIdChavePix());
                    bundle.putDouble("total", total);
                    bundle.putInt("orderId", orderId);
                    methods.openScreenActivityWithBundle(activity, QrCodePayment.class, bundle);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Residue> call, @NonNull Throwable t) { }
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
