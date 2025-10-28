package com.purpura.app.adapters.mongo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.accountFeatures.MyProducts;
import com.purpura.app.ui.screens.productRegister.UpdateProduct;

import java.util.ArrayList;
import java.util.List;


public class MyResiduesAdapter extends RecyclerView.Adapter<MyResiduesAdapter.ResidueViewHolder> {

    private List<Residue> products;
    private final Methods methods = new Methods();
    Bundle bundle = new Bundle();
    private final MongoService mongoService = new MongoService();
    private Activity activity;
    public MyResiduesAdapter(List<Residue> products, Activity activity) {
        this.activity = activity;
        this.products = products != null ? products : new ArrayList<>();
    }

    @NonNull
    @Override
    public ResidueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_products_card, parent, false);
        return new ResidueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResidueViewHolder holder, int position) {
        Residue residue = products.get(position);

        Glide.with(holder.residueImage.getContext())
                .load(residue.getUrlFoto())
                .into(holder.residueImage);

        holder.residueName.setText(residue.getNome());

        holder.editResidueButton.setOnClickListener(v -> {
            bundle.putSerializable("residueId", residue.getId());
            bundle.putString("pixKeyId", residue.getIdChavePix());
            bundle.putString("addressId", residue.getIdEndereco());
            methods.openScreenActivityWithBundle(activity, UpdateProduct.class, bundle);
        });

        holder.deleteResidueButton.setOnClickListener(v -> {
            try {
                FirebaseFirestore.getInstance()
                        .collection("empresa")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                bundle.putString("cnpj", cnpj);
                                mongoService.deleteResidue(cnpj, residue.getId(), v.getContext());
                                products.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, products.size());
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateList(List<Residue> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ResidueViewHolder extends RecyclerView.ViewHolder {

        ImageView residueImage;
        TextView residueName;
        ImageView editResidueButton;
        ImageView deleteResidueButton;

        public ResidueViewHolder(@NonNull View itemView) {
            super(itemView);
            residueImage = itemView.findViewById(R.id.myProductCardProductImage);
            residueName = itemView.findViewById(R.id.myProductsCardProductName);
            editResidueButton = itemView.findViewById(R.id.productCardEditResidueButton);
            deleteResidueButton = itemView.findViewById(R.id.productCardDeleteResidueButton);
        }
    }
}
