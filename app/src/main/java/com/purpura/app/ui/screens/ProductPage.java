package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductPage extends AppCompatActivity {

    private final Methods methods = new Methods();
    private final MongoService mongoService = new MongoService();

    @Nullable private Residue residue;

    private ImageView backButton;
    private ImageView residueImage;
    private TextView residueName;
    private TextView residuePrice;
    private EditText residueDescription;
    private TextView residueWeight;
    private TextView residueUnitType;
    private ShapeableImageView companyPhoto;
    private TextView companyName;
    private TextView addressName;
    private ImageView addQuantity;
    private TextView productQuantity;
    private ImageView removeQuantity;
    private Button addToCart;
    private Button goToChat;

    @Nullable private Company company;
    @Nullable private Address address;

    private Call<Company> companyCall;
    private Call<Address> addressCall;
    private boolean probeStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_page);
        bindViews();
        Bundle env = getIntent().getExtras();
        String cnpjFromIntent = env != null ? env.getString("cnpj", "") : "";
        Serializable ser = env != null ? env.getSerializable("residue") : null;
        if (ser instanceof Residue) residue = (Residue) ser;
        if (residue == null) {
            methods.openScreenActivity(this, com.purpura.app.ui.screens.errors.InternetError.class);
            finish();
            return;
        }
        backButton.setOnClickListener(v -> finish());
        setupResidueData(cnpjFromIntent);

        addQuantity.setOnClickListener(v -> {

            Integer currentQuantity = Integer.getInteger(productQuantity.getText().toString());

            if(residue.getEstoque() >= currentQuantity + 1){
                productQuantity.setText(String.valueOf(currentQuantity + 1));
            } else {
                Toast.makeText(this, "Quantidade máxima atingida", Toast.LENGTH_SHORT).show();
            }
        });

        removeQuantity.setOnClickListener(v -> {
            Integer currentQuantity = Integer.getInteger(productQuantity.getText().toString());
            if(currentQuantity > 1){
                productQuantity.setText(String.valueOf(currentQuantity - 1));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (companyCall != null) companyCall.cancel();
        if (addressCall != null) addressCall.cancel();
    }

    private void bindViews() {
        backButton         = findViewById(R.id.productPageBackButton);
        residueImage       = findViewById(R.id.productPageImage);
        residueName        = findViewById(R.id.productPageProductName);
        residuePrice       = findViewById(R.id.productPageProductValue);
        residueDescription = findViewById(R.id.productPageDescription);
        residueWeight      = findViewById(R.id.productPageProductWeight);
        residueUnitType    = findViewById(R.id.producPageUnitMesure);
        companyPhoto       = findViewById(R.id.productPageCompanyPhoto);
        companyName        = findViewById(R.id.productPageCompanyName);
        addressName        = findViewById(R.id.productPageProductLocation);
        addToCart          = findViewById(R.id.productPageAddToShoppingCart);
        goToChat           = findViewById(R.id.productPageGoToChat);
        productQuantity    = findViewById(R.id.productPageQuantity);
        addQuantity         = findViewById(R.id.addQuantity);
        removeQuantity      = findViewById(R.id.removeQuantity);
    }

    public void setupResidueData(String cnpjFallback) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        residueName.setText(nvl(residue.getNome()));
        residuePrice.setText("R$ " + df.format(residue.getPreco()));
        residueDescription.setText(nvl(residue.getDescricao()));
        residueWeight.setText(df.format(residue.getPeso()));
        residueUnitType.setText(nvl(residue.getTipoUnidade()));
        loadResidueImage(residue.getUrlFoto());
        companyName.setText("Carregando empresa...");
        addressName.setText("Carregando endereço...");
        String cnpj = effectiveCnpj(nvl(residue.getCnpj()), cnpjFallback);
        Log.d("ProductPage", "ResidueId=" + residue.getId() + ", IdEndereco=" + residue.getIdEndereco() + ", CNPJ=" + cnpj);
        loadCompany(cnpj);
        loadAddress(cnpj, residue.getIdEndereco());
    }

    private void loadResidueImage(@Nullable String url) {
        if (residueImage == null) return;
        Glide.with(residueImage.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(residueImage);
    }

    private void loadCompany(String cnpj) {
        if (isEmpty(cnpj)) {
            companyName.setText("Empresa não encontrada");
            loadCompanyPhoto(null);
            probeOwnerCnpj();
            return;
        }
        companyCall = mongoService.getCompanyByCnpj(cnpj);
        companyCall.enqueue(new Callback<Company>() {
            @Override public void onResponse(Call<Company> call, Response<Company> response) {
                if (response.isSuccessful() && response.body() != null) {
                    company = response.body();
                    String nome = nvl(company.getNome());
                    companyName.setText(notEmpty(nome) ? nome : "—");
                    loadCompanyPhoto(company.getUrlFoto());
                } else {
                    companyName.setText("Empresa não encontrada");
                    loadCompanyPhoto(null);
                    probeOwnerCnpj();
                }
            }
            @Override public void onFailure(Call<Company> call, Throwable t) {
                companyName.setText("Empresa não encontrada");
                loadCompanyPhoto(null);
                probeOwnerCnpj();
            }
        });
    }

    private void loadCompanyPhoto(@Nullable String url) {
        Glide.with(this)
                .load(url)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(companyPhoto);
    }

    private void loadAddress(String cnpj, @Nullable String enderecoId) {
        if (isEmpty(cnpj) || isEmpty(enderecoId)) {
            addressName.setText("Endereço não encontrado");
            return;
        }
        addressCall = mongoService.getAdressById(cnpj, enderecoId);
        addressCall.enqueue(new Callback<Address>() {
            @Override public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful() && response.body() != null) {
                    address = response.body();
                    String nomeEnd = nvl(address.getNome());
                    addressName.setText(notEmpty(nomeEnd) ? nomeEnd : "—");
                } else {
                    addressName.setText("Endereço não encontrado");
                }
            }
            @Override public void onFailure(Call<Address> call, Throwable t) {
                addressName.setText("Endereço não encontrado");
            }
        });
    }

    private void probeOwnerCnpj() {
        if (probeStarted || residue == null) return;
        probeStarted = true;
        mongoService.getAllCompanies().enqueue(new Callback<List<Company>>() {
            @Override public void onResponse(Call<List<Company>> call, Response<List<Company>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) return;
                tryCompaniesSequentially(resp.body(), 0);
            }
            @Override public void onFailure(Call<List<Company>> call, Throwable t) { }
        });
    }

    private void tryCompaniesSequentially(List<Company> companies, int idx) {
        if (idx >= companies.size()) return;
        String cnpjTry = sanitize(nvl(companies.get(idx).getCnpj()));
        mongoService.getResidueById(cnpjTry, residue.getId()).enqueue(new Callback<Residue>() {
            @Override public void onResponse(Call<Residue> call, Response<Residue> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    loadCompany(cnpjTry);
                    loadAddress(cnpjTry, residue.getIdEndereco());
                } else {
                    tryCompaniesSequentially(companies, idx + 1);
                }
            }
            @Override public void onFailure(Call<Residue> call, Throwable t) {
                tryCompaniesSequentially(companies, idx + 1);
            }
        });
    }

    private static String nvl(String s) { return s == null ? "" : s; }
    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static boolean notEmpty(String s) { return !isEmpty(s); }
    private static String sanitize(String s) { return s == null ? "" : s.replaceAll("\\D+", ""); }
    private String effectiveCnpj(String cnpjResidue, String cnpjFallback) {
        String c = sanitize(cnpjResidue);
        if (isEmpty(c)) c = sanitize(cnpjFallback);
        return c;
    }
}
