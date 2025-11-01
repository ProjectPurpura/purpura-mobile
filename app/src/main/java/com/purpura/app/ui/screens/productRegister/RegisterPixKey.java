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
import com.purpura.app.model.mongo.PixKey;

public class RegisterPixKey extends AppCompatActivity {

    private ImageView back;
    private EditText name;
    private EditText key;
    private Button next;

    private Bundle carry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_pix_key);

        back = findViewById(R.id.registerAdressBackButton);
        name = findViewById(R.id.registerPixKeyNameInput);
        key  = findViewById(R.id.registerPixKeyInput);
        next = findViewById(R.id.registerPixKeyAddPixKeyButton);

        carry = getIntent() != null && getIntent().getExtras() != null ? new Bundle(getIntent().getExtras()) : new Bundle();

        back.setOnClickListener(v -> finish());

        next.setOnClickListener(v -> {
            String n = s(name);
            String k = s(key);

            if (TextUtils.isEmpty(n)) { toast("Informe o nome da chave"); return; }
            if (TextUtils.isEmpty(k)) { toast("Informe a chave Pix"); return; }

            PixKey pixKey = new PixKey(n, k, null);
            carry.putSerializable("pixKey", pixKey);

            Intent i = new Intent(this, RegisterProductEndPage.class);
            i.putExtras(carry);
            startActivity(i);
        });
    }

    private static String s(EditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}