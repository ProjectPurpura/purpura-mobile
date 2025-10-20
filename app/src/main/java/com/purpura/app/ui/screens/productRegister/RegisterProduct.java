package com.purpura.app.ui.screens.productRegister;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.cloudnary.Cloudinary;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

import java.util.Map;

public class RegisterProduct extends AppCompatActivity {

    Methods methods = new Methods();
    Cloudinary cloudinary = new Cloudinary();
    MongoService mongoService = new MongoService();
    private ActivityResultLauncher<Intent> requestGallery;
    private ActivityResultLauncher<String[]> requestPermission;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.registerProductBackButton);
        Button continueButton = findViewById(R.id.registerProductAddProductButton);

        EditText nome = findViewById(R.id.registerProductName);
        EditText descricao = findViewById(R.id.registerProductDescription);
        EditText preco = findViewById(R.id.registerProductPrice);
        EditText peso = findViewById(R.id.registerProductWeight);
        EditText unidadeMedida = findViewById(R.id.registerProductWeightType);
        EditText quantidade = findViewById(R.id.registerProductQuantity);
        ImageView urlFoto = findViewById(R.id.registerProductImage);

        cloudinary.initCloudinary(this);

        requestGallery =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() != null) {
                        imageUri = result.getData().getData();

                        cloudinary.uploadImage(this, imageUri, new Cloudinary.ImageUploadCallback() {
                            @Override
                            public void onUploadSuccess(String imageUrl) {
                                Glide.with(RegisterProduct.this)
                                        .load(imageUrl)
                                        .into(urlFoto);
                                urlFoto.setTag(imageUrl);
                            }

                            @Override
                            public void onUploadFailure(String error) {
                                methods.openScreenActivity(RegisterProduct.this, GenericError.class);
                            }
                        });
                    }
                });

        requestPermission =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        String permission = entry.getKey();
                        Boolean isGranted = entry.getValue();
                        if (isGranted) {
                            Log.d("Permissions", "Permission granted: " + permission);
                        } else {
                            methods.openScreenActivity(RegisterProduct.this, GenericError.class);
                        }
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.CAMERA
            });
        } else {
            requestPermission.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            });
        }

        urlFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            requestGallery.launch(intent);
        });

        continueButton.setOnClickListener(v -> {
            String nomeTxt = nome.getText().toString().trim();
            String descricaoTxt = descricao.getText().toString().trim();
            String precoTxt = preco.getText().toString().trim();
            String pesoTxt = peso.getText().toString().trim();
            String unidadeTxt = unidadeMedida.getText().toString().trim();
            String quantidadeTxt = quantidade.getText().toString().trim();
            String urlFotoTxt = urlFoto.getTag().toString();

            if (nomeTxt.isEmpty() || descricaoTxt.isEmpty() || precoTxt.isEmpty() || pesoTxt.isEmpty()
                    || unidadeTxt.isEmpty() || quantidadeTxt.isEmpty() || urlFotoTxt.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            double precoVal = Double.parseDouble(precoTxt);
            double pesoVal = Double.parseDouble(pesoTxt);
            int quantidadeVal = Integer.parseInt(quantidadeTxt);


            Residue residue = new Residue(
                    null,
                    nomeTxt,
                    descricaoTxt,
                    pesoVal,
                    precoVal,
                    quantidadeVal,
                    unidadeTxt,
                    urlFotoTxt,
                    null,
                    null,
                    null
            );

            try {
                FirebaseFirestore.getInstance()
                        .collection("empresa")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                mongoService.createResidue(cnpj, residue, this);
                                methods.openScreenActivity(this, RegisterAdress.class);
                            }
                        }).addOnFailureListener(view ->
                                methods.openScreenActivity(RegisterProduct.this, GenericError.class)
                        );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao cadastrar produto", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

}