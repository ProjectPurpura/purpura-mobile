package com.purpura.app.ui.screens.errors;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.postgres.order.OrderRequest;
import com.purpura.app.model.postgres.order.OrderResponse;
import com.purpura.app.remote.service.PostgresService;
import com.purpura.app.ui.screens.QrCodePayment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentToPay extends AppCompatActivity {

    PostgresService postgresService = new PostgresService();
    Methods methods = new Methods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_content_to_pay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button cancelOrder = findViewById(R.id.cancelOrdersButton);
        Button payOrder = findViewById(R.id.payNowButton);

        cancelOrder.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if(document.exists()){
                            String cnpj = document.getString("cnpj");

                            postgresService.getOrdersByClient(cnpj).enqueue(new Callback<List<OrderResponse>>() {
                                @Override
                                public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                                    List<OrderResponse> orders = response.body();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                                        postgresService.deleteOrderByOrderId(orders.getFirst().getIdPedido());
                                        Toast.makeText(ContentToPay.this, "Pedido deletado!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                                    Toast.makeText(ContentToPay.this, "Não foi possível deletar seu pedido...", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
        });

        payOrder.setOnClickListener(v -> {
            methods.openScreenActivity(this, QrCodePayment.class);
        });

    }
}