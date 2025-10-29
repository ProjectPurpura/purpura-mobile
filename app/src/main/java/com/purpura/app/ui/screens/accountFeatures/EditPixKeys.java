package com.purpura.app.ui.screens.accountFeatures;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.adapters.mongo.PixKeyAdapter;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPixKeys extends AppCompatActivity {

    Methods methods = new Methods();
    MongoService mongoService = new MongoService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_pix_keys);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.editPixKeyBackButton);
        backButton.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.editPixKeysRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            Call<List<PixKey>> call = mongoService.getAllPixKeys(cnpj);
                            call.enqueue(new Callback<List<PixKey>>() {
                                @Override
                                public void onResponse(Call<List<PixKey>> call, Response<List<PixKey>> response) {
                                    if (response.isSuccessful()) {
                                        List<PixKey> pixKeys = response.body();
                                        PixKeyAdapter adapter = new PixKeyAdapter(pixKeys, EditPixKeys.this);
                                        recyclerView.setAdapter(adapter);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<PixKey>> call, Throwable t) {
                                    methods.openScreenActivity(EditPixKeys.this, GenericError.class);
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            methods.openScreenActivity(EditPixKeys.this, GenericError.class);
            throw new RuntimeException(e);
        }
    }
}
