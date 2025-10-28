package com.purpura.app.ui.screens.productRegister;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.purpura.app.ui.screens.errors.GenericError;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProduct extends AppCompatActivity {
    private static boolean cloudinaryInitialized = false;
    private ActivityResultLauncher<String[]> requestPermissions;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri photoUri;
    private String uploadedImageUrl;
    private String uploadProjeto = "Purpura";
    private ImageView productImageView;
    MongoService service = new MongoService();
    Methods methods = new Methods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle env = getIntent().getExtras();
        String adressId = env.getString("adressId");
        String cnpj =  env.getString("cnpj");
        String productId = env.getString("residueId");
        String pixKeyId = env.getString("pixKeyId");

        initCloudnary();
        checkPermissions();
        setCamera();

        TextView name = findViewById(R.id.updateProductName);
        TextView description = findViewById(R.id.updateProductDescription);
        TextView price = findViewById(R.id.updateProductPrice);
        TextView weight = findViewById(R.id.updateProductWeight);
        TextView weightUnity = findViewById(R.id.updateProductWeightType);
        TextView quantity = findViewById(R.id.updateProductQuantity);
        ImageView image = findViewById(R.id.updateProductImage);
        ImageView backButton = findViewById(R.id.updateProductBackButton);

        image.setOnClickListener(v -> {
            try {
                captureImage(v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ((Button) findViewById(R.id.updateProductAddProductButton)).setOnClickListener(v -> {
            try{
                methods.openConfirmationPopUp(
                        this,
                        () -> {
                            Residue residue = new Residue(
                                    env.get("residueId").toString(),
                                    name.getText().toString(),
                                    description.getText().toString(),
                                    Double.parseDouble(price.getText().toString()),
                                    Double.parseDouble(weight.getText().toString()),
                                    Integer.parseInt(quantity.getText().toString()),
                                    weightUnity.getText().toString(),
                                    "",
                                    pixKeyId,
                                    adressId,
                                    cnpj
                            );
                            if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                                residue.setUrlFoto(uploadedImageUrl);
                            }

                            try{
                                service.updateResidue(cnpj, productId, residue, this);
                            }catch(Exception e){
                                methods.openScreenActivity(this, GenericError.class);
                            }
                            },
                        null
                );
            } catch (Exception e) {
                methods.openScreenActivity(this, GenericError.class);
            }
        });

        backButton.setOnClickListener(v -> finish());

        service.getResidueById(cnpj, getIntent().getStringExtra("residueId")).enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(Call<Residue> call, Response<Residue> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Residue residue = response.body();

                    name.setText(residue.getNome());
                    description.setText(residue.getDescricao());
                    price.setText(String.valueOf(residue.getPreco()));
                    weight.setText(String.valueOf(residue.getPeso()));
                    weightUnity.setText(residue.getTipoUnidade());
                    quantity.setText(String.valueOf(residue.getEstoque()));

                    Glide.with(UpdateProduct.this)
                            .load(residue.getUrlFoto())
                            .into(image);
                } else {
                    Toast.makeText(UpdateProduct.this, "Erro ao carregar produto: " + response.message(), Toast.LENGTH_SHORT).show();
                    methods.openScreenActivity(UpdateProduct.this, GenericError.class);
                }
            }

            @Override
            public void onFailure(Call<Residue> call, Throwable t) {
            }
        });
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
        requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});
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
        if (pasta == null) throw new IOException("Diretório de imagens externo indisponível");
        File photo = File.createTempFile(name, ".jpg", pasta);
        photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photo);
        cameraLauncher.launch(photoUri);
    }

    private void setCamera() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (Boolean.TRUE.equals(success)) {
                        if (photoUri != null) {
                            preUpload(photoUri);
                            reloadImage(photoUri);
                        } else {
                            Toast.makeText(UpdateProduct.this, "URI da foto não está disponível.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UpdateProduct.this, "Foto não foi tirada", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void reloadImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(productImageView);
    }

    private void preUpload(Uri imageUri) {
        if (imageUri == null) return;
        Button continueButton = findViewById(R.id.updateProductAddProductButton);
        if (continueButton != null) {
            continueButton.setEnabled(false);
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
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        uploadedImageUrl = url;
                        runOnUiThread(() -> {
                            if (productImageView != null) {
                                Glide.with(UpdateProduct.this)
                                        .load(url)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(productImageView);
                            }
                            Button continueButton = findViewById(R.id.registerProductAddProductButton);
                            if (continueButton != null) {
                                continueButton.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            Toast.makeText(UpdateProduct.this, "Erro no upload: " + (error != null ? error.getDescription() : "desconhecido"), Toast.LENGTH_SHORT).show();
                            Button continueButton = findViewById(R.id.registerProductAddProductButton);
                            if (continueButton != null) {
                                continueButton.setEnabled(false);
                            }
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch(UpdateProduct.this);
    }
}