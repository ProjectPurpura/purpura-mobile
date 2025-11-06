package com.purpura.app.ui.screens;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.ui.screens.autentication.RegisterOrLogin;


public class SplashScreen extends AppCompatActivity {
    Methods methods = new Methods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(this::abrirTela, 4000);
    }

    private void abrirTela() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            methods.openScreenActivity(this, RegisterOrLogin.class);
        }else{
            methods.openScreenActivity(this, MainActivity.class);
        }
        finish();
    }


}
