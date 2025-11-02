package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.accountFeatures.MyProducts;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterProductEndPage extends AppCompatActivity {

    private final MongoService mongoService = new MongoService();
    private final Methods methods = new Methods();
    private Residue residue;
    private Address address;
    private PixKey pixKey;
    private String cnpj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_product_end_page);

        ImageView back = findViewById(R.id.registerAdressBackButton);
        Button finish = findViewById(R.id.registerProductEnd);
        back.setOnClickListener(v -> finish());

        if (getIntent() != null) {
            Object r = getIntent().getSerializableExtra("residue");
            Object a = getIntent().getSerializableExtra("address");
            Object p = getIntent().getSerializableExtra("pixKey");
            String c = getIntent().getStringExtra("cnpj");
            if (r instanceof Residue) residue = (Residue) r;
            if (a instanceof Address) address = (Address) a;
            if (p instanceof PixKey) pixKey = (PixKey) p;
            if (c != null && !c.isEmpty()) cnpj = c;
        }

        if (cnpj == null || cnpj.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            cnpj = document.getString("cnpj");
                        }
                    });
        }

        finish.setOnClickListener(v -> {
            if (residue == null || address == null || pixKey == null) {
                Toast.makeText(this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cnpj == null || cnpj.isEmpty()) {
                Toast.makeText(this, "Aguarde o carregamento do CNPJ.", Toast.LENGTH_SHORT).show();
                return;
            }
            createAddress();
        });
    }

    private void createAddress() {
        mongoService.createAddressCall(cnpj, address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String addressId = response.body().getId();
                    createPixKey(addressId);
                } else {
                    Toast.makeText(RegisterProductEndPage.this, "Falha ao criar endereço.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Toast.makeText(RegisterProductEndPage.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPixKey(String addressId) {
        mongoService.createPixKeyCall(cnpj, pixKey).enqueue(new Callback<PixKey>() {
            @Override
            public void onResponse(Call<PixKey> call, Response<PixKey> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String pixKeyId = response.body().getId();
                    createResidue(addressId, pixKeyId);
                } else {
                    Toast.makeText(RegisterProductEndPage.this, "Falha ao criar chave Pix.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<PixKey> call, Throwable t) {
                Toast.makeText(RegisterProductEndPage.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createResidue(String addressId, String pixKeyId) {
        residue.setIdEndereco(addressId);
        residue.setIdChavePix(pixKeyId);
        residue.setCnpj(null);
        mongoService.createResidueCall(cnpj, residue).enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(Call<Residue> call, Response<Residue> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterProductEndPage.this, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    methods.openScreenActivity(RegisterProductEndPage.this, MyProducts.class);
                } else {
                    Toast.makeText(RegisterProductEndPage.this, "Falha ao cadastrar produto.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Residue> call, Throwable t) {
                Toast.makeText(RegisterProductEndPage.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}