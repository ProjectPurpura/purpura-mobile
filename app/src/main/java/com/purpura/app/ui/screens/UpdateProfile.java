package com.purpura.app.ui.screens;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.remote.service.MongoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfile extends AppCompatActivity {

    private final MongoService mongoService = new MongoService();

    private TextView header;
    private EditText phone;
    private EditText name;
    private ImageView back;
    private Button update;
    private ImageView exportIcon;
    private ImageView profileImage;

    private Company company;
    private Call<Company> fetchCall;

    private String cnpj;
    private String uriImage;

    private static boolean cloudinaryInitialized = false;
    private final String cloud_name = "dughz83oa";
    private final String unsignedPreset = "Purpura";

    private ActivityResultLauncher<String[]> requestPermission;
    private ActivityResultLauncher<Intent> requestGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets s = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(s.left, s.top, s.right, s.bottom);
            return insets;
        });

        header = findViewById(R.id.textView3);
        phone = findViewById(R.id.updatePhone);
        name = findViewById(R.id.updateCompanyName);
        back = findViewById(R.id.registerProductBackButton2);
        update = findViewById(R.id.registerButton);
        exportIcon = findViewById(R.id.imageView34);
        profileImage = findViewById(R.id.updateProfileImage);

        initCloudinary();
        setupGallery(profileImage);
        checkPermissions();

        String extraCnpj = getIntent() != null ? getIntent().getStringExtra("cnpj") : "";
        String urlPhoto = getIntent() != null ? getIntent().getStringExtra("urlPhoto") : "";
        cnpj = sanitize(extraCnpj);

        header.setText("Atualizando Perfil");
        if (!TextUtils.isEmpty(urlPhoto)) {
            uriImage = urlPhoto;
            loadImage(urlPhoto);
        }

        if (!TextUtils.isEmpty(cnpj)) {
            fetchCompany(cnpj);
        } else {
            fetchCnpjFromFirestoreAndLoad();
        }

        back.setOnClickListener(v -> finish());

        update.setOnClickListener(v -> {
            String resolvedCnpj = resolveCnpj();
            if (TextUtils.isEmpty(resolvedCnpj)) {
                Toast.makeText(this, "CNPJ não informado.", Toast.LENGTH_SHORT).show();
                return;
            }

            String inputName = n(name.getText().toString());
            String inputPhone = n(phone.getText().toString());

            String currentName = company != null ? n(company.getNome()) : "";
            String currentEmail = company != null ? n(company.getEmail()) :
                    (FirebaseAuth.getInstance().getCurrentUser() != null ? n(FirebaseAuth.getInstance().getCurrentUser().getEmail()) : "");
            String currentPhone = company != null ? n(company.getPhone()) : "";
            String currentImg = company != null ? n(company.getUrlFoto()) : "";

            String finalName = TextUtils.isEmpty(inputName) ? currentName : inputName;
            String finalPhone = TextUtils.isEmpty(inputPhone) ? currentPhone : inputPhone;
            String finalEmail = currentEmail;

            String img = !TextUtils.isEmpty(uriImage) ? uriImage : null;
            if (img == null) {
                Object tag = profileImage.getTag();
                if (tag instanceof String && !TextUtils.isEmpty((String) tag)) img = (String) tag;
            }
            String finalImg = TextUtils.isEmpty(img) ? currentImg : img;

            Company body = new Company(resolvedCnpj, finalName, finalEmail, finalPhone, finalImg);
            body.setCnpj(resolvedCnpj);

            mongoService.updateCompanyCall(resolvedCnpj, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        updateFirestore(body, () -> {
                            Toast.makeText(UpdateProfile.this, "Perfil atualizado", Toast.LENGTH_SHORT).show();
                            goBackToAccount();
                        }, () -> {
                            Toast.makeText(UpdateProfile.this, "Atualizado no servidor, falhou no Firebase", Toast.LENGTH_SHORT).show();
                            goBackToAccount();
                        });
                    } else {
                        Toast.makeText(UpdateProfile.this, "Erro ao atualizar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("UpdateProfile", "updateCompany failure", t);
                    Toast.makeText(UpdateProfile.this, "Falha na atualização: " + n(t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        });

        exportIcon.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(uriImage)) {
                if (company == null) company = new Company(resolveCnpj(), "", "", "", "");
                company.setUrlFoto(uriImage);
                Toast.makeText(this, "URL da foto aplicada ao perfil.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selecione uma imagem primeiro.", Toast.LENGTH_SHORT).show();
            }
        });

        profileImage.setOnClickListener(v -> openGallery());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchCall != null) fetchCall.cancel();
    }

    private void fetchCnpjFromFirestoreAndLoad() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String c = doc.getString("cnpj");
                    if (!TextUtils.isEmpty(c)) {
                        cnpj = sanitize(c);
                        fetchCompany(cnpj);
                    } else {
                        header.setText("CNPJ não encontrado");
                    }
                })
                .addOnFailureListener(e -> header.setText("Erro ao obter CNPJ"));
    }

    private String resolveCnpj() {
        if (!TextUtils.isEmpty(cnpj)) return cnpj;
        if (company != null && !TextUtils.isEmpty(company.getCnpj())) return sanitize(company.getCnpj());
        return "";
    }

    private void fetchCompany(String cnpj) {
        header.setText("Carregando...");
        fetchCall = mongoService.getCompanyByCnpj(cnpj);
        fetchCall.enqueue(new Callback<Company>() {
            @Override
            public void onResponse(Call<Company> call, Response<Company> r) {
                if (r.isSuccessful() && r.body() != null) {
                    company = r.body();
                    if (!TextUtils.isEmpty(company.getCnpj())) UpdateProfile.this.cnpj = sanitize(company.getCnpj());
                    name.setText(n(company.getNome()));
                    phone.setText(n(company.getPhone()));
                    String u = n(company.getUrlFoto());
                    if (!TextUtils.isEmpty(u)) {
                        uriImage = u;
                        loadImage(u);
                    }
                    header.setText("Atualizando Perfil");
                } else {
                    header.setText("Empresa não encontrada");
                    probeByListingCompanies();
                }
            }

            @Override
            public void onFailure(Call<Company> call, Throwable t) {
                header.setText("Falha ao carregar");
            }
        });
    }

    private void probeByListingCompanies() {
        mongoService.getAllCompanies().enqueue(new Callback<List<Company>>() {
            @Override
            public void onResponse(Call<List<Company>> call, Response<List<Company>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) return;
                for (Company c : resp.body()) {
                    if (!TextUtils.isEmpty(c.getCnpj())) {
                        company = c;
                        cnpj = sanitize(c.getCnpj());
                        name.setText(n(company.getNome()));
                        phone.setText(n(company.getPhone()));
                        String u = n(company.getUrlFoto());
                        if (!TextUtils.isEmpty(u)) {
                            uriImage = u;
                            loadImage(u);
                        }
                        header.setText("Atualizando Perfil");
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Company>> call, Throwable t) {
                Log.e("UpdateProfile", "probe error", t);
            }
        });
    }

    private void updateFirestore(Company body, Runnable onSuccess, Runnable onError) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) { onError.run(); return; }
        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> patch = new HashMap<>();
        patch.put("cnpj", body.getCnpj());
        patch.put("nome", body.getNome());
        patch.put("email", body.getEmail());
        patch.put("phone", body.getPhone());
        patch.put("urlFoto", body.getUrlFoto());
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(uid)
                .set(patch, SetOptions.merge())
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.run());
    }

    private void goBackToAccount() {
        Intent i = new Intent(this, com.purpura.app.ui.screens.MainActivity.class);
        i.putExtra("navigateTo", "account");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void loadImage(String url) {
        profileImage.setTag(url);
        Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(profileImage);
    }

    private void initCloudinary() {
        if (!cloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloud_name);
            MediaManager.init(this, config);
            cloudinaryInitialized = true;
        }
    }

    private void setupGallery(ImageView imageView) {
        requestGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null && result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            uploadImagem(imageUri, new ImageUploadCallback() {
                                @Override
                                public void onUploadSuccess(String imageUrl) {
                                    uriImage = imageUrl;
                                    runOnUiThread(() -> Glide.with(UpdateProfile.this)
                                            .load(imageUrl)
                                            .transform(new CircleCrop())
                                            .into(imageView));
                                    Toast.makeText(UpdateProfile.this, "Imagem carregada com sucesso!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onUploadFailure(String error) {
                                    Toast.makeText(UpdateProfile.this, "Erro ao enviar imagem: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    private void uploadImagem(Uri imageUri, ImageUploadCallback callback) {
        MediaManager.get().upload(imageUri)
                .option("folder", "Purpura")
                .unsigned(unsignedPreset)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        runOnUiThread(() -> Glide.with(UpdateProfile.this).load(url).into(profileImage));
                        callback.onUploadSuccess(url);
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        callback.onUploadFailure(error.getDescription());
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onUploadFailure("Reagendado: " + error.getDescription());
                    }
                })
                .dispatch(UpdateProfile.this);
    }

    private void checkPermissions() {
        requestPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});
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
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        requestGallery.launch(intent);
    }

    private static String n(String s) {
        return s == null ? "" : s.trim();
    }

    private static String sanitize(String s) {
        return s == null ? "" : s.replaceAll("\\D+", "");
    }

    interface ImageUploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(String error);
    }
}
