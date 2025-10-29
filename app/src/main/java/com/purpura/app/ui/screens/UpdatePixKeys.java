package com.purpura.app.ui.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.accountFeatures.EditAddress;
import com.purpura.app.ui.screens.accountFeatures.EditPixKeys;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePixKeys extends AppCompatActivity {

    private final MongoService mongoService = new MongoService();

    private ImageView back;
    private EditText inputName;
    private EditText inputKey;
    private Button update;

    private String cnpj;
    private String pixKeyId;

    private Call<PixKey> fetchCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_pix_keys);

        back = findViewById(R.id.updateAdressBackButton);
        inputName = findViewById(R.id.updatePixKeyNameInput);
        inputKey = findViewById(R.id.updatePixKeyInput);
        update = findViewById(R.id.updatePixKeyAddPixKeyButton);

        String extraCnpj = getIntent() != null ? getIntent().getStringExtra("cnpj") : "";
        String extraId = getIntent() != null ? getIntent().getStringExtra("pixKeyId") : "";
        cnpj = sanitize(extraCnpj);
        pixKeyId = n(extraId);

        back.setOnClickListener(v -> finish());
        update.setOnClickListener(v -> doUpdate());

        if (!TextUtils.isEmpty(cnpj) && !TextUtils.isEmpty(pixKeyId)) {
            fetchPixKey(cnpj, pixKeyId);
        } else if (!TextUtils.isEmpty(pixKeyId)) {
            fetchCnpjThenLoad(pixKeyId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchCall != null) fetchCall.cancel();
    }

    private void fetchPixKey(String c, String id) {
        fetchCall = mongoService.getPixKeyById(c, id);
        fetchCall.enqueue(new Callback<PixKey>() {
            @Override public void onResponse(Call<PixKey> call, Response<PixKey> r) {
                if (r.isSuccessful() && r.body() != null) {
                    PixKey p = r.body();
                    inputName.setText(n(p.getName()));
                    inputKey.setText(n(p.getKey()));
                }
            }
            @Override public void onFailure(Call<PixKey> call, Throwable t) { }
        });
    }

    private void fetchCnpjThenLoad(String id) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String c = sanitize(doc.getString("cnpj"));
                    if (!TextUtils.isEmpty(c)) {
                        cnpj = c;
                        fetchPixKey(cnpj, id);
                    }
                });
    }

    private void doUpdate() {
        String c = sanitize(n(cnpj));
        if (TextUtils.isEmpty(c)) { Toast.makeText(this, "CNPJ não identificado.", Toast.LENGTH_SHORT).show(); return; }
        String id = n(pixKeyId);
        if (TextUtils.isEmpty(id)) { Toast.makeText(this, "ID da chave não identificado.", Toast.LENGTH_SHORT).show(); return; }

        String nome = n(inputName.getText() == null ? "" : inputName.getText().toString());
        String chave = n(inputKey.getText() == null ? "" : inputKey.getText().toString());
        if (TextUtils.isEmpty(nome)) { Toast.makeText(this, "Informe o nome da chave.", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(chave)) { Toast.makeText(this, "Informe a chave.", Toast.LENGTH_SHORT).show(); return; }

        PixKey body = new PixKey(nome, chave, id);
        mongoService.updatePixKey(c, id, body, this);
        Toast.makeText(this, "Atualizando chave...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, EditPixKeys.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private static String n(String s) { return s == null ? "" : s.trim(); }
    private static String sanitize(String s) { return s == null ? "" : s.replaceAll("\\D+", ""); }
}
