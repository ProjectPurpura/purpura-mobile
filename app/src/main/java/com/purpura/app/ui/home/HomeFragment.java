package com.purpura.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.adapters.mongo.HomeAdapter;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.ProductPage;
import com.purpura.app.ui.screens.errors.GenericError;
import com.purpura.app.ui.screens.errors.InternetError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAdapter adapter;

    Methods methods = new Methods();
    private final MongoService mongoService = new MongoService();
    private Call<List<Residue>> residuosCall;

    private String currentCompanyCnpj = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = view.findViewById(R.id.homeRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new HomeAdapter(new ArrayList<>(), residue -> {
            Intent rota = new Intent(requireContext(), ProductPage.class);
            rota.putExtra("residue", residue);
            String cnpjFromResidue = residue.getCnpj();
            rota.putExtra("cnpj", sanitizeCnpj(cnpjFromResidue));
            startActivity(rota);
        });

        recyclerView.setAdapter(adapter);

        loadResidues(this);
    }

    private void loadResidues(Fragment fragment) {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                if (isAdded())
                    Toast.makeText(requireContext(), "Usuário não autenticado", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (!isAdded()) return;
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            if (cnpj == null || cnpj.isEmpty()) {
                                Toast.makeText(requireContext(), "CNPJ não encontrado", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            currentCompanyCnpj = sanitizeCnpj(cnpj);

                            int limit = 50;
                            int page = 1;
                            residuosCall = mongoService.getAllResiduosMain(currentCompanyCnpj, limit, page);
                            residuosCall.enqueue(new Callback<List<Residue>>() {
                                @Override
                                public void onResponse(Call<List<Residue>> call, Response<List<Residue>> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful() && response.body() != null) {
                                        adapter.updateList(response.body());
                                    } else {
                                        methods.openScreenFragments(fragment, GenericError.class);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Residue>> call, Throwable t) {
                                    methods.openScreenFragments(HomeFragment.this, GenericError.class);
                                }
                            });
                        }
                    });
        } catch (Exception ignored) {
            methods.openScreenFragments(HomeFragment.this, GenericError.class);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (residuosCall != null) {
            residuosCall.cancel();
            residuosCall = null;
        }
    }

    private static String sanitizeCnpj(String c) { return c == null ? "" : c.replaceAll("\\D+", ""); }
}
