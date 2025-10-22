package com.purpura.app.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.remote.service.MongoService;

import com.purpura.app.ui.screens.Dashboards;
import com.google.firebase.auth.FirebaseAuth;
import com.purpura.app.ui.screens.UpdateProfile;
import com.purpura.app.ui.screens.accountFeatures.MyOrders;
import com.purpura.app.configuration.Methods;
import com.purpura.app.remote.firebase.FirebaseMethods;
import com.purpura.app.databinding.FragmentAccountBinding;
import com.purpura.app.ui.screens.accountFeatures.EditAddress;
import com.purpura.app.ui.screens.accountFeatures.EditPixKeys;
import com.purpura.app.ui.screens.accountFeatures.MyProducts;
import com.purpura.app.ui.screens.autentication.RegisterOrLogin;
import com.purpura.app.ui.screens.errors.GenericError;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    FirebaseAuth objAutenticar = FirebaseAuth.getInstance();
    Methods methods = new Methods();
    FirebaseMethods firebaseMethods = new FirebaseMethods();
    MongoService mongoService = new MongoService();
    private FragmentAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // ----- Views ----- //
        //Imagem da empresa
        ShapeableImageView profileImage = binding.profilePhoto;
        //Informações da empresa
        ImageView accountProfileImage = binding.accountProfileImage;
        TextView accountProfileText = binding.accountFragmentCompanyName;
        //Meus pedidos
        ImageView myOrdersIcon = binding.accountBagIcon;
        TextView myOrdersText = binding.accountBagText;
        //Meus produtos
        ImageView myProductsIcon = binding.accountBagIcon;
        TextView myProductsText = binding.accountProductsText;
        //Meus endereços
        ImageView myAddressesIcon = binding.accountLocationIcon;
        TextView myAddressesText = binding.accountLocationText;
        //Minhas chaves pix
        ImageView editPixKeysIcon = binding.accountPixcon;
        TextView editPixKeys = binding.accountPixText;
        //Ver dashboards
        ImageView dashboardIcon = binding.accountFragmentDashboardIcon;
        TextView dashboardText = binding.accountFragmentDasboardText;
        //Alterar senha
        ImageView changePasswordIcon = binding.accountPasswordIcon;
        TextView changePassword = binding.accountPasswordText;
        //Editar perfil
        ImageView editProfileIcon = binding.accountProfileImage;
        TextView editProfile = binding.accountProfileText;
        //Sair da conta
        ImageView logOutIcon = binding.accountLogOutIcon;
        TextView logOut = binding.accountLogOutText;

        // ----- SetOnClickListeners ----- //

        editPixKeys.setOnClickListener(v -> methods.openScreenFragments(this, EditPixKeys.class));
        editPixKeysIcon.setOnClickListener(v -> methods.openScreenFragments(this, EditPixKeys.class));

        myAddressesIcon.setOnClickListener(v -> methods.openScreenFragments(this, EditAddress.class));
        myAddressesText.setOnClickListener(v -> methods.openScreenFragments(this, EditAddress.class));

        changePasswordIcon.setOnClickListener(v -> showPasswordReset());
        changePassword.setOnClickListener(v -> showPasswordReset());

        myOrdersIcon.setOnClickListener(v -> methods.openScreenFragments(this, MyOrders.class));
        myOrdersText.setOnClickListener(v -> methods.openScreenFragments(this, MyOrders.class));

        dashboardIcon.setOnClickListener(v -> methods.openScreenFragments(this, Dashboards.class));
        dashboardText.setOnClickListener(v -> methods.openScreenFragments(this, Dashboards.class));

        myProductsIcon.setOnClickListener(v -> methods.openScreenFragments(this, MyProducts.class));
        myProductsText.setOnClickListener(v -> methods.openScreenFragments(this, MyProducts.class));

        editProfileIcon.setOnClickListener(v -> methods.openScreenFragments(this, UpdateProfile.class));
        editProfile.setOnClickListener(v -> methods.openScreenFragments(this, UpdateProfile.class));

        Activity activity = this.getActivity();

        try {
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");
                            Call<Company> call = mongoService.getCompanyByCnpj(cnpj);
                            call.enqueue(new Callback<Company>() {
                                @Override
                                public void onResponse(Call<Company> call, Response<Company> response) {
                                    Company companyResponse = response.body();
                                    accountProfileText.setText(companyResponse.getNome());
                                    Glide.with(AccountFragment.this)
                                            .load(companyResponse.getUrlFoto())
                                            .circleCrop()
                                            .into(profileImage);
                                }

                                @Override
                                public void onFailure(Call<Company> call, Throwable t) {
                                    methods.openScreenFragments(AccountFragment.this, GenericError.class);
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            methods.openScreenFragments(AccountFragment.this, GenericError.class);
            throw new RuntimeException(e);
        }

        logOutIcon.setOnClickListener(v -> {
            methods.openConfirmationPopUp(this.getContext(),
                    () -> logOut(),
                    null);
        });
        logOut.setOnClickListener(v -> {
            methods.openConfirmationPopUp(this.getContext(),
                    () -> logOut(),
                    null);
        });

        editProfile.setOnClickListener(v -> methods.openScreenFragments(this, UpdateProfile.class));

        return root;
    }

    private void logOut() {
        firebaseMethods.logout();
        methods.openScreenFragments(this, RegisterOrLogin.class);
    }

    private void showPasswordReset() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle("Esqueci minha senha");
        alert.setMessage("Entre com seu email para redefinir sua senha");

        EditText editTextEmail = new EditText(this.getContext());
        alert.setView(editTextEmail);

        alert.setPositiveButton("Enviar", (dialog, which) -> {
            objAutenticar.sendPasswordResetEmail(editTextEmail.getText().toString());
            Toast.makeText(this.getContext(), "Email Enviado", Toast.LENGTH_LONG).show();
        });
        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}