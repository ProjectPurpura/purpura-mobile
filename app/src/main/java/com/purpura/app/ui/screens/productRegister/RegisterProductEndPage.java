package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.widget.Button;
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
import com.purpura.app.model.mongo.PixKey;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

public class RegisterProductEndPage extends AppCompatActivity {

    Methods methods = new Methods();
    Bundle bundle = getIntent().getExtras();
    MongoService mongoService = new MongoService();
    String pixKeyName = bundle.getString("pixKeyName");
    String pixKeyPixKey = bundle.getString("pixKey");
    PixKey pixKey = new PixKey(pixKeyName, pixKeyPixKey, null);

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
                                mongoService.createPixKey(cnpj, pixKey, this);
                                methods.openScreenActivity(this, RegisterProductEndPage.class);
                            }
                        }).addOnFailureListener(view ->
                                methods.openScreenActivity(this, GenericError.class)
                        );

            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}