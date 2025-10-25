package com.purpura.app.adapters.postgres;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    }

    @NonNull
    @Override
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
    }

    @Override
    public int getItemCount() {
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