package com.purpura.app.adapters.postgres;

<<<<<<< HEAD
=======
import android.annotation.SuppressLint;
>>>>>>> 55922afc56f3b3de86afc13c86e5d0720ac1f19d
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
<<<<<<< HEAD

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.purpura.app.R;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.model.postgres.OrderItem;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.VH> {

    private List<Order> orders;
    private final PostgresService service;
    private final String cnpj;
    private final MongoService mongoService;

    public PurchaseAdapter(List<Order> orders, PostgresService service, String cnpj, MongoService mongoService) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.service = service;
        this.cnpj = cnpj;
        this.mongoService = mongoService;
=======
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.purpura.app.R;
import com.purpura.app.model.postgres.Order;

import java.util.ArrayList;
import java.util.List;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

    private List<Order> sales;

    public PurchaseAdapter(List<Order> listOrders) {
        this.sales = listOrders != null ? listOrders : new ArrayList<>();
>>>>>>> 55922afc56f3b3de86afc13c86e5d0720ac1f19d
    }

    @NonNull
    @Override
<<<<<<< HEAD
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_orders_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Order o = orders.get(position);
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
=======
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_orders_card, parent, false);
        return new PurchaseViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        Order order = sales.get(position);

        holder.mySalesCardId.setText(String.valueOf(order.getIdPedido()));
        holder.mySalesCardTotal.setText(order.getValorTotal().toString());
        holder.myOrderCardPaymentStatus.setText(order.getStatus() != null ? order.getStatus() : "Sem status");
        holder.myOrderCardDate.setText(order.getData().toString());
        holder.myOrderCardObservations.setText(order.getObservacoes() != null ? order.getObservacoes() : "Sem observações");

        if (holder.mySalesRecicleView.getLayoutManager() == null) {
            holder.mySalesRecicleView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        }
>>>>>>> 55922afc56f3b3de86afc13c86e5d0720ac1f19d
    }

    @Override
    public int getItemCount() {
<<<<<<< HEAD
        return orders != null ? orders.size() : 0;
    }

    public void updateList(List<Order> list) {
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
=======
        return sales != null ? sales.size() : 0;
    }

    public void updateList(List<Order> listOrders) {
        this.sales = listOrders != null ? listOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        RecyclerView mySalesRecicleView;
        TextView mySalesCardId;
        TextView mySalesCardTotal;
        TextView myOrderCardPaymentStatus;
        TextView myOrderCardDate;
        TextView myOrderCardObservations;

        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            mySalesRecicleView = itemView.findViewById(R.id.myOrdersCardReciclerView);
            mySalesCardId = itemView.findViewById(R.id.myOrdersCardId);
            mySalesCardTotal = itemView.findViewById(R.id.myOrdersCardTotal);
            myOrderCardPaymentStatus = itemView.findViewById(R.id.myOrderCardPaymentStatus);
            myOrderCardDate = itemView.findViewById(R.id.myOrderCardDate);
            myOrderCardObservations = itemView.findViewById(R.id.myOrderCardObservations);
        }
    }
}
>>>>>>> 55922afc56f3b3de86afc13c86e5d0720ac1f19d
