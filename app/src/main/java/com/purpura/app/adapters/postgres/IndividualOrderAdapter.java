package com.purpura.app.adapters.postgres;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.purpura.app.R;
import com.purpura.app.model.postgres.order.OrderResponse;

import java.util.List;

public class IndividualOrderAdapter extends RecyclerView.Adapter<IndividualOrderAdapter.IndividualOrderViewHolder> {

    private final List<OrderResponse> orderList;

    public IndividualOrderAdapter(List<OrderResponse> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public IndividualOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.individual_order_card, parent, false);
        return new IndividualOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IndividualOrderViewHolder holder, int position) {
        OrderResponse order = orderList.get(position);

        if (order != null) {
            holder.orderCardId.setText(order.getIdPedido().toString());
            holder.orderCardTotal.setText(order.getValorTotal().toString());
            holder.orderCardPaymentStatus.setText(order.getStatus());
            holder.orderCardDate.setText(order.getData() != null ? order.getData().toString() : "Sem data");
            holder.orderCardObservations.setText(order.getObservacoes() != null ? order.getObservacoes() : "-");
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class IndividualOrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderCardId, orderCardTotal, orderCardPaymentStatus, orderCardDate, orderCardObservations;

        public IndividualOrderViewHolder(@NonNull View itemView) {
            super(itemView);

            orderCardId = itemView.findViewById(R.id.myOrdersCardId);
            orderCardTotal = itemView.findViewById(R.id.myOrdersCardTotal);
            orderCardPaymentStatus = itemView.findViewById(R.id.myOrderCardPaymentStatus);
            orderCardDate = itemView.findViewById(R.id.myOrderCardDate);
            orderCardObservations = itemView.findViewById(R.id.myOrderCardObservations);
        }
    }
}
