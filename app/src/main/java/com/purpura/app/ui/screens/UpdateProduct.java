package com.purpura.app.ui.screens;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProduct extends AppCompatActivity {

    private final MongoService mongoService = new MongoService();

    private ImageView back;
    private EditText name;
    private EditText desc;
    private EditText price;
    private EditText weight;
    private EditText unit;
    private EditText stock;
    private ImageView image;
    private ImageView exportIcon;
    private Button update;

    private Residue residue;
    private String cnpj;
    private String residueId;
    private String imageUrl;

    private Call<Residue> fetchCall;

    private static boolean cloudinaryInitialized = false;
    private final String cloud_name = "dughz83oa";
    private final String unsignedPreset = "Purpura";

    private ActivityResultLauncher<String[]> requestPermission;
    private ActivityResultLauncher<Intent> requestGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_product);

        back = findViewById(R.id.updateProductBackButton);
        name = findViewById(R.id.updateProductName);
        desc = findViewById(R.id.updateProductDescription);
        price = findViewById(R.id.updateProductPrice);
        weight = findViewById(R.id.updateProductWeight);
        unit = findViewById(R.id.updateProductWeightType);
        stock = findViewById(R.id.updateProductQuantity);
        image = findViewById(R.id.updateProductImage);
        exportIcon = findViewById(R.id.updateProductExportImageIcon);
        update = findViewById(R.id.updateProductAddProductButton);

        price.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        price.setKeyListener(DigitsKeyListener.getInstance("0123456789,."));
        price.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2, ',')});

        initCloudinary();
        setupGallery(image);
        checkPermissions();

        Serializable ser = getIntent() != null ? getIntent().getSerializableExtra("residue") : null;
        String extraCnpj = getIntent() != null ? getIntent().getStringExtra("cnpj") : "";
        String extraId = getIntent() != null ? getIntent().getStringExtra("residueId") : "";
        cnpj = sanitize(extraCnpj);
        residueId = n(extraId);

        back.setOnClickListener(v -> finish());
        image.setOnClickListener(v -> openGallery());
        exportIcon.setOnClickListener(v -> {
            Object tag = image.getTag();
            String candidate = imageUrl;
            if (TextUtils.isEmpty(candidate) && tag instanceof String) candidate = (String) tag;
            if (!TextUtils.isEmpty(candidate)) {
                imageUrl = candidate;
                Toast.makeText(this, "Imagem definida.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selecione uma imagem primeiro.", Toast.LENGTH_SHORT).show();
            }
        });
        update.setOnClickListener(v -> doUpdate());

        if (ser instanceof Residue) {
            residue = (Residue) ser;
            if (TextUtils.isEmpty(residue.getCnpj())) residue.setCnpj(cnpj);
            fill(residue);
            return;
        }

        if (!TextUtils.isEmpty(cnpj) && !TextUtils.isEmpty(residueId)) {
            fetchResidue(cnpj, residueId);
        } else if (!TextUtils.isEmpty(residueId)) {
            fetchCnpjThenLoad(residueId);
        } else {
            Toast.makeText(this, "Dados insuficientes.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchCall != null) fetchCall.cancel();
    }

    private void fetchResidue(String c, String id) {
        fetchCall = mongoService.getResidueById(c, id);
        fetchCall.enqueue(new Callback<Residue>() {
            @Override public void onResponse(Call<Residue> call, Response<Residue> r) {
                if (r.isSuccessful() && r.body() != null) {
                    residue = r.body();
                    if (TextUtils.isEmpty(n(residue.getCnpj()))) residue.setCnpj(c);
                    fill(residue);
                } else {
                    Toast.makeText(UpdateProduct.this, "Produto não encontrado.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onFailure(Call<Residue> call, Throwable t) {
                Toast.makeText(UpdateProduct.this, "Falha ao carregar.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchCnpjThenLoad(String id) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Sem sessão.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String c = sanitize(doc.getString("cnpj"));
                    if (!TextUtils.isEmpty(c)) {
                        cnpj = c;
                        fetchResidue(cnpj, id);
                    } else {
                        Toast.makeText(this, "CNPJ não encontrado.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao obter CNPJ.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fill(Residue r) {
        name.setText(n(r.getNome()));
        desc.setText(n(r.getDescricao()));
        price.setText(toStr(r.getPreco()));
        weight.setText(toStr(r.getPeso()));
        unit.setText(n(r.getTipoUnidade()));
        stock.setText(String.valueOf(r.getEstoque()));
        imageUrl = n(r.getUrlFoto());
        image.setTag(imageUrl);
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(image);
        residueId = n(r.getId());
        cnpj = TextUtils.isEmpty(cnpj) ? sanitize(n(r.getCnpj())) : cnpj;
    }

    private void doUpdate() {
        if (residue == null) { Toast.makeText(this, "Produto não carregado.", Toast.LENGTH_SHORT).show(); return; }
        String c = sanitize(n(cnpj));
        if (TextUtils.isEmpty(c)) { Toast.makeText(this, "CNPJ não identificado.", Toast.LENGTH_SHORT).show(); return; }
        String id = n(residue.getId());
        if (TextUtils.isEmpty(id)) { Toast.makeText(this, "ID não identificado.", Toast.LENGTH_SHORT).show(); return; }

        String nome = n(name.getText() == null ? "" : name.getText().toString());
        String d = n(desc.getText() == null ? "" : desc.getText().toString());
        String un = n(unit.getText() == null ? "" : unit.getText().toString());
        double pr = parseDouble(n(price.getText() == null ? "" : price.getText().toString()));
        double pe = parseDouble(n(weight.getText() == null ? "" : weight.getText().toString()));
        int est = parseIntSafe(n(stock.getText() == null ? "" : stock.getText().toString()), 0);
        if (TextUtils.isEmpty(nome)) { Toast.makeText(this, "Informe o nome.", Toast.LENGTH_SHORT).show(); return; }
        if (pr < 0) { Toast.makeText(this, "Preço inválido.", Toast.LENGTH_SHORT).show(); return; }
        if (pe < 0) { Toast.makeText(this, "Peso inválido.", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(un)) { Toast.makeText(this, "Informe a unidade.", Toast.LENGTH_SHORT).show(); return; }

        String idEnd = n(residue.getIdEndereco());
        String idPix = n(residue.getIdChavePix());
        if (TextUtils.isEmpty(idEnd) || TextUtils.isEmpty(idPix)) {
            Toast.makeText(this, "Endereço/Chave Pix não definidos.", Toast.LENGTH_LONG).show();
            return;
        }

        Residue body = new Residue(
                id,
                nome,
                d,
                pe,
                pr,
                est,
                un,
                TextUtils.isEmpty(imageUrl) ? n(residue.getUrlFoto()) : imageUrl,
                idPix,
                idEnd,
                c
        );

        mongoService.updateResidue(c, id, body, this);
        Toast.makeText(this, "Atualizando...", Toast.LENGTH_SHORT).show();
    }

    private void initCloudinary() {
        if (!cloudinaryInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloud_name);
            MediaManager.init(this, config);
            cloudinaryInitialized = true;
        }
    }

    private void setupGallery(ImageView target) {
        requestGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null && result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            uploadImagem(uri, new ImageUploadCallback() {
                                @Override public void onUploadSuccess(String url) {
                                    imageUrl = url;
                                    runOnUiThread(() -> Glide.with(UpdateProduct.this)
                                            .load(url)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(target));
                                    Toast.makeText(UpdateProduct.this, "Imagem enviada.", Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onUploadFailure(String error) {
                                    Toast.makeText(UpdateProduct.this, "Erro ao enviar: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    private void uploadImagem(Uri imageUri, ImageUploadCallback cb) {
        MediaManager.get().upload(imageUri)
                .option("folder", "Purpura")
                .unsigned(unsignedPreset)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        cb.onUploadSuccess(url);
                    }
                    @Override public void onError(String requestId, ErrorInfo error) {
                        cb.onUploadFailure(error.getDescription());
                    }
                    @Override public void onReschedule(String requestId, ErrorInfo error) {
                        cb.onUploadFailure("Reagendado: " + error.getDescription());
                    }
                })
                .dispatch(this);
    }

    private void checkPermissions() {
        requestPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {});
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
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        requestGallery.launch(i);
    }

    private static String n(String s) { return s == null ? "" : s.trim(); }
    private static String sanitize(String s) { return s == null ? "" : s.replaceAll("\\D+", ""); }
    private static String toStr(double v) { return String.valueOf(v); }
    private static double parseDouble(String s) { try { return Double.parseDouble(s.replace(",", ".")); } catch (Exception e) { return -1.0; } }
    private static int parseIntSafe(String s, int def) { try { return Integer.parseInt(TextUtils.isEmpty(s) ? String.valueOf(def) : s.trim()); } catch (Exception e) { return def; } }

    interface ImageUploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(String error);
    }

    private static class DecimalDigitsInputFilter implements InputFilter {
        private final int maxInt;
        private final int maxFrac;
        private final char sep;
        private final java.util.regex.Pattern pattern;

        DecimalDigitsInputFilter(int maxInt, int maxFrac, char sep) {
            this.maxInt = maxInt;
            this.maxFrac = maxFrac;
            this.sep = sep;
            String esc = java.util.regex.Pattern.quote(String.valueOf(sep));
            this.pattern = java.util.regex.Pattern.compile("^\\d{0," + maxInt + "}" + "(" + esc + "\\d{0," + maxFrac + "})?$");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   android.text.Spanned dest, int dstart, int dend) {
            String incoming = source.toString().replace('.', sep);
            String before = dest.subSequence(0, dstart).toString();
            String after = dest.subSequence(dend, dest.length()).toString();
            String candidate = before + incoming + after;
            if (candidate.isEmpty()) return null;
            int countSep = 0;
            for (int i = 0; i < candidate.length(); i++) {
                if (candidate.charAt(i) == sep) countSep++;
            }
            if (countSep > 1) return "";
            if (!pattern.matcher(candidate).matches()) return "";
            return incoming.equals(source.toString()) ? null : incoming;
        }
    }
}
