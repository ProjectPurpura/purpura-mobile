package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterAdress extends AppCompatActivity {

    Methods methods = new Methods();
    MongoService mongoService = new MongoService();
    private Residue residue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_adress);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle env = getIntent().getExtras();
        Serializable serResidue = env.getSerializable("residue");
        residue = (Residue) serResidue;

        EditText name = findViewById(R.id.registerAdressName);
        EditText zipCode = findViewById(R.id.registerAdressZipCode);
        EditText number = findViewById(R.id.registerAdressNumber);
        EditText complement = findViewById(R.id.registerAdressComplement);
        Button continueButton = findViewById(R.id.registerAdressValidateZipCode);

        continueButton.setOnClickListener(v -> {
            String nomeValue = name.getText().toString().trim();
            String cepValue = zipCode.getText().toString().trim();
            String complementValue = complement.getText().toString().trim();
            String numberValue = number.getText().toString().trim();

            if (nomeValue.isEmpty() || cepValue.isEmpty() || numberValue.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            int numberInt;
            try {
                numberInt = Integer.parseInt(numberValue);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Número do endereço inválido!", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = new Address(
                    null,
                    nomeValue,
                    cepValue,
                    complementValue,
                    numberInt
            );

            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            mongoService.createAdress(cnpj, address, this).enqueue(new Callback<Address>() {
                                @Override
                                public void onResponse(Call<Address> call, Response<Address> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        residue.setIdEndereco(response.body().getId());
                                        methods.openScreenActivityWithBundle(RegisterAdress.this, RegisterPixKey.class, getIntent().getExtras());
                                    } else {
                                        Toast.makeText(RegisterAdress.this, response.message(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Address> call, Throwable t) {
                                    Toast.makeText(RegisterAdress.this, "Falha endereço.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(e ->
                            methods.openScreenActivity(RegisterAdress.this, GenericError.class)
                    );
        });
    }
}