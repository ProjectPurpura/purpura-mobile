package com.purpura.app.ui.screens;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.ui.account.AccountFragment;
import com.purpura.app.ui.screens.errors.InternetError;

public class Dashboards extends AppCompatActivity {

    Methods methods = new Methods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        WebView webView = findViewById(R.id.dasboardWebView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://app.powerbi.com/view?r=eyJrIjoiMDA5NjFlZGMtYzZjZS00NzY4LWJlM2MtNDRmZmJlNzA1YjA5IiwidCI6ImIxNDhmMTRjLTIzOTctNDAyYy1hYjZhLTFiNDcxMTE3N2FjMCJ9");

        ImageView backButton = findViewById(R.id.dashboardsBackButton);

        backButton.setOnClickListener(v -> finish());
    }
}