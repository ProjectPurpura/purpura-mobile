package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.adapters.postgres.SalesAdapter;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MySalesFragment extends Fragment {

    private RecyclerView recyclerView;
    private SalesAdapter adapter;
    private final PostgresService service = new PostgresService();
    private final MongoService mongoService = new MongoService();
    private Call<List<Order>> salesCall;
    private String cnpj;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_sales, container, false);
        recyclerView = v.findViewById(R.id.mySalesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SalesAdapter(new ArrayList<>(), service, "", mongoService);
        recyclerView.setAdapter(adapter);
        loadSales();
        return v;
    }

    private void loadSales() {
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(d -> {
                    cnpj = d.getString("cnpj");
                    if (cnpj == null || cnpj.isEmpty()) {
                        Toast.makeText(requireContext(), "CNPJ não encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    salesCall = service.getOrdersBySeller(cnpj);

                    adapter = new SalesAdapter(new ArrayList<>(), service, cnpj, mongoService);
                    recyclerView.setAdapter(adapter);

                    salesCall.enqueue(new Callback<List<Order>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null) {
                                adapter.updateList(response.body());
                            } else {
                                Toast.makeText(requireContext(), "HTTP " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Erro empresa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (salesCall != null && !salesCall.isCanceled()) salesCall.cancel();
    }
}
