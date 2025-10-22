package com.purpura.app.adapters.mongo;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.remote.service.MongoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileAdapter extends RecyclerView.Adapter<UpdateProfileAdapter.VH> {

    private enum State { LOADING, SUCCESS, ERROR }

    private final MongoService mongoService = new MongoService();
    private final String cnpj;
    private Company company;
    private State state = State.LOADING;
    private Call<Company> call;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long TIMEOUT = 12000L;

    public UpdateProfileAdapter(@NonNull String cnpj) {
        this.cnpj = cnpj == null ? "" : cnpj.replaceAll("\\D+","");
        fetch();
        handler.postDelayed(() -> {
            if (state == State.LOADING && call != null) {
                call.cancel();
                state = State.ERROR;
                notifyItemChanged(0);
            }
        }, TIMEOUT);
    }

    private void fetch() {
        if (TextUtils.isEmpty(cnpj)) { state = State.ERROR; notifyItemChanged(0); return; }
        call = mongoService.getCompanyByCnpj(cnpj);
        call.enqueue(new Callback<Company>() {
            @Override public void onResponse(@NonNull Call<Company> c, @NonNull Response<Company> r) {
                if (r.isSuccessful() && r.body() != null) {
                    company = r.body();
                    state = State.SUCCESS;
                } else {
                    state = State.ERROR;
                }
                notifyItemChanged(0);
            }
            @Override public void onFailure(@NonNull Call<Company> c, @NonNull Throwable t) {
                state = State.ERROR;
                notifyItemChanged(0);
            }
        });
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_update_profile, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        if (state == State.SUCCESS && company != null) {
            h.name.setText(n(company.getNome()));
            h.email.setText(n(company.getEmail()));
            h.phone.setText(n(company.getPhone()));
            Glide.with(h.photo.getContext())
                    .load(n(company.getUrlFoto()))
                    .into(h.photo);
        } else if (state == State.ERROR) {
            h.header.setText("Falha ao carregar");
        } else {
            h.header.setText("Carregando...");
        }

        h.back.setOnClickListener(v -> {
            if (v.getContext() instanceof Activity) ((Activity) v.getContext()).finish();
        });

        h.update.setOnClickListener(v -> {
            if (company == null) company = new Company(cnpj, "", "", "", "");
            company.setNome(h.name.getText().toString().trim());
            company.setEmail(h.email.getText().toString().trim());
            company.setPhone(h.phone.getText().toString().trim());
            Object tag = h.photo.getTag();
            if (tag instanceof String) company.setUrlFoto((String) tag);
            mongoService.updateCompany(cnpj, company, v.getContext());
        });

        h.exportIcon.setOnClickListener(v -> {
            Object url = h.photo.getTag();
            if (url instanceof String) {
                company.setUrlFoto((String) url);
            }
        });
    }

    @Override
    public int getItemCount() { return 1; }

    private static String n(String s) { return s == null ? "" : s; }

    public static class VH extends RecyclerView.ViewHolder {
        TextView header;
        EditText email;
        EditText phone;
        EditText name;
        ImageView back;
        Button update;
        ImageView exportIcon;
        ImageView photo;

        public VH(@NonNull View v) {
            super(v);
            header = v.findViewById(R.id.textView3);
            email = v.findViewById(R.id.updateEmail);
            phone = v.findViewById(R.id.updatePhone);
            name = v.findViewById(R.id.updateCompanyName);
            back = v.findViewById(R.id.registerProductBackButton2);
            update = v.findViewById(R.id.registerButton);
            exportIcon = v.findViewById(R.id.imageView34);
            photo = v.findViewById(R.id.registerImage);
        }
    }
}
