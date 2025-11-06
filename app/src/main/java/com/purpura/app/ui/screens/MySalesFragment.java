package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Importado
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.adapters.postgres.SalesAdapter;
import com.purpura.app.model.postgres.order.OrderResponse;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MySalesFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyMessage; // View para a mensagem amigável
    private SalesAdapter adapter;
    private final PostgresService service = new PostgresService();
    private final MongoService mongoService = new MongoService();
    private Call<List<OrderResponse>> salesCall;
    private String cnpj;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_sales, container, false);

        // Encontra as views
        recyclerView = v.findViewById(R.id.mySalesRecyclerView);
        emptyMessage = v.findViewById(R.id.mySalesEmptyMessage); // Encontra o TextView

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // O Adapter será criado e setado dentro do loadSales, depois que tivermos o CNPJ

        loadSales();
        return v;
    }

    private void loadSales() {
        // Esconde tudo e mostra um "carregando" (implícito, lista vazia)
        recyclerView.setVisibility(View.GONE);
        emptyMessage.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(d -> {
                    if (!isAdded()) return; // Garante que o Fragment ainda existe

                    cnpj = d.getString("cnpj");
                    if (cnpj == null || cnpj.isEmpty()) {
                        Toast.makeText(requireContext(), "CNPJ não encontrado", Toast.LENGTH_SHORT).show();
                        emptyMessage.setText("Erro: CNPJ não encontrado."); // Mostra erro
                        emptyMessage.setVisibility(View.VISIBLE);
                        return;
                    }

                    // AGORA SIM: Cria o adapter com o CNPJ correto
                    adapter = new SalesAdapter(new ArrayList<>(), service, cnpj, mongoService, this.getActivity());
                    recyclerView.setAdapter(adapter);

                    salesCall = service.getOrdersBySeller(cnpj);
                    salesCall.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<List<OrderResponse>> call, @NonNull Response<List<OrderResponse>> response) {
                            if (!isAdded()) return;

                            List<OrderResponse> sales = response.body();

                            if (response.isSuccessful() && sales != null && !sales.isEmpty()) {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyMessage.setVisibility(View.GONE);
                                adapter.updateList(sales);
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                emptyMessage.setVisibility(View.VISIBLE);

                                if (!response.isSuccessful()) {
                                    emptyMessage.setText("Erro ao carregar vendas (HTTP " + response.code() + ")");
                                } else {
                                    emptyMessage.setText("Você ainda não fez nenhuma venda 💸");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<OrderResponse>> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            // Erro de API
                            recyclerView.setVisibility(View.GONE);
                            emptyMessage.setVisibility(View.VISIBLE);
                            emptyMessage.setText("Erro de conexão. Tente novamente.");
                            Toast.makeText(requireContext(), "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    // Erro de Firestore
                    recyclerView.setVisibility(View.GONE);
                    emptyMessage.setVisibility(View.VISIBLE);
                    emptyMessage.setText("Erro ao buscar dados da empresa.");
                    Toast.makeText(requireContext(), "Erro empresa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (salesCall != null && !salesCall.isCanceled()) salesCall.cancel();
    }
}