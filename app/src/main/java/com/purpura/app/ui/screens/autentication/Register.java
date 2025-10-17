package com.purpura.app.ui.screens.autentication;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapDecoder;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Limit;
import com.cloudinary.android.preprocess.Rotate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.Company;
import com.purpura.app.remote.cloudnary.Cloudinary;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.MainActivity;
import com.purpura.app.ui.screens.errors.GenericError;
import com.purpura.app.ui.screens.productRegister.RegisterProduct;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    Methods methods = new Methods();
    MongoService mongoService = new MongoService();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    GoogleSignInClient googleSignInClient;
    Cloudinary cloudinary = new Cloudinary();
    private String uploadedImageUrl = null;


    private ActivityResultLauncher<String[]> requestPermission;
    private ActivityResultLauncher<Intent> requestGallery;

    ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    try {
                        GoogleSignInAccount account =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                        .getResult(ApiException.class);

                        if (account == null || account.getEmail() == null) {
                            Toast.makeText(this, "Erro ao obter conta do Google.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String email = account.getEmail();

                        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean existe = !task.getResult().getSignInMethods().isEmpty();

                                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                auth.signInWithCredential(credential).addOnCompleteListener(loginTask -> {
                                    if (loginTask.isSuccessful()) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        if (user != null) {
                                            verificarCNPJTelefone(user.getUid());
                                        }
                                    } else {
                                        Toast.makeText(this, "Erro ao logar com Google.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Erro ao verificar email: " + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (ApiException e) {
                        Toast.makeText(this, "Erro Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        EditText edtEmail = findViewById(R.id.registerEmail);
        EditText edtTelefone = findViewById(R.id.registerPhone);
        EditText edtNome = findViewById(R.id.registerCompanyName);
        EditText edtCNPJ = findViewById(R.id.registerCNPJ);
        EditText edtSenha = findViewById(R.id.registerPassword);
        Button btnCadastrar = findViewById(R.id.registerButton);
        ImageView image = findViewById(R.id.registerImage);
        SignInButton btnGoogle = findViewById(R.id.loginWithGoogle);
        TextView txtLogin = findViewById(R.id.registerLoginText);

        image.setOnClickListener(v -> {
            requestGallery.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        });

        btnCadastrar.setOnClickListener(v -> {
            String nome = edtNome.getText().toString().trim();
            String telefone = edtTelefone.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String cnpj = edtCNPJ.getText().toString().trim();
            String senha = edtSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || telefone.isEmpty() || cnpj.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (uploadedImageUrl == null) {
                Toast.makeText(this, "Aguarde o upload da imagem antes de cadastrar!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cnpj.length() != 14 || !cnpj.matches("\\d+")) {
                Toast.makeText(this, "CNPJ inválido. Deve ter 14 dígitos.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (telefone.length() < 10 || !telefone.matches("\\d+")) {
                Toast.makeText(this, "Telefone inválido. Deve ter no mínimo 10 dígitos.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {

                        Company company = new Company(cnpj, email, uploadedImageUrl, nome, telefone);

                        FirebaseFirestore.getInstance()
                                .collection("empresa")
                                .document(user.getUid())
                                .set(company)
                                .addOnSuccessListener(aVoid -> {
                                    try {
                                        runOnUiThread(() -> {
                                            Log.e("Entrou antes da requisição", "Entrou antes da requisição");
                                            mongoService.createCompany(company, this);
                                            Toast.makeText(this, "Cadastro finalizado!", Toast.LENGTH_SHORT).show();
                                            methods.openScreenActivity(this, MainActivity.class);
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erro ao salvar no Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                } else {
                    Toast.makeText(this, "Erro ao cadastrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("875459757357-fhss1jko4af6m0mj5jl9l65g2njufe0e.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> {
            Intent intent = googleSignInClient.getSignInIntent();
            googleLauncher.launch(intent);
        });

        txtLogin.setOnClickListener(v -> {
            methods.openScreenActivity(this, Login.class);
            finish();
        });

        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario != null) {
            verificarCNPJTelefone(usuario.getUid());
        }


        cloudinary.initCloudinary(this);

        requestGallery =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() == null || result.getData().getData() == null) {
                        return;
                    }

                    if (result.getData() != null) {
                        Uri loadedURI = result.getData().getData();

                        cloudinary.uploadImage(this, loadedURI, new Cloudinary.ImageUploadCallback() {
                            @Override
                            public void onUploadSuccess(String imageUrl) {
                                uploadedImageUrl = imageUrl;
                                Glide.with(Register.this)
                                        .load(imageUrl)
                                        .into(image);
                                image.setTag(imageUrl);
                            }

                            @Override
                            public void onUploadFailure(String error) {
                            }
                        });
                    }
                });

        requestPermission =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        String permission = entry.getKey();
                        Boolean isGranted = entry.getValue();
                        if (isGranted) {
                            Log.d("Permissions", "Permission granted: " + permission);
                        } else {
                        }
                    }
                });

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

    private void verificarCNPJTelefone(String uid) {
        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String cnpj = document.getString("cnpj");
                        String telefone = document.getString("telefone");

                        if (cnpj == null || cnpj.isEmpty() || telefone == null || telefone.isEmpty()) {
                            Toast.makeText(this, "Complete seu cadastro", Toast.LENGTH_SHORT).show();
                            methods.openScreenActivity(this, AddicionalInformacionsRegisterGoogle.class);
                            finish();
                        } else {
                            Toast.makeText(this, "Logado com sucesso", Toast.LENGTH_SHORT).show();
                            methods.openScreenActivity(this, MainActivity.class);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Complete seu cadastro", Toast.LENGTH_SHORT).show();
                        methods.openScreenActivity(this, AddicionalInformacionsRegisterGoogle.class);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao verificar cadastro", Toast.LENGTH_SHORT).show();
                });
    }
}