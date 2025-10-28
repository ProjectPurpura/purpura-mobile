package com.purpura.app.ui.screens.productRegister;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.purpura.app.remote.service.MongoService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterProduct extends AppCompatActivity {

    MongoService service = new MongoService();
    Methods methods = new Methods();
    private static boolean cloudinaryInitialized = false;
    private ActivityResultLauncher<String[]> requestPermissions;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri photoUri;
    private String cloudname = EnvironmentVariables.CLOUD_NAME;
    private String uploadProjeto = "Purpura";
    private String cnpj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_product);

        Bundle env = new Bundle();

        TextView name = findViewById(R.id.registerProductName);
        TextView description = findViewById(R.id.registerProductDescription);
        TextView quantity = findViewById(R.id.registerProductQuantity);
        TextView price = findViewById(R.id.registerProductPrice);
        TextView weight = findViewById(R.id.registerProductWeight);
        TextView weightType = findViewById(R.id.registerProductWeightType);
        ImageView imageView = findViewById(R.id.registerProductImage);
        Button continueButton = findViewById(R.id.registerProductAddProductButton);


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
            if(photoUri != null) {


                Residue residue = new Residue(
                        null,
                        name.getText().toString(),
                        description.getText().toString(),
                        Double.parseDouble(price.getText().toString()),
                        Double.parseDouble(weight.getText().toString()),
                        Integer.parseInt(quantity.getText().toString()),
                        weightType.getText().toString(),
                        photoUri.toString(),
                        null,
                        null,
                        cnpj
                );
                env.putSerializable("residue", residue);
                methods.openScreenActivityWithBundle(this, RegisterAdress.class, env);
            }
        });

        checkPermissions();
        initCloudnary();
        setCamera();

    }

    private void initCloudnary() {
        if (!cloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", EnvironmentVariables.CLOUD_NAME);
            MediaManager.init(this, config);
            cloudinaryInitialized = true;
        }
    }
    private void checkPermissions() {
        requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                String permission = entry.getKey();
                Boolean isGranted = entry.getValue();
                if (isGranted) {
                    Log.d(TAG, "Permission granted: " + permission);
                } else {
                    Log.d(TAG, "Permission denied: " + permission);
                }
            }
        });

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
        String name = "Purpura_Products_" + time;
        File pasta = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (pasta == null) {
            throw new IOException("Diretório de imagens externo indisponível");
        }
        File photo = File.createTempFile(name, ".jpg", pasta);

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
                                Log.w(TAG, "photoUri é null após TakePicture");
                            }
                        } else {
                            Toast.makeText(RegisterProduct.this, "Foto não foi tirada", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "TakePicture retornou false");
                        }
                    }
                }
        );
    }

    private void preUpload(Uri imageUri) {
        if (imageUri == null) {
            Log.w(TAG, "preUpload chamado com uri nula");
            return;
        }
        MediaManager.get().upload(imageUri)
                .option("folder", "AulaFoto")
                .unsigned(uploadProjeto)
                .preprocess(new ImagePreprocessChain()
                        .loadWith(new BitmapDecoder(1000, 1000))
                        .addStep(new Limit(1000, 1000))
                        .addStep(new DimensionsValidator(10, 10, 1000, 1000))
                        .addStep(new Rotate(90))
                        .saveWith(new BitmapEncoder(BitmapEncoder.Format.JPEG, 60))
                )
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload success: " + requestId + " url=" + url);

                        runOnUiThread(() -> {
                            ImageView iv = findViewById(R.id.registerProductImage);
                            if (iv != null) {
                                Glide.with(RegisterProduct.this)
                                        .load(url)
                                        .into(iv);
                            }
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> Toast.makeText(RegisterProduct.this, "Erro no upload: " + (error != null ? error.getDescription() : "desconhecido"), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch(RegisterProduct.this);
    }
}