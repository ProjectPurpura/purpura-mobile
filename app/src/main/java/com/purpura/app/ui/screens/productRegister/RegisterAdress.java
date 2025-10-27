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

import com.purpura.app.R;
import com.purpura.app.configuration.ZipCodeMask;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Adress;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;

import java.io.Serializable;

public class RegisterAdress extends AppCompatActivity {
    Adress address = null;
    Methods methods = new Methods();
    MongoService mongoService = new MongoService();

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

        Bundle received = getIntent().getExtras();
        Bundle sent = new Bundle();

        ImageView backButton = findViewById(R.id.registerAdressBackButton);
        Button continueButton = findViewById(R.id.registerAdressValidateZipCode);
        EditText name = findViewById(R.id.registerAdressName);
        EditText zipCode = findViewById(R.id.registerAdressZipCode);
        EditText number = findViewById(R.id.registerAdressNumber);
        EditText complement = findViewById(R.id.registerAdressComplement);

        zipCode.addTextChangedListener(new ZipCodeMask(zipCode));

        continueButton.setOnClickListener(v -> {
            if(name != null || zipCode != null || number != null || complement != null){
                address = new Adress(
                        name.getText().toString(),
                        zipCode.getText().toString(),
                        complement.getText().toString(),
                        Integer.parseInt(number.getText().toString())
                );

                Residue residue = (Residue) received.getSerializable("residue");
                sent.putSerializable("residue", residue);

                System.out.println(residue);

                sent.putSerializable("address", (Serializable) address);

                System.out.println(address);
                methods.openScreenActivityWithBundle(this, RegisterPixKey.class, sent);
            }else{
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }
}