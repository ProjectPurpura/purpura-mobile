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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

public class RegisterAdress extends AppCompatActivity {

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

        ImageView backButton = findViewById(R.id.registerAdressBackButton);
        Button continueButton = findViewById(R.id.registerAdressValidateZipCode);
        EditText name = findViewById(R.id.registerAdressName);
        EditText zipCode = findViewById(R.id.registerAdressZipCode);
        EditText number = findViewById(R.id.registerAdressNumber);
        EditText complement = findViewById(R.id.registerAdressComplement);
        Address address = new Address(null ,name.getText().toString(), zipCode.getText().toString(), complement.getText().toString(), Integer.parseInt(number.getText().toString()));

        backButton.setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> {
            try{
                FirebaseFirestore.getInstance()
                        .collection("empresa")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                mongoService.createAdress(cnpj, address, this);
                                methods.openScreenActivity(this, RegisterPixKey.class);
                            }
                        }).addOnFailureListener(view ->
                                methods.openScreenActivity(RegisterAdress.this, GenericError.class)
                        );

            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}