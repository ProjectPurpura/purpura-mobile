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
import com.purpura.app.model.postgres.order.OrderItem;
import com.purpura.app.remote.service.MicroService;
import com.purpura.app.remote.service.PostgresService;
import com.purpura.app.ui.screens.errors.GenericError;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrCodePayment extends AppCompatActivity {

    Methods methods = new Methods();

    MicroService microService = new MicroService();
    PostgresService postgresService = new PostgresService();
    Integer orderId = null;

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
        TextView totalTextView = findViewById(R.id.qrCodePaymentSubtotal);

        String pixKey = null;
        Double total = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("total")) {
                Object totalExtra = extras.get("total");
                if (totalExtra instanceof Double) {
                    total = (Double) totalExtra;
                } else if (totalExtra instanceof String) {
                    try { total = Double.parseDouble((String) totalExtra); } catch (Exception ignored) {}
                }
            }
            pixKey = extras.getString("pix");
            orderId = extras.getInt("orderId");
        }

        if (pixKey == null) {
            pixKey = "1234567890876543";
        }
        if (orderId == null) {
            orderId = 400000000;
        }
        qrCodeTextView.setText(pixKey);

        if (total != null) {
            NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            totalTextView.setText(brl.format(total));
        }

        loadQr(pixKey);

        backButton.setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v ->{
            postgresService.concludeOrder(orderId).enqueue(new Callback<OrderItem>() {
                @Override
                public void onResponse(Call<OrderItem> call, Response<OrderItem> response) {
                    methods.openScreenActivity(QrCodePayment.this, MainActivity.class);
                }

                @Override
                public void onFailure(Call<OrderItem> call, Throwable t) {

                }
            });
        });
        copyButton.setOnClickListener(v -> methods.copyText(this, qrCodeTextView.getText().toString()));
    }

    private void loadQr(String pixKey) {
        ImageView qrCodeImage = findViewById(R.id.imageView14);
        microService.generateQr(pixKey,
                bytes -> Glide.with(this).load(bytes).into(qrCodeImage),
                error -> methods.openScreenActivity(this, GenericError.class)
        );
    }
}
