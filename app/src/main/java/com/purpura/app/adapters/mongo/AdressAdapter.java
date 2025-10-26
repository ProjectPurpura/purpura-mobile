package com.purpura.app.adapters.mongo;

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
import com.purpura.app.model.mongo.Adress;
import com.purpura.app.remote.service.MongoService;

import java.util.ArrayList;
import java.util.List;

public class AdressAdapter extends RecyclerView.Adapter<AdressAdapter.AddressViewHolder> {

    private List<Adress> address;
    private final Methods methods = new Methods();
    private final MongoService mongoService = new MongoService();

    public AdressAdapter(List<Adress> Adresses) {
        this.address = Adresses != null ? Adresses : new ArrayList<>();
    }

    @NonNull
    @Override
    public AdressAdapter.AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adresses_card, parent, false);
        return new AdressAdapter.AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Adress address1 = address.get(position);

        holder.addressCardName.setText(address1.getNome());
        holder.addresCardZipCode.setText(address1.getCep());

        holder.addressCardButtonEdit.setOnClickListener(v -> {
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
                                mongoService.deleteAddress(cnpj, address1.getId(), v.getContext());
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

    public void updateList(List<Adress> newAddress) {
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