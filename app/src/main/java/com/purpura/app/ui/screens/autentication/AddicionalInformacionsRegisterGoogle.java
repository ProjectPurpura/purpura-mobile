package com.purpura.app.ui.screens.autentication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.MainActivity;
import com.purpura.app.ui.screens.errors.GenericError;

public class AddicionalInformacionsRegisterGoogle extends AppCompatActivity {

    Methods methods = new Methods();
    MongoService mongoService = new MongoService();
    Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addicional_informacions_register_google);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AddicionalInformacionsRegisterGoogle.this,
                        "Complete seu cadastro para continuar", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button finalizar = findViewById(R.id.addicionalInformationsEndRegister);
        finalizar.setOnClickListener(v -> salvarInformacoes());
    }

    private void salvarInformacoes() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = user.getDisplayName();
        String email = user.getEmail();
        String foto = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

        String telefone = ((EditText) findViewById(R.id.addicionalInformationsPhoneNumber))
                .getText().toString().trim();
        String cnpj = ((EditText) findViewById(R.id.AddictionalInformationsCnpj))
                .getText().toString().trim();

        if (telefone.isEmpty() || cnpj.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cnpj.length() != 14 || !cnpj.matches("\\d+")) {
            Toast.makeText(this, "CNPJ inválido. Deve ter 14 dígitos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (telefone.length() < 10 || !telefone.matches("\\d+")) {
            Toast.makeText(this, "Telefone inválido. Deve ter no mínimo 10 dígitos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Company empresa = new Company( cnpj, telefone, email, nome, foto);

        Toast.makeText(this, "Salvando no Firestore...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(user.getUid())
                .set(empresa)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cadastro finalizado!", Toast.LENGTH_SHORT).show();
                    methods.openScreenActivity(this, MainActivity.class);
                    finish();
                })
                .addOnFailureListener(e -> {
                    methods.openScreenActivity(activity, GenericError.class);
                    Toast.makeText(this,
                            "Erro ao salvar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
        mongoService.createCompany(empresa, this);
    }
}