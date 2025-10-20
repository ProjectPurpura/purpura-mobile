package com.purpura.app.adapters.mongo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.remote.service.MongoService;

import java.util.ArrayList;
import java.util.List;

public class PixKeyAdapter extends RecyclerView.Adapter<PixKeyAdapter.PixKeyViewHolder> {

    private List<PixKey> pixKeys;
    private final Methods methods = new Methods();
    private final MongoService mongoService = new MongoService();

    public PixKeyAdapter(List<PixKey> pixKeys) {
        this.pixKeys = pixKeys != null ? pixKeys : new ArrayList<>();
    }

    @NonNull
    @Override
    public PixKeyAdapter.PixKeyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pix_key_card, parent, false);
        return new PixKeyAdapter.PixKeyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PixKeyViewHolder holder, int position) {
        PixKey pixKey = pixKeys.get(position);

        holder.pixKeyCardName.setText(pixKey.getName());
        holder.pixKeyCardPixKey.setText(pixKey.getKey());

        holder.pixKeyCardButtonEdit.setOnClickListener(v -> {
        });

        holder.pixKeyCardDeleteButton.setOnClickListener(v -> {
            try {
                FirebaseFirestore.getInstance()
                        .collection("empresa")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                mongoService.deletePixKey(cnpj, pixKey.getId(), v.getContext());
                                pixKeys.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, pixKeys.size());
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public int getItemCount() {
        return pixKeys != null ? pixKeys.size() : 0;
    }

    public void updateList(List<PixKey> newPixKeys) {
        this.pixKeys = newPixKeys != null ? newPixKeys : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class PixKeyViewHolder extends RecyclerView.ViewHolder {

        TextView pixKeyCardName;
        ImageView pixKeyCardButtonEdit;
        TextView pixKeyCardPixKey;
        ImageView pixKeyCardDeleteButton;

        public PixKeyViewHolder(@NonNull View itemView) {
            super(itemView);
            pixKeyCardName = itemView.findViewById(R.id.pixKeyCardName);
            pixKeyCardButtonEdit = itemView.findViewById(R.id.pixKeyCardButtonEdit);
            pixKeyCardPixKey = itemView.findViewById(R.id.pixKeyCardPixKey);
            pixKeyCardDeleteButton = itemView.findViewById(R.id.pixKeyCardDeletePixKey);
        }
    }
}
