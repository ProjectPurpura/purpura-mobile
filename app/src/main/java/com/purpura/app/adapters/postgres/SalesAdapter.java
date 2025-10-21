package com.purpura.app.adapters.postgres;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.remote.service.PostgresService;

import java.util.ArrayList;
import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> {

    private List<Order> orders;
    private IndividualOrderAdapter individualOrderAdapter;

    public SalesAdapter(List<Order> listOrders) {
        this.orders = listOrders != null ? listOrders : new ArrayList<>();
    }

    @NonNull
    @Override
    public SalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_orders_card, parent, false);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesViewHolder holder, int position) {
        Order order2 = orders.get(position);

        holder.mySalesRecicleView.setAdapter(individualOrderAdapter);
        holder.mySalesCardTotal.setText(order2.getValorTotal().toString());
        holder.myOrderCardPaymentStatus.setText(order2.getStatus());
        holder.mySalesCardId.setText(order2.getIdPedido().toString());
        holder.myOrderCardDate.setText(order2.getData().toString());
        holder.myOrderCardObservations.setText(order2.getObservacoes());

    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateList(List<Order> listOrders) {
        this.orders = listOrders != null ? listOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class SalesViewHolder extends RecyclerView.ViewHolder {

        RecyclerView mySalesRecicleView;
        TextView mySalesCardId;
        TextView mySalesCardTotal;
        TextView myOrderCardPaymentStatus;
        TextView myOrderCardDate;
        TextView myOrderCardObservations;

        public SalesViewHolder(@NonNull View itemView) {
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
