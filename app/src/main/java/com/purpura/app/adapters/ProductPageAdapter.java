package com.purpura.app.adapters;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.purpura.app.R;
import com.purpura.app.model.mongo.Address;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.model.mongo.Residue;
import com.purpura.app.remote.service.MongoService;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductPageAdapter extends RecyclerView.Adapter<ProductPageAdapter.ViewHolder> {

    private static final String TAG = "ProductPageAdapter";
    private enum LoadState { LOADING, SUCCESS, ERROR }

    private final MongoService mongoService = new MongoService();
    private final Residue residue;

    // Fallback de CNPJ (quando Residue não traz cnpj mapeado)
    private final String cnpjFallback;

    // Dados e estados
    private Company company;
    private Address address;
    private LoadState companyState = LoadState.LOADING;
    private LoadState addressState = LoadState.LOADING;

    private boolean companyRequested = false;
    private boolean addressRequested = false;

    private Call<Company> companyCall;
    private Call<Address> addressCall;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long TIMEOUT_MS = 12000L; // 12s watchdog

    public ProductPageAdapter(@NonNull Residue residue, @NonNull String cnpjFallback) {
        this.residue = residue;
        // limpa e guarda CNPJ externo (não confie no model depois do refresh)
        this.cnpjFallback = sanitizeCnpj(cnpjFallback);

        fetchCompanyOnce();
        fetchAddressOnce();
        startWatchdogs();
    }

    // Construtor compatível (se algum lugar antigo ainda chama com 1 arg)
    public ProductPageAdapter(@NonNull Residue residue) {
        this(residue, "");
    }

    private String sanitizeCnpj(String c) {
        if (c == null) return "";
        return c.replaceAll("\\D+", "");
    }

    private String effectiveCnpj() {
        // tenta do Residue, senão usa fallback fornecido pela Activity
        String c = residue.getCnpj();
        if (TextUtils.isEmpty(c)) c = cnpjFallback;
        return sanitizeCnpj(c);
    }

    private void fetchCompanyOnce() {
        if (companyRequested) return;
        companyRequested = true;
        companyState = LoadState.LOADING;

        String cnpj = effectiveCnpj();
        if (TextUtils.isEmpty(cnpj)) {
            Log.w(TAG, "CNPJ vazio/nulo para buscar empresa");
            company = null;
            companyState = LoadState.ERROR;
            notifyItemChanged(0);
            return;
        }

        companyCall = mongoService.getCompanyByCnpj(cnpj);
        companyCall.enqueue(new Callback<Company>() {
            @Override
            public void onResponse(@NonNull Call<Company> call, @NonNull Response<Company> resp) {
                if (resp.isSuccessful() && resp.body() != null
                        && notEmpty(resp.body().getNome())) {
                    company = resp.body();
                    companyState = LoadState.SUCCESS;
                } else {
                    Log.w(TAG, "Empresa não encontrada. code=" + resp.code());
                    company = null;
                    companyState = LoadState.ERROR;
                }
                notifyItemChanged(0);
            }

            @Override
            public void onFailure(@NonNull Call<Company> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao buscar empresa: " + t.getMessage(), t);
                company = null;
                companyState = LoadState.ERROR;
                notifyItemChanged(0);
            }
        });
    }

    private void fetchAddressOnce() {
        if (addressRequested) return;
        addressRequested = true;
        addressState = LoadState.LOADING;

        String cnpj = effectiveCnpj();
        if (TextUtils.isEmpty(cnpj)) {
            Log.w(TAG, "CNPJ vazio/nulo para buscar endereço");
            address = null;
            addressState = LoadState.ERROR;
            notifyItemChanged(0);
            return;
        }

        String enderecoId = residue.getIdEndereco();
        if (TextUtils.isEmpty(enderecoId)) {
            Log.w(TAG, "idEndereco vazio/nulo no Residue");
            address = null;
            addressState = LoadState.ERROR;
            notifyItemChanged(0);
            return;
        }

        addressCall = mongoService.getAdressById(cnpj, enderecoId);
        addressCall.enqueue(new Callback<Address>() {
            @Override
            public void onResponse(@NonNull Call<Address> call, @NonNull Response<Address> resp) {
                if (resp.isSuccessful() && resp.body() != null
                        && notEmpty(resp.body().getNome())) {
                    address = resp.body();
                    addressState = LoadState.SUCCESS;
                } else {
                    Log.w(TAG, "Endereço não encontrado. code=" + resp.code());
                    address = null;
                    addressState = LoadState.ERROR;
                }
                notifyItemChanged(0);
            }

            @Override
            public void onFailure(@NonNull Call<Address> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao buscar endereço: " + t.getMessage(), t);
                address = null;
                addressState = LoadState.ERROR;
                notifyItemChanged(0);
            }
        });
    }

    private void startWatchdogs() {
        handler.postDelayed(() -> {
            if (companyState == LoadState.LOADING) {
                Log.w(TAG, "Timeout empresa - marcando ERROR");
                if (companyCall != null) companyCall.cancel();
                companyState = LoadState.ERROR;
                notifyItemChanged(0);
            }
        }, TIMEOUT_MS);

        handler.postDelayed(() -> {
            if (addressState == LoadState.LOADING) {
                Log.w(TAG, "Timeout endereço - marcando ERROR");
                if (addressCall != null) addressCall.cancel();
                addressState = LoadState.ERROR;
                notifyItemChanged(0);
            }
        }, TIMEOUT_MS);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (companyCall != null) companyCall.cancel();
        if (addressCall != null) addressCall.cancel();
        handler.removeCallbacksAndMessages(null);
    }

    @NonNull
    @Override
    public ProductPageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_product_page, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductPageAdapter.ViewHolder h, int position) {
        // Imagens do resíduo
        List<String> imgs = isEmpty(residue.getUrlFoto())
                ? Collections.emptyList()
                : Arrays.asList(residue.getUrlFoto());
        h.viewPager.setAdapter(new ImagePagerAdapter(imgs));

        DecimalFormat df = new DecimalFormat("#,##0.00");

        // Campos do resíduo
        h.residueName.setText(nvl(residue.getNome()));
        h.residuePrice.setText("R$ " + df.format(residue.getPreco()));
        h.residueDescription.setText(nvl(residue.getDescricao()));
        h.residueWeight.setText(df.format(residue.getPeso()));
        h.residueUnitType.setText(nvl(residue.getTipoUnidade()));

        // COMPANY (estado)
        String companyName =
                companyState == LoadState.SUCCESS ? nvl(company != null ? company.getNome() : "") :
                        companyState == LoadState.ERROR   ? "Empresa não encontrada" :
                                "Carregando empresa...";
        h.companyName.setText(companyName);

        String companyPhotoUrl = (companyState == LoadState.SUCCESS && company != null)
                ? company.getUrlFoto() : null;

        Glide.with(h.companyPhoto.getContext())
                .load(companyPhotoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.companyPhoto);

        // ADDRESS (estado)
        String addressTxt =
                addressState == LoadState.SUCCESS ? nvl(address != null ? address.getNome() : "") :
                        addressState == LoadState.ERROR   ? "Endereço não encontrado" :
                                "Carregando endereço...";
        h.addressName.setText(addressTxt);
    }

    @Override
    public int getItemCount() { return 1; }

    private static String nvl(String s) { return s == null ? "" : s; }
    private static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
    private static boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 viewPager;
        TextView residueName;
        TextView residuePrice;
        ShapeableImageView companyPhoto;
        TextView companyName;
        EditText residueDescription;
        TextView residueWeight;
        TextView residueUnitType;
        TextView addressName;
        Button addToCart;
        Button goToChat;

        public ViewHolder(@NonNull View v) {
            super(v);
            viewPager = v.findViewById(R.id.viewPager);
            residueName = v.findViewById(R.id.productPageProductName);
            residuePrice = v.findViewById(R.id.productPageProductValue);
            companyPhoto = v.findViewById(R.id.productPageCompanyPhoto);
            companyName = v.findViewById(R.id.productPageCompanyName);
            residueDescription = v.findViewById(R.id.productPageDescription);
            residueWeight = v.findViewById(R.id.productPageProductWeight);
            residueUnitType = v.findViewById(R.id.producPageUnitMesure);
            addressName = v.findViewById(R.id.productPageProductLocation);
            addToCart = v.findViewById(R.id.productPageAddToShoppingCart);
            goToChat = v.findViewById(R.id.productPageGoToChat);
        }
    }

    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImgVH> {
        private final List<String> urls;
        ImagePagerAdapter(List<String> urls) { this.urls = urls; }

        static class ImgVH extends RecyclerView.ViewHolder {
            ImageView img;
            ImgVH(@NonNull View item) { super(item); img = (ImageView) item; }
        }

        @NonNull
        @Override
        public ImgVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImgVH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull ImgVH h, int pos) {
            Glide.with(h.img.getContext())
                    .load(urls.get(pos))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(h.img);
        }

        @Override
        public int getItemCount() { return urls.size(); }
    }
}
