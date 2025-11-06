package com.purpura.app.ui.screens.errors;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.ui.screens.MainActivity;
import com.purpura.app.ui.screens.autentication.RegisterOrLogin;

public class GenericError extends AppCompatActivity {

    Methods methods = new Methods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generic_error);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button tryAgain = findViewById(R.id.tryAgainErrorButton);
        tryAgain.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() != null){

                finish();
            } else {
                methods.openScreenActivity(this, RegisterOrLogin.class);
            }
        });
    }
}