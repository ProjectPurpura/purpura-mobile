package com.purpura.app.adapters.postgres;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.model.postgres.order.OrderItem;
import com.purpura.app.remote.service.MongoService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.VH> {

    private final List<OrderItem> items;
    private final MongoService mongoService;
    private final String cnpj;
    private final DecimalFormat money = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("pt", "BR"));
    private final Map<String, Residue> cache = new HashMap<>();

    {
        money.applyPattern("#,##0.00");
    }

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_order_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OrderItem it = items.get(position);

        h.currentCall = null;

        h.quantity.setText(it.getQuantidade() == null ? "-" : String.valueOf(it.getQuantidade()));
        h.unit.setText(it.getTipoUnidade() == null ? "-" : it.getTipoUnidade());
        h.weight.setText(it.getPeso() == null ? "-" : String.valueOf(it.getPeso()));
        h.value.setText(it.getPreco() == null ? "-" : money.format(it.getPreco()));

        h.title.setText("Carregando resíduo...");
        Glide.with(h.image.getContext())
                .load(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(h.image);

        String resId = it.getIdResiduo();
        if (resId == null || resId.isEmpty()) {
            bindError(h, "Resíduo inválido");
            return;
        }

        Residue cached = cache.get(resId);
        if (cached != null) {
            bindResidue(h, cached);
            return;
        }

        Call<Residue> call = mongoService.getResidueById(cnpj, resId);
        h.currentCall = call;
        call.enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(@NonNull Call<Residue> c, @NonNull Response<Residue> response) {
                if (c != h.currentCall) return;
                if (!response.isSuccessful() || response.body() == null) {
                    bindError(h, "Erro ao carregar resíduo");
                    return;
                }
                Residue residue = response.body();
                cache.put(resId, residue);
                if (h.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    bindResidue(h, residue);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Residue> c, @NonNull Throwable t) {
                if (c != h.currentCall) return;
                bindError(h, "Erro ao carregar resíduo");
            }
        });
    }

    private void bindResidue(@NonNull VH h, @NonNull Residue residue) {
        h.title.setText(residue.getNome() == null ? "Sem nome" : residue.getNome());
        Glide.with(h.image.getContext())
                .load(residue.getUrlFoto())
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(h.image);
    }

    private void bindError(@NonNull VH h, @NonNull String msg) {
        h.title.setText(msg);
        Glide.with(h.image.getContext())
                .load(R.drawable.ic_image_placeholder)
                .circleCrop()
                .into(h.image);
    }

    @Override
    public void onViewRecycled(@NonNull VH h) {
        super.onViewRecycled(h);
        if (h.currentCall != null) h.currentCall.cancel();
        h.currentCall = null;
        Glide.with(h.image.getContext()).clear(h.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        String id = items.get(position).getIdResiduo();
        return (id == null || id.isEmpty()) ? position : id.hashCode();
    }

    public void updateList(List<OrderItem> list) {
        this.items.clear();
        if (list != null) this.items.addAll(list);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, weight, unit, value, quantity;
        @Nullable Call<Residue> currentCall;
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
