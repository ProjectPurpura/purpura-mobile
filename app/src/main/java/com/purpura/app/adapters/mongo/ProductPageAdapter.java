package com.purpura.app.adapters.mongo;

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

    private enum LoadState { LOADING, SUCCESS, ERROR }

    private final MongoService mongoService = new MongoService();
    private final Residue residue;
    private final String cnpjFallback;

    private Company company;
    private Address address;
    private LoadState companyState = LoadState.LOADING;
    private LoadState addressState = LoadState.LOADING;

    private boolean companyRequested = false;
    private boolean addressRequested = false;

    private Call<Company> companyCall;
    private Call<Address> addressCall;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long TIMEOUT_MS = 12000L;

    private String ownerCnpj = "";
    private boolean probing = false;

    public ProductPageAdapter(@NonNull Residue residue, @NonNull String cnpjFallback) {
        this.residue = residue;
        this.cnpjFallback = sanitize(cnpjFallback);
        fetchCompanyOnce();
        fetchAddressOnce();
        startWatchdogs();
    }

    public ProductPageAdapter(@NonNull Residue residue) {
        this(residue, "");
    }

    private String sanitize(String s) { return s == null ? "" : s.replaceAll("\\D+", ""); }

    private String effectiveCnpj() {
        if (!TextUtils.isEmpty(ownerCnpj)) return ownerCnpj;
        String c = residue.getCnpj();
        if (TextUtils.isEmpty(c)) c = cnpjFallback;
        return sanitize(c);
    }

    private void fetchCompanyOnce() {
        if (companyRequested) return;
        companyRequested = true;
        companyState = LoadState.LOADING;
        String cnpj = effectiveCnpj();
        if (TextUtils.isEmpty(cnpj)) {
            companyState = LoadState.ERROR;
            notifyItemChanged(0);
            probeOwnerCnpj();
            return;
        }
        fetchCompanyWithCnpj(cnpj, true);
    }

    private void fetchCompanyWithCnpj(String cnpj, boolean allowProbe) {
        companyCall = mongoService.getCompanyByCnpj(cnpj);
        companyCall.enqueue(new Callback<Company>() {
            @Override public void onResponse(@NonNull Call<Company> call, @NonNull Response<Company> resp) {
                if (resp.isSuccessful() && resp.body() != null && notEmpty(resp.body().getNome())) {
                    company = resp.body();
                    companyState = LoadState.SUCCESS;
                    notifyItemChanged(0);
                } else {
                    company = null;
                    companyState = LoadState.ERROR;
                    notifyItemChanged(0);
                    if (allowProbe) probeOwnerCnpj();
                }
            }
            @Override public void onFailure(@NonNull Call<Company> call, @NonNull Throwable t) {
                company = null;
                companyState = LoadState.ERROR;
                notifyItemChanged(0);
                if (allowProbe) probeOwnerCnpj();
            }
        });
    }

    private void fetchAddressOnce() {
        if (addressRequested) return;
        addressRequested = true;
        addressState = LoadState.LOADING;
        String cnpj = effectiveCnpj();
        String enderecoId = residue.getIdEndereco();
        if (TextUtils.isEmpty(cnpj) || TextUtils.isEmpty(enderecoId)) {
            addressState = LoadState.ERROR;
            notifyItemChanged(0);
            return;
        }
        fetchAddressWithCnpj(cnpj);
    }

    private void fetchAddressWithCnpj(String cnpj) {
        addressCall = mongoService.getAdressById(cnpj, residue.getIdEndereco());
        addressCall.enqueue(new Callback<Address>() {
            @Override public void onResponse(@NonNull Call<Address> call, @NonNull Response<Address> resp) {
                if (resp.isSuccessful() && resp.body() != null && notEmpty(resp.body().getNome())) {
                    address = resp.body();
                    addressState = LoadState.SUCCESS;
                } else {
                    address = null;
                    addressState = LoadState.ERROR;
                }
                notifyItemChanged(0);
            }
            @Override public void onFailure(@NonNull Call<Address> call, @NonNull Throwable t) {
                address = null;
                addressState = LoadState.ERROR;
                notifyItemChanged(0);
            }
        });
    }

    private void probeOwnerCnpj() {
        if (probing) return;
        probing = true;
        mongoService.getAllCompanies().enqueue(new Callback<List<Company>>() {
            @Override public void onResponse(@NonNull Call<List<Company>> call, @NonNull Response<List<Company>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) return;
                List<Company> companies = resp.body();
                attemptFindResidueOwner(companies, 0);
            }
            @Override public void onFailure(@NonNull Call<List<Company>> call, @NonNull Throwable t) { }
        });
    }

    private void attemptFindResidueOwner(List<Company> companies, int idx) {
        if (idx >= companies.size()) return;
        String cnpjTry = sanitize(companies.get(idx).getCnpj());
        mongoService.getResidueById(cnpjTry, residue.getId()).enqueue(new Callback<Residue>() {
            @Override public void onResponse(@NonNull Call<Residue> call, @NonNull Response<Residue> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    ownerCnpj = cnpjTry;
                    fetchCompanyWithCnpj(ownerCnpj, false);
                    fetchAddressWithCnpj(ownerCnpj);
                } else {
                    attemptFindResidueOwner(companies, idx + 1);
                }
            }
            @Override public void onFailure(@NonNull Call<Residue> call, @NonNull Throwable t) {
                attemptFindResidueOwner(companies, idx + 1);
            }
        });
    }

    private void startWatchdogs() {
        handler.postDelayed(() -> {
            if (companyState == LoadState.LOADING) {
                if (companyCall != null) companyCall.cancel();
                companyState = LoadState.ERROR;
                notifyItemChanged(0);
                probeOwnerCnpj();
            }
        }, TIMEOUT_MS);
        handler.postDelayed(() -> {
            if (addressState == LoadState.LOADING) {
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
        List<String> imgs = isEmpty(residue.getUrlFoto())
                ? Collections.emptyList()
                : Arrays.asList(residue.getUrlFoto());
        h.viewPager.setAdapter(new ImagePagerAdapter(imgs));

        DecimalFormat df = new DecimalFormat("#,##0.00");

        h.residueName.setText(nvl(residue.getNome()));
        h.residuePrice.setText("R$ " + df.format(residue.getPreco()));
        h.residueDescription.setText(nvl(residue.getDescricao()));
        h.residueWeight.setText(df.format(residue.getPeso()));
        h.residueUnitType.setText(nvl(residue.getTipoUnidade()));

        String companyName =
                companyState == LoadState.SUCCESS ? nvl(company != null ? company.getNome() : "") :
                        companyState == LoadState.ERROR ? "Empresa não encontrada" :
                                "Carregando empresa...";
        h.companyName.setText(companyName);

        String companyPhotoUrl = (companyState == LoadState.SUCCESS && company != null)
                ? company.getUrlFoto() : null;

        Glide.with(h.companyPhoto.getContext())
                .load(companyPhotoUrl)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(h.companyPhoto);

        String addressTxt =
                addressState == LoadState.SUCCESS ? nvl(address != null ? address.getNome() : "") :
                        addressState == LoadState.ERROR ? "Endereço não encontrado" :
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

