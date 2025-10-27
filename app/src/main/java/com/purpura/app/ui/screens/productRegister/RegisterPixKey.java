package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.purpura.app.configuration.Methods;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;

public class RegisterPixKey extends AppCompatActivity {

    Methods methods = new Methods();
    MongoService mongoService = new MongoService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_pix_key);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle received = getIntent().getExtras();
        Bundle sent = new Bundle();

        ImageView backButton = findViewById(R.id.registerAdressBackButton);
        Button continueButton = findViewById(R.id.registerPixKeyAddPixKeyButton);
        EditText pixKeyInput = findViewById(R.id.registerPixKeyInput);
        EditText pixKeyNameInput = findViewById(R.id.registerPixKeyNameInput);

        backButton.setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> {
            if(pixKeyInput != null || pixKeyNameInput != null){
                PixKey pixKey = new PixKey(
                        pixKeyNameInput.getText().toString(),
                        pixKeyInput.getText().toString()
                );

                Residue residue = (Residue) received.getSerializable("residue");
                Address address = (Address) received.getSerializable("address");
                sent.putSerializable("residue", residue);
                sent.putSerializable("address", address);
                sent.putSerializable("pixKey", pixKey);
                methods.openScreenActivityWithBundle(this, RegisterProductEndPage.class, sent);
            }
        });

    }
}