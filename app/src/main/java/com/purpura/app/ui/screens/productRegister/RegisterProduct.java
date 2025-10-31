package com.purpura.app.ui.screens.productRegister;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapDecoder;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Limit;
import com.cloudinary.android.preprocess.Rotate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.EnvironmentVariables;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Residue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterProduct extends AppCompatActivity {

    private Methods methods = new Methods();
    private static boolean cloudinaryInitialized = false;

    private ActivityResultLauncher<String[]> requestPermissions;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private Uri photoUri;
    private String imageUrl = "";
    private String uploadPreset = "Purpura";
    private String cnpj;

    private EditText name;
    private EditText description;
    private EditText quantity;
    private EditText price;
    private EditText weight;
    private EditText weightType;
    private ImageView imageView;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_product);

        name        = findViewById(R.id.registerProductName);
        description = findViewById(R.id.registerProductDescription);
        quantity    = findViewById(R.id.registerProductQuantity);
        price       = findViewById(R.id.registerProductPrice);
        weight      = findViewById(R.id.registerProductWeight);
        weightType  = findViewById(R.id.registerProductWeightType);
        imageView   = findViewById(R.id.registerProductImage);
        continueButton = findViewById(R.id.registerProductAddProductButton);

        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        cnpj = document.getString("cnpj");
                    }
                });

        imageView.setOnClickListener(v -> {
            try {
                captureImage(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        continueButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(imageUrl)) {
                Toast.makeText(this, "Tire a foto do produto antes de continuar.", Toast.LENGTH_SHORT).show();
                return;
            }

            String n  = tx(name);
            String d  = tx(description);
            String qS = tx(quantity);
            String pS = tx(price);
            String wS = tx(weight);
            String u  = tx(weightType);

            if (TextUtils.isEmpty(n))  { toast("Informe o nome"); return; }
            if (TextUtils.isEmpty(d))  { toast("Informe a descrição"); return; }
            if (TextUtils.isEmpty(pS)) { toast("Informe o preço"); return; }
            if (TextUtils.isEmpty(wS)) { toast("Informe o peso"); return; }
            if (TextUtils.isEmpty(u))  { toast("Informe a unidade"); return; }
            if (TextUtils.isEmpty(qS)) { toast("Informe o estoque"); return; }
            if (TextUtils.isEmpty(cnpj)) { toast("Aguarde o carregamento do CNPJ"); return; }

            double preco  = parseDouble(pS, 0d);
            double pesoV  = parseDouble(wS, 0d);
            int est       = parseInt(qS, 0);
            String unidade = u.toUpperCase();

            Residue residue = new Residue(
                    null,
                    n,
                    d,
                    pesoV,
                    preco,
                    est,
                    unidade,
                    imageUrl,
                    null,
                    null,
                    null
            );

            Bundle env = new Bundle();
            env.putSerializable("residue", residue);
            env.putString("cnpj", cnpj);
            methods.openScreenActivityWithBundle(this, RegisterAdress.class, env);
        });

        checkPermissions();
        initCloudinary();
        setCamera();
    }

    private void initCloudinary() {
        if (!cloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", EnvironmentVariables.CLOUD_NAME);
            MediaManager.init(this, config);
            cloudinaryInitialized = true;
        }
    }

    private void checkPermissions() {
        requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.CAMERA
            });
        } else {
            requestPermissions.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            });
        }
    }

    public void captureImage(View v) throws IOException {
        String time = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String nm = "Purpura_Products_" + time;
        File pasta = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (pasta == null) throw new IOException("Diretório de imagens externo indisponível");
        File photo = File.createTempFile(nm, ".jpg", pasta);
        photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photo);
        cameraLauncher.launch(photoUri);
    }

    private void setCamera() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean success) {
                        if (Boolean.TRUE.equals(success)) {
                            if (photoUri != null) {
                                preUpload(photoUri);
                            } else {
                                Toast.makeText(RegisterProduct.this, "URI da foto não está disponível.", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "photoUri null após TakePicture");
                            }
                        } else {
                            Toast.makeText(RegisterProduct.this, "Foto não foi tirada", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void preUpload(Uri imageUri) {
        if (imageUri == null) return;
        MediaManager.get().upload(imageUri)
                .option("folder", "Purpura")
                .unsigned(uploadPreset)
                .preprocess(new ImagePreprocessChain()
                        .loadWith(new BitmapDecoder(1000, 1000))
                        .addStep(new Limit(1000, 1000))
                        .addStep(new DimensionsValidator(10, 10, 1000, 1000))
                        .addStep(new Rotate(90))
                        .saveWith(new BitmapEncoder(BitmapEncoder.Format.JPEG, 60))
                )
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        imageUrl = url;
                        runOnUiThread(() -> {
                            imageView.setTag(url);
                            Glide.with(RegisterProduct.this).load(url).into(imageView);
                            Toast.makeText(RegisterProduct.this, "Imagem enviada", Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() ->
                                Toast.makeText(RegisterProduct.this, "Erro no upload: " + (error != null ? error.getDescription() : "desconhecido"), Toast.LENGTH_SHORT).show()
                        );
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch(RegisterProduct.this);
    }

    private static String tx(EditText e) { return e.getText() == null ? "" : e.getText().toString().trim(); }
    private static int parseInt(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }
    private static double parseDouble(String s, double def) { try { return Double.parseDouble(s.replace(",", ".")); } catch (Exception e) { return def; } }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}