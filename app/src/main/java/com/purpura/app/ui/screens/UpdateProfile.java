package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.purpura.app.R;
import com.purpura.app.configuration.PriceMask;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.remote.service.MongoService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfile extends AppCompatActivity {

    private final MongoService mongoService = new MongoService();

    private TextView header;
    private EditText email;
    private EditText phone;
    private EditText name;
    private ImageView back;
    private Button update;
    private ImageView exportIcon;
    private ImageView profileImage;

    @Nullable private Company company;
    @Nullable private Call<Company> fetchCall;

    private String cnpj;

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
        email = findViewById(R.id.updateEmail);
        phone = findViewById(R.id.updatePhone);
        name = findViewById(R.id.updateCompanyName);
        back = findViewById(R.id.registerProductBackButton2);
        update = findViewById(R.id.registerButton);
        exportIcon = findViewById(R.id.imageView34);
        profileImage = findViewById(R.id.updateProfileImage);

        phone.addTextChangedListener(new PriceMask.PhoneMask(phone));

        String extraCnpj = getIntent() != null ? getIntent().getStringExtra("cnpj") : "";
        String urlPhoto = getIntent() != null ? getIntent().getStringExtra("urlPhoto") : "";
        cnpj = sanitize(extraCnpj);

        header.setText("Atualizando Perfil");
        loadImage(urlPhoto);

        if (TextUtils.isEmpty(cnpj)) {
            header.setText("CNPJ inválido");
        } else {
            fetchCompany(cnpj);
        }

        back.setOnClickListener(v -> finish());

        update.setOnClickListener(v -> {
            if (company == null) company = new Company(cnpj, "", "", "", "");
            company.setNome(n(name.getText().toString()));
            company.setEmail(n(email.getText().toString()));
            company.setPhone(n(phone.getText().toString()));
            Object tag = profileImage.getTag();
            if (tag instanceof String) company.setUrlFoto((String) tag);
            mongoService.updateCompany(cnpj, company, v.getContext());
        });

        exportIcon.setOnClickListener(v -> {
            Object tag = profileImage.getTag();
            if (tag instanceof String) {
                if (company == null) company = new Company(cnpj, "", "", "", "");
                company.setUrlFoto((String) tag);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchCall != null) fetchCall.cancel();
    }

    private void fetchCompany(String cnpj) {
        header.setText("Carregando...");
        fetchCall = mongoService.getCompanyByCnpj(cnpj);
        fetchCall.enqueue(new Callback<Company>() {
            @Override public void onResponse(Call<Company> call, Response<Company> r) {
                if (r.isSuccessful() && r.body() != null) {
                    company = r.body();
                    name.setText(n(company.getNome()));
                    email.setText(n(company.getEmail()));
                    phone.setText(n(company.getPhone()));
                    loadImage(n(company.getUrlFoto()));
                    header.setText("Atualizando Perfil");
                } else {
                    header.setText("Empresa não encontrada");
                    probeByListingCompanies();
                }
            }
            @Override public void onFailure(Call<Company> call, Throwable t) {
                header.setText("Falha ao carregar");
            }
        });
    }

    private void probeByListingCompanies() {
        mongoService.getAllCompanies().enqueue(new Callback<List<Company>>() {
            @Override public void onResponse(Call<List<Company>> call, Response<List<Company>> resp) {
                for (Company c : resp.body()) {
                    if (sanitize(n(c.getCnpj())).equals(cnpj)) {
                        company = c;
                        name.setText(n(company.getNome()));
                        email.setText(n(company.getEmail()));
                        phone.setText(n(company.getPhone()));
                        loadImage(n(company.getUrlFoto()));
                        header.setText("Atualizando Perfil");
                        break;
                    }
                }
            }
            @Override public void onFailure(Call<List<Company>> call, Throwable t) {
                Log.e("UpdateProfile", "probe error", t);
            }
        });
    }

    private void loadImage(String url) {
        profileImage.setTag(url);
        Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(profileImage);
    }

    private static String n(String s) { return s == null ? "" : s.trim(); }
    private static String sanitize(String s) { return s == null ? "" : s.replaceAll("\\D+",""); }
}
