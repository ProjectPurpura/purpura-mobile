package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProduct extends AppCompatActivity {

    MongoService service = new MongoService();
    Methods methods = new Methods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView name = findViewById(R.id.updateProductName);
        TextView description = findViewById(R.id.updateProductDescription);
        TextView price = findViewById(R.id.updateProductPrice);
        TextView weight = findViewById(R.id.updateProductWeight);
        TextView weightUnity = findViewById(R.id.updateProductWeightType);
        TextView quantity = findViewById(R.id.updateProductQuantity);
        ImageView image = findViewById(R.id.updateProductImage);
        ImageView backButton = findViewById(R.id.updateProductBackButton);

        backButton.setOnClickListener(v -> finish());

        try{
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            service.getResidueById(cnpj, getIntent().getStringExtra("residueId")).enqueue(new Callback<Residue>() {
                                @Override
                                public void onResponse(Call<Residue> call, Response<Residue> response) {
                                    Residue residue = response.body();

                                    name.setHint(residue.getNome());
                                    description.setHint(residue.getDescricao());
                                    price.setHint(String.valueOf(residue.getPreco()));
                                    weight.setHint(String.valueOf(residue.getPeso()));
                                    weightUnity.setHint(residue.getTipoUnidade());
                                    quantity.setHint(String.valueOf(residue.getEstoque()));

                                    Glide.with(UpdateProduct.this)
                                            .load(residue.getUrlFoto())
                                            .into(image);
                                }

                                @Override
                                public void onFailure(Call<Residue> call, Throwable t) {

                                }
                            });
                        }
                    }).addOnFailureListener(view -> finish());
        }catch(Exception e){
            methods.openScreenActivity(this, GenericError.class);
        }
    }
}