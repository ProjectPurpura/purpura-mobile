package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.purpura.app.R;
import com.purpura.app.model.mongo.ChatRequest;
import com.purpura.app.model.mongo.ChatResponse;
import com.purpura.app.remote.service.MongoService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatIndividual extends AppCompatActivity {

    MongoService mongoService = new MongoService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_individual);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WebView webView = findViewById(R.id.individualChatWebView);

        Bundle env = new Bundle();
        String part1 = env.getString("sellerId");
        String part2 = env.getString("buyerId");

        List<String> participants = new ArrayList<>();
        participants.add(part1);
        participants.add(part2);
        ChatRequest request = new ChatRequest(participants);

        mongoService.createChat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {

            }
        });

    }
}