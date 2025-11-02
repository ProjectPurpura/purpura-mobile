package com.purpura.app.ui.screens;

import android.app.Activity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.configuration.Notifications;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.model.postgres.order.OrderRequest;
import com.purpura.app.model.postgres.order.OrderResponse;
import com.purpura.app.model.postgres.order.OrderItem;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.remote.service.PostgresService;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductPage extends AppCompatActivity {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private final Methods methods = new Methods();
    private final MongoService mongoService = new MongoService();
    private final PostgresService postgresService = new PostgresService();

    @Nullable private Residue residue;
    Activity activity = ProductPage.this;

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
    private Button buyNow;
    private Button addToCart;
    private Button goToChat;

    @Nullable private Company company;
    @Nullable private Address address;

    private Call<Company> companyCall;
    private Call<Address> addressCall;
    private boolean probeStarted = false;
    String sellerId;
    String cnpj;
    Bundle chat = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_page);
        bindViews();
        addToCart.setEnabled(false);
        goToChat.setEnabled(false);
        Bundle env = getIntent().getExtras();
        String cnpjFromIntent = env != null ? env.getString("cnpj", "") : "";
        Serializable ser = env != null ? env.getSerializable("residue") : null;
        if (ser instanceof Residue) residue = (Residue) ser;

        if (residue == null) {
            methods.openScreenActivity(this, com.purpura.app.ui.screens.errors.InternetError.class);
            finish();
            return;
        }
        if (backButton != null) backButton.setOnClickListener(v -> finish());
        setupResidueData(cnpjFromIntent);

        if (addQuantity != null) {
            addQuantity.setOnClickListener(v -> {
                int currentQuantity = safeParseInt(productQuantity != null ? productQuantity.getText().toString() : "1", 1);
                int estoque = (residue != null) ? residue.getEstoque() : Integer.MAX_VALUE;
                if (productQuantity != null) {
                    if (currentQuantity + 1 <= estoque) {
                        productQuantity.setText(String.valueOf(currentQuantity + 1));
                    } else {
                        Toast.makeText(ProductPage.this, "Quantidade máxima atingida", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (removeQuantity != null) {
            removeQuantity.setOnClickListener(v -> {
                int currentQuantity = safeParseInt(productQuantity != null ? productQuantity.getText().toString() : "1", 1);
                if (productQuantity != null && currentQuantity > 1) {
                    productQuantity.setText(String.valueOf(currentQuantity - 1));
                }
            });
        }

        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        cnpj = document.getString("cnpj");
                        Log.d("ProductPage", "Firestore cnpj encontrado: " + cnpj);
                        if (!isEmpty(cnpj)) {
                            addToCart.setEnabled(true);
                            goToChat.setEnabled(true);
                            goToChat.setOnClickListener(v -> {
                                chat.putString("buyerId", cnpj);
                                methods.openScreenActivityWithBundle(this, ChatIndividual.class, chat);
                            });
                        } else {
                            Toast.makeText(this, "CNPJ do usuário não encontrado, operação não permitida.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("ProductPage", "Documento Firestore empresa não existe");
                    }
                });

        buyNow.setOnClickListener(v -> {
            if (isEmpty(sellerId)) {
                Toast.makeText(this, "Erro: vendedor não identificado, por favor tente novamente.", Toast.LENGTH_SHORT).show();
                return;
            }
            createOrder();
        });
    }

    public void setupResidueData(String cnpjFallback) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(PT_BR);
        setTextSafe(residueName, nvl(residue.getNome()));
        setTextSafe(residuePrice, formatPriceBRL(residue.getPreco(), nf));
        if (residueDescription != null) residueDescription.setText(nvl(residue.getDescricao()));
        setTextSafe(residueWeight, formatNumber(residue.getPeso()));
        setTextSafe(residueUnitType, nvl(residue.getTipoUnidade()));
        loadResidueImage(residue.getUrlFoto());
        setTextSafe(companyName, "Carregando empresa...");
        setTextSafe(addressName, "Carregando endereço...");

        sellerId = effectiveCnpj(nvl(residue.getCnpj()), cnpjFallback);
        Log.d("ProductPage", "CNPJ efetivo para sellerId: " + sellerId);

        loadCompany(sellerId);
        loadAddress(sellerId, residue.getIdEndereco());
    }

    private void loadCompany(String cnpj) {
        if (isEmpty(cnpj)) {
            setTextSafe(companyName, "Empresa não encontrada");
            loadCompanyPhoto(null);
            probeOwnerCnpj();
            return;
        }
        companyCall = mongoService.getCompanyByCnpj(cnpj);
        companyCall.enqueue(new Callback<Company>() {
            @Override
            public void onResponse(Call<Company> call, Response<Company> response) {
                if (response.isSuccessful() && response.body() != null) {
                    company = response.body();
                    chat.putString("SellerId", company.getCnpj());
                    String nome = nvl(company.getNome());
                    setTextSafe(companyName, notEmpty(nome) ? nome : "—");
                    loadCompanyPhoto(company.getUrlFoto());

                    sellerId = company.getCnpj();
                    Log.d("ProductPage", "SellerId definido em loadCompany: " + sellerId);
                    addToCart.setEnabled(true);
                } else {
                    setTextSafe(companyName, "Empresa não encontrada");
                    loadCompanyPhoto(null);
                    probeOwnerCnpj();
                }
            }

            @Override
            public void onFailure(Call<Company> call, Throwable t) {
                setTextSafe(companyName, "Empresa não encontrada");
                loadCompanyPhoto(null);
                probeOwnerCnpj();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (companyCall != null) companyCall.cancel();
        if (addressCall != null) addressCall.cancel();
    }

    public void createOrder(){
        OrderRequest order = new OrderRequest(
                sellerId,
                cnpj,
                ""
        );

        postgresService.createOrder(order).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if(response.isSuccessful()){
                    try {
                        addResidueIntoOrder(response.body().getIdPedido());
                    } catch(Exception e){
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
            }
        });
    }

    public void addResidueIntoOrder(Integer id){
        OrderItem item = new OrderItem(
                residue.getId(),
                residue.getUrlFoto(),
                residue.getNome(),
                residue.getPreco(),
                Integer.parseInt(productQuantity.getText().toString()),
                residue.getTipoUnidade(),
                residue.getPeso()
        );
        postgresService.addItemOrder(item, id)
                .enqueue(new Callback<OrderItem>() {
                    @Override
                    public void onResponse(Call<OrderItem> call, Response<OrderItem> response) {
                        if(response.isSuccessful()){
                            new Notifications().orderNotification(activity, ProductPage.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<OrderItem> call, Throwable t) {

                    }
                });
    }
    private void bindViews() {
        buyNow = findViewById(R.id.productPageAddToShoppingCart);
        backButton         = findViewById(R.id.productPageBackButton);
        residueImage       = findViewById(R.id.productPageImage);
        residueName        = findViewById(R.id.productPageProductName);
        residuePrice       = findViewById(R.id.productPageProductValue2);
        residueDescription = findViewById(R.id.productPageDescription);
        residueWeight      = findViewById(R.id.productPageProductWeight);
        residueUnitType    = findViewById(R.id.producPageUnitMesure);
        if (residueUnitType == null) {
            int altId = getResources().getIdentifier("productPageUnitMeasure", "id", getPackageName());
            if (altId != 0) residueUnitType = findViewById(altId);
        }
        companyPhoto       = findViewById(R.id.productPageCompanyPhoto);
        companyName        = findViewById(R.id.productPageCompanyName);
        addressName        = findViewById(R.id.productPageProductLocation);
        addToCart          = findViewById(R.id.productPageAddToShoppingCart);
        goToChat           = findViewById(R.id.productPageGoToChat2);
        productQuantity    = findViewById(R.id.productPageQuantity);
        addQuantity        = findViewById(R.id.addQuantity);
        removeQuantity     = findViewById(R.id.removeQuantity);
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
    private void loadCompanyPhoto(@Nullable String url) {
        if (companyPhoto == null) return;
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
            setTextSafe(addressName, "Endereço não encontrado");
            return;
        }
        addressCall = mongoService.getAdressById(cnpj, enderecoId);
        addressCall.enqueue(new Callback<Address>() {
            @Override public void onResponse(Call<Address> call, Response<Address> response) {
                if (response.isSuccessful() && response.body() != null) {
                    address = response.body();
                    String nomeEnd = nvl(address.getNome());
                    setTextSafe(addressName, notEmpty(nomeEnd) ? nomeEnd : "—");
                } else {
                    setTextSafe(addressName, "Endereço não encontrado");
                }
            }
            @Override public void onFailure(Call<Address> call, Throwable t) {
                setTextSafe(addressName, "Endereço não encontrado");
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

    private void tryCompaniesSequentially (List < Company > companies,int idx){
        if (idx >= companies.size()) return;
        String cnpjTry = sanitize(nvl(companies.get(idx).getCnpj()));
        mongoService.getResidueById(cnpjTry, residue.getId()).enqueue(new Callback<Residue>() {
            @Override
            public void onResponse(Call<Residue> call, Response<Residue> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    loadCompany(cnpjTry);
                    loadAddress(cnpjTry, residue.getIdEndereco());
                } else {
                    tryCompaniesSequentially(companies, idx + 1);
                }
            }

            @Override
            public void onFailure(Call<Residue> call, Throwable t) {
                tryCompaniesSequentially(companies, idx + 1);
            }
        });
    }

    private static String nvl (String s){
        return s == null ? "" : s;
    }
    private static boolean isEmpty (String s){
        return s == null || s.trim().isEmpty();
    }
    private static boolean notEmpty (String s){
        return !isEmpty(s);
    }
    private static String sanitize (String s){
        return s == null ? "" : s.replaceAll("\\D+", "");
    }
    private String effectiveCnpj (String cnpjResidue, String cnpjFallback){
        String c = sanitize(cnpjResidue);
        if (isEmpty(c)) c = sanitize(cnpjFallback);
        return c;
    }
    private static void setTextSafe (TextView v, String text){
        if (v != null) v.setText(text == null ? "" : text);
    }
    private static int safeParseInt (String s,int def){
        try {
            return Integer.parseInt(TextUtils.isEmpty(s) ? String.valueOf(def) : s.trim());
        } catch (Exception e) {
            return def;
        }
    }
    private static String formatPriceBRL (Object price, NumberFormat nf){
        if (price == null) return nf.format(0);
        try {
            if (price instanceof Number) return nf.format(((Number) price).doubleValue());
            String s = price.toString().trim().replace(",", ".");
            return nf.format(Double.parseDouble(s));
        } catch (Exception e) {
            return nf.format(0);
        }
    }
    private static String formatNumber (Object n){
        try {
            if (n instanceof Number)
                return String.format(PT_BR, "%,.2f", ((Number) n).doubleValue());
            return String.format(PT_BR, "%,.2f", Double.parseDouble(String.valueOf(n).replace(",", ".")));
        } catch (Exception e) {
            return "0,00";
        }
    }
}
