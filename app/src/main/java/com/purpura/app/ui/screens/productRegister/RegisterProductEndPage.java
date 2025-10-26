package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Adress;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.api.RegisterProductInterfaces;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.accountFeatures.MyProducts;
import com.purpura.app.ui.screens.errors.GenericError;

public class RegisterProductEndPage extends AppCompatActivity {

    Methods methods = new Methods();
    MongoService mongoService = new MongoService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_product_end_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button continueButton = findViewById(R.id.registerProductEnd);
        ImageView backButton = findViewById(R.id.registerAdressBackButton);

        Bundle env = getIntent().getExtras();
        if (env == null) {
            methods.openScreenActivity(this, GenericError.class);
            return;
        }

        Residue residue = (Residue) env.getSerializable("residue");
        Adress adress = (Adress) env.getSerializable("address");
        PixKey pixKey = (PixKey) env.getSerializable("pixKey");

        if (residue == null || adress == null || pixKey == null) {
            methods.openScreenActivity(this, GenericError.class);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String cnpj = document.getString("cnpj");
                        if (cnpj == null) {
                            methods.openScreenActivity(this, GenericError.class);
                            return;
                        }
                        registerAdress(cnpj, adress, new RegisterProductInterfaces.AdressCallback() {
                            @Override
                            public void onSuccess(String adressId) {
                                registerPixKey(cnpj, pixKey, new RegisterProductInterfaces.PixKeyCallback() {
                                    @Override
                                    public void onSuccess(String pixKeyId) {
                                        residue.setIdChavePix(pixKeyId);
                                        residue.setIdEndereco(adressId);
                                        registerResidue(cnpj, residue, new RegisterProductInterfaces.ResidueCallback() {
                                            @Override
                                            public void onSuccess(String residueId) {
                                                Toast.makeText(RegisterProductEndPage.this, "Produto registrado com sucesso!", Toast.LENGTH_SHORT).show();
                                                methods.openScreenActivity(RegisterProductEndPage.this, MyProducts.class);
                                            }
                                            @Override
                                            public void onError(Throwable t) {
                                                Toast.makeText(RegisterProductEndPage.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(RegisterProductEndPage.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            @Override
                            public void onError(Throwable t) {
                                Toast.makeText(RegisterProductEndPage.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        methods.openScreenActivity(this, GenericError.class);
                    }
                })
                .addOnFailureListener(e -> methods.openScreenActivity(this, GenericError.class));

        backButton.setOnClickListener(v -> finish());
    }

    public void registerAdress(String cnpj, Adress adress, RegisterProductInterfaces.AdressCallback callback) {
        mongoService.createAdress(cnpj, adress, callback);
    }

    public void registerPixKey(String cnpj, PixKey pixKey, RegisterProductInterfaces.PixKeyCallback callback) {
        mongoService.createPixKey(cnpj, pixKey, callback);
    }

    public void registerResidue(String cnpj, Residue residue, RegisterProductInterfaces.ResidueCallback callback) {
        mongoService.createResidue(cnpj, residue, callback);
    }
}
