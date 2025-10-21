package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.adapters.postgres.SalesAdapter;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.remote.service.PostgresService;
import com.purpura.app.ui.screens.errors.GenericError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MySalesFragment extends Fragment {

    private RecyclerView recyclerView;
    private Methods methods = new Methods();
    private final PostgresService postgresService = new PostgresService();
    private Call<List<Order>> salesCall;

    public MySalesFragment() {}

    public static MySalesFragment newInstance(String param1, String param2) {
        MySalesFragment fragment = new MySalesFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_sales, container, false);

        recyclerView = view.findViewById(R.id.mySalesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        SalesAdapter adapter = new SalesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadSales();

        return view;
    }

    private void loadSales() {
        try {
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            loadOrders(cnpj);
                        }
                    })
                    .addOnFailureListener(e -> {
                        methods.openScreenFragments(this, GenericError.class);
                    });

        } catch (Exception e) {
            methods.openScreenFragments(this, GenericError.class);
        }
    }

    private void loadOrders(String cnpj) {
        salesCall = postgresService.getOrdersBySeller(cnpj);
        salesCall.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    SalesAdapter adapter = new SalesAdapter(orders);
                    recyclerView.setAdapter(adapter);
                } else {
                    methods.openScreenFragments(MySalesFragment.this, GenericError.class);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                methods.openScreenFragments(MySalesFragment.this, GenericError.class);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (salesCall != null && !salesCall.isCanceled()) {
            salesCall.cancel();
        }
    }
}
