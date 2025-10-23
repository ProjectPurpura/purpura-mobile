package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.remote.service.MicroService;
import com.purpura.app.ui.screens.errors.GenericError;

public class QrCodePayment extends AppCompatActivity {

    Methods methods = new Methods();
    MicroService microService = new MicroService();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_code_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.qrCodePaymentBackButton);
        Button continueButton = findViewById(R.id.qrCodePaymentContinueButton);
        ImageView copyButton = findViewById(R.id.qrCodePaymentCopyPasteButton);
        TextView qrCodeTextView = findViewById(R.id.qrCodePaymentPixURL);


        //----- Bundle -----------------//
        String pixKey = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pixKey = extras.getString("pix");
        }
        if (pixKey == null) {
            pixKey = "1234567890876543";
        }
        qrCodeTextView.setText(pixKey);

        loadQr(pixKey);


        //----- SetOnClickListener -----//

        backButton.setOnClickListener(v -> finish());

        continueButton.setOnClickListener(v -> methods.openScreenActivity(this, PaymentStatus.class));

        copyButton.setOnClickListener(v -> {
            String key = qrCodeTextView.getText().toString();
            methods.copyText(this, key);
        });
    }

    private void loadQr(String pixKey) {
        ImageView qrCodeImage = findViewById(R.id.imageView14);

        microService.generateQr(pixKey,
                bytes -> Glide.with(this)
                        .load(bytes)
                        .into(qrCodeImage),
                error ->
                        methods.openScreenActivity(this, GenericError.class)
        );
    }
}
