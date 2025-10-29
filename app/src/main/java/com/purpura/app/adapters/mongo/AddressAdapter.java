package com.purpura.app.adapters.mongo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.UpdateAddress;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> address;
    private final Methods methods = new Methods();
    private final MongoService mongoService = new MongoService();
    private final Activity activity;
    private final Bundle bundle = new Bundle();

    public AddressAdapter(List<Address> addresses, Activity activity) {
        this.address = addresses != null ? addresses : new ArrayList<>();
        this.activity = activity;
    }

    @NonNull
    @Override
    public AddressAdapter.AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adresses_card, parent, false);
        return new AddressAdapter.AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address item = address.get(position);

        holder.addressCardName.setText(item.getNome());
        holder.addresCardZipCode.setText(item.getCep());

        holder.addressCardButtonEdit.setOnClickListener(v -> {
            bundle.clear();
            bundle.putString("addressId", item.getId());
            methods.openScreenActivityWithBundle(activity, UpdateAddress.class, bundle);
        });

        holder.addressCardDeleteButton.setOnClickListener(v -> {
            try {
                FirebaseFirestore.getInstance()
                        .collection("empresa")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                mongoService.deleteAddress(cnpj, item.getId(), v.getContext());
                                address.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, address.size());
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return address != null ? address.size() : 0;
    }

    public void updateList(List<Address> newAddress) {
        this.address = newAddress != null ? newAddress : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {

        TextView addressCardName;
        Button addressCardButtonEdit;
        TextView addresCardZipCode;
        ImageView addressCardDeleteButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            addressCardName = itemView.findViewById(R.id.adressCardName);
            addressCardButtonEdit = itemView.findViewById(R.id.adressCardButtonEdit);
            addresCardZipCode = itemView.findViewById(R.id.adressCardZipCode);
            addressCardDeleteButton = itemView.findViewById(R.id.adressCardDeleteAdress);
        }
    }
}
