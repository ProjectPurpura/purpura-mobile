package com.purpura.app.ui.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.accountFeatures.EditAddress;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateAddress extends AppCompatActivity {

    private final MongoService mongoService = new MongoService();

    private ImageView back;
    private TextView header;
    private EditText name;
    private EditText cep;
    private EditText number;
    private EditText complement;
    private Button update;

    private String cnpj;
    private String addressId;
    private Address address;

    private Call<Address> fetchCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_adress);

        back = findViewById(R.id.registerAdressBackButton);
        name = findViewById(R.id.registerAdressName);
        cep = findViewById(R.id.registerAdressZipCode);
        number = findViewById(R.id.registerAdressNumber);
        complement = findViewById(R.id.registerAdressComplement);
        update = findViewById(R.id.registerAdressValidateZipCode);

        Serializable ser = getIntent() != null ? getIntent().getSerializableExtra("address") : null;
        String extraCnpj = getIntent() != null ? n(getIntent().getStringExtra("cnpj")) : "";
        String extraId = getIntent() != null ? n(getIntent().getStringExtra("addressId")) : "";
        cnpj = sanitize(extraCnpj);
        addressId = n(extraId);

        back.setOnClickListener(v -> finish());
        update.setOnClickListener(v -> doUpdate());

        if (ser instanceof Address) {
            address = (Address) ser;
            if (!TextUtils.isEmpty(address.getId())) addressId = address.getId();
            fill(address);
            return;
        }

        if (!TextUtils.isEmpty(cnpj) && !TextUtils.isEmpty(addressId)) {
            fetchAddress(cnpj, addressId);
        } else if (!TextUtils.isEmpty(addressId)) {
            fetchCnpjThenLoad(addressId);
        } else {
            Toast.makeText(this, "Dados insuficientes.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchCall != null) fetchCall.cancel();
    }

    private void fetchAddress(String c, String id) {
        fetchCall = mongoService.getAdressById(c, id);
        fetchCall.enqueue(new Callback<Address>() {
            @Override public void onResponse(Call<Address> call, Response<Address> r) {
                if (r.isSuccessful() && r.body() != null) {
                    address = r.body();
                    addressId = n(address.getId());
                    fill(address);
                } else {
                    Toast.makeText(UpdateAddress.this, "Endereço não encontrado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onFailure(Call<Address> call, Throwable t) {
                Toast.makeText(UpdateAddress.this, "Falha ao carregar.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchCnpjThenLoad(String id) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sem sessão.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String c = sanitize(doc.getString("cnpj"));
                    if (!TextUtils.isEmpty(c)) {
                        cnpj = c;
                        fetchAddress(cnpj, id);
                    } else {
                        Toast.makeText(this, "CNPJ não encontrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter CNPJ.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fill(Address a) {
        name.setText(n(a.getNome()));
        cep.setText(n(a.getCep()));
        number.setText(String.valueOf(a.getNumber()));
        complement.setText(n(a.getComplement()));
    }

    private void doUpdate() {
        String c = sanitize(n(cnpj));
        if (TextUtils.isEmpty(c)) { Toast.makeText(this, "CNPJ não identificado.", Toast.LENGTH_SHORT).show(); return; }
        String id = n(addressId);
        if (TextUtils.isEmpty(id)) { Toast.makeText(this, "ID do endereço não identificado.", Toast.LENGTH_SHORT).show(); return; }

        String nm = n(name.getText() != null ? name.getText().toString() : "");
        String cp = sanitize(n(cep.getText() != null ? cep.getText().toString() : ""));
        int num = parseIntSafe(n(number.getText() != null ? number.getText().toString() : ""), 0);
        String comp = n(complement.getText() != null ? complement.getText().toString() : "");

        Address body = new Address(id, nm, cp, comp, num);
        mongoService.updateAdress(cnpj, addressId, body, this);
        Toast.makeText(this, "Atualizando endereço...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, EditAddress.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    private static String n(String s) { return s == null ? "" : s.trim(); }
    private static String sanitize(String s) { return s == null ? "" : s.replaceAll("\\D+", ""); }
    private static int parseIntSafe(String s, int def) { try { return Integer.parseInt(TextUtils.isEmpty(s) ? String.valueOf(def) : s.trim()); } catch (Exception e) { return def; } }
}
