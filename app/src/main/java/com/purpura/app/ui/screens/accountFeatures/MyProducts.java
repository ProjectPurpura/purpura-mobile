package com.purpura.app.ui.screens.accountFeatures;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.adapters.mongo.MyResiduesAdapter;
import com.purpura.app.configuration.Methods;
import com.purpura.app.databinding.ActivityMyProductsBinding;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;
import com.purpura.app.ui.screens.productRegister.RegisterProduct;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProducts extends AppCompatActivity {

    Methods methods = new Methods();
    private RecyclerView recyclerView;
    MongoService mongoService = new MongoService();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMyProductsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageView backButton = findViewById(R.id.myProductsBackButton);
        backButton.setOnClickListener(v -> finish());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_my_prroducts);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        recyclerView = findViewById(R.id.myProductsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.fab.setOnClickListener(v -> {
            methods.openScreenActivity(this, RegisterProduct.class);
        });

        try {
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            mongoService.getAllResidues(cnpj).enqueue(new Callback<List<Residue>>() {
                                @Override
                                public void onResponse(Call<List<Residue>> call, Response<List<Residue>> response) {
                                    if (response.isSuccessful()) {
                                        List<Residue> residues = response.body();
                                    } else {
                                        Toast.makeText(MyProducts.this, "Erro ao buscar resíduos", Toast.LENGTH_SHORT).show();
                                        methods.openScreenActivity(MyProducts.this, GenericError.class);
                                    }
                                }
                                @Override
                                public void onFailure(Call<List<Residue>> call, Throwable t) {
                                    methods.openScreenActivity(MyProducts.this, GenericError.class);
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            methods.openScreenActivity(MyProducts.this, GenericError.class);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numeroDeColunas = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(this, numeroDeColunas);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new MyResiduesAdapter(new ArrayList<Residue>()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_my_prroducts);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}