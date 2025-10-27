package com.purpura.app.adapters.postgres;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.model.postgres.OrderItem;
import com.purpura.app.remote.service.MongoService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.VH> {

    private final List<OrderItem> items;
    private final MongoService mongoService;
    private final String cnpj;
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    public OrderItemsAdapter(@NonNull List<OrderItem> items,
                             @NonNull String cnpj,
                             @NonNull MongoService mongoService) {
        this.items = items != null ? items : new ArrayList<>();
        this.cnpj = cnpj;
        this.mongoService = mongoService;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.individual_order_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderItem it = items.get(position);

        h.quantity.setText(String.valueOf(it.getQuantidade()));
        h.unit.setText(it.getTipoUnidade() == null ? "-" : it.getTipoUnidade());
        h.weight.setText(String.valueOf(it.getPeso()));
        h.value.setText(money.format(it.getPreco()));

        h.title.setText("Carregando resíduo...");
        Glide.with(h.image.getContext())
                .load(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(h.image);

        Call<Residue> call = mongoService.getResidueById(cnpj, it.getIdResiduo());
        call.enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(@NonNull Call<Residue> call, @NonNull Response<Residue> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    bindError(h, "Erro ao carregar resíduo");
                    return;
                }
                int adapterPos = h.getBindingAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                OrderItem current = items.get(adapterPos);
                if (!it.getIdResiduo().equals(current.getIdResiduo())) return;

                Residue residue = response.body();
                h.title.setText(residue.getNome() == null ? "Sem nome" : residue.getNome());

                String url = residue.getUrlFoto();
                Glide.with(h.image.getContext())
                        .load(url)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(h.image);
            }

            @Override
            public void onFailure(@NonNull Call<Residue> call, @NonNull Throwable t) {
                bindError(h, "Erro ao carregar resíduo");
            }
        });
    }

    private void bindError(@NonNull VH h, @NonNull String msg) {
        h.title.setText(msg);
        Glide.with(h.image.getContext())
                .load(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(h.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return (items.get(position).getIdResiduo() + "_" + position).hashCode();
    }

    public void updateList(List<OrderItem> list) {
        this.items.clear();
        if (list != null) this.items.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, weight, unit, value, quantity;
        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.individualOrderCartImage);
            title = itemView.findViewById(R.id.individualOrderCartCardTitle);
            weight = itemView.findViewById(R.id.individualOrderCardWeight);
            unit = itemView.findViewById(R.id.individualOrderCartWeightMeasure);
            value = itemView.findViewById(R.id.individualOrderCardValue);
            quantity = itemView.findViewById(R.id.individualOrderCartQuantity);
        }
    }
}
