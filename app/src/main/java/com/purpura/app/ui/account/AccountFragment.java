package com.purpura.app.ui.account;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.configuration.Notifications;
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

    public static final int NOTIFICATION_ID = 1;
    FirebaseAuth objAutenticar = FirebaseAuth.getInstance();
    Methods methods = new Methods();
    private static final int REQUEST_CODE_NOTIFICATION = 1001;
    FirebaseMethods firebaseMethods = new FirebaseMethods();
    MongoService mongoService = new MongoService();
    private FragmentAccountBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        solicitarPermissaoNotificacoes();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // ----- Views ----- //
        ShapeableImageView profileImage = binding.profilePhoto;
        TextView accountProfileText = binding.accountFragmentCompanyName;


        // ----- SetOnClickListeners ----- //

        binding.myPixKeysRow.setOnClickListener(v -> methods.openScreenFragments(this, EditPixKeys.class));

        binding.myAddressesRow.setOnClickListener(v -> methods.openScreenFragments(this, EditAddress.class));

        binding.changePasswordRow.setOnClickListener(v -> showPasswordReset());

        binding.myOrdersRow.setOnClickListener(v -> methods.openScreenFragments(this, MyOrders.class));

        binding.dashboardsRow.setOnClickListener(v -> methods.openScreenFragments(this, Dashboards.class));

        binding.myProductsRow.setOnClickListener(v -> methods.openScreenFragments(this, MyProducts.class));

        binding.editProfileRow.setOnClickListener(v -> methods.openScreenFragments(this, UpdateProfile.class));

        binding.logOutRow.setOnClickListener(v -> {
            methods.openConfirmationPopUp(this.getContext(),
                    () -> logOut(),
                    null);
        });


        Activity activity = this.getActivity();
        FirebaseUser currentUser = objAutenticar.getCurrentUser();

        if (currentUser != null) {
            try {
                String uid = currentUser.getUid();
                FirebaseFirestore.getInstance()
                        .collection("empresa")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                profileImage.setVisibility(INVISIBLE);
                                accountProfileText.setVisibility(INVISIBLE);
                                mongoService.getCompanyByCnpj(cnpj).enqueue(new Callback<Company>() {
                                    @Override
                                    public void onResponse(Call<Company> call, Response<Company> response) {
                                        Company companyResponse = response.body();
                                        if (companyResponse != null) {
                                            profileImage.setVisibility(VISIBLE);
                                            if (activity != null) {
                                                Glide.with(activity)
                                                        .load(companyResponse.getUrlFoto())
                                                        .transform(new CircleCrop())
                                                        .into(profileImage);
                                            }
                                            accountProfileText.setVisibility(VISIBLE);
                                            accountProfileText.setText(companyResponse.getNome());
                                        } else {
                                            Log.e("AccountFragment", "MongoService respondeu, mas o Company veio NULO. CNPJ: " + cnpj);
                                            methods.openScreenFragments(AccountFragment.this, GenericError.class);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Company> call, Throwable t) {
                                        Log.e("AccountFragment", "Falha na chamada do MongoService!", t);
                                        methods.openScreenFragments(AccountFragment.this, GenericError.class);
                                    }
                                });
                            } else {
                                Log.e("AccountFragment", "Documento da empresa não existe no Firestore para o UID: " + uid);
                                methods.openScreenFragments(AccountFragment.this, GenericError.class);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("AccountFragment", "Falha ao buscar documento no Firestore!", e);
                            methods.openScreenFragments(AccountFragment.this, GenericError.class);
                        });
            } catch (Exception e) {
                Log.e("AccountFragment", "Exceção inesperada no bloco try!", e);
                methods.openScreenFragments(AccountFragment.this, GenericError.class);
            }
        } else {
            Toast.makeText(getContext(), "Sua sessão expirou. Faça login novamente.", Toast.LENGTH_LONG).show();
            logOut();
        }

        return root;
    }

    private void solicitarPermissaoNotificacoes() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (getContext() != null &&
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
            }
        }
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
        editTextEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        alert.setView(editTextEmail);

        alert.setPositiveButton("Enviar", (dialog, which) -> {
            String email = editTextEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this.getContext(), "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            new Notifications().changePasswordNotification(this.getActivity(), this.getContext());
                        } else {
                            Toast.makeText(this.getContext(), "Erro ao enviar e-mail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        alert.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        alert.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}