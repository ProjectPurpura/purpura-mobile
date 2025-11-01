package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.purpura.app.R;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.remote.service.MicroService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterAdress extends AppCompatActivity {

    private ImageView back;
    private EditText nome;
    private EditText cep;
    private EditText numero;
    private EditText complemento;
    private Button next;
    private Bundle carry;
    private MicroService microService;
    private Call<Boolean> cep_valid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_adress);

        back = findViewById(R.id.registerAdressBackButton);
        nome = findViewById(R.id.registerAdressName);
        cep = findViewById(R.id.registerAdressZipCode);
        numero = findViewById(R.id.registerAdressNumber);
        complemento = findViewById(R.id.registerAdressComplement);
        next = findViewById(R.id.registerAdressValidateZipCode);

        microService = new MicroService();

        carry = getIntent() != null && getIntent().getExtras() != null
                ? new Bundle(getIntent().getExtras())
                : new Bundle();

        back.setOnClickListener(v -> finish());

        next.setOnClickListener(v -> {
            String n = x(nome);
            String c = x(cep);
            String nu = x(numero);
            String co = x(complemento);

            if (TextUtils.isEmpty(n)) { toast("Informe o nome do endereço"); return; }
            if (TextUtils.isEmpty(c)) { toast("Informe o CEP"); return; }
            if (TextUtils.isEmpty(nu)) { toast("Informe o número"); return; }

            String cDigits = c.replaceAll("\\D", "");
            if (cDigits.length() != 8) { toast("CEP inválido"); return; }

            String cFormatted = cDigits.substring(0, 5) + "-" + cDigits.substring(5);

            cep_valid = microService.callCepIsValid(cFormatted);
            cep_valid.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                        int number = 0;
                        try { number = Integer.parseInt(nu.trim()); } catch (Exception ignored) {}
                        Address address = new Address(null, n, cFormatted, co, number);
                        carry.putSerializable("address", address);
                        Intent i = new Intent(RegisterAdress.this, RegisterPixKey.class);
                        i.putExtras(carry);
                        startActivity(i);
                    } else {
                        Toast.makeText(RegisterAdress.this, "CEP inválido", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(RegisterAdress.this, "Falha ao validar CEP", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cep_valid != null) cep_valid.cancel();
    }

    private static String x(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
