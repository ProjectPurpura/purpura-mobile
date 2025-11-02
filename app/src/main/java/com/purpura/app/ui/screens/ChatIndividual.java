package com.purpura.app.ui.screens;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.mongo.ChatRequest;
import com.purpura.app.model.mongo.ChatResponse;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.chat.ChatListFragment;
import com.purpura.app.ui.screens.errors.GenericError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatIndividual extends AppCompatActivity {

    MongoService mongoService = new MongoService();
    Methods methods = new Methods();

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

        Bundle env = getIntent().getExtras();
        String part1 = env.getString("sellerId");
        String part2 = env.getString("buyerId");

        List<String> participants = new ArrayList<>();
        participants.add(part1);
        participants.add(part2);
        ChatRequest request = new ChatRequest(participants);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        System.out.println(request);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        try{
            mongoService.createChat(request).enqueue(new Callback<ChatResponse>() {
                @Override
                public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    System.out.println(response);
                    if(response.isSuccessful()){
                        String url =  "https://purpura-react-site.vercel.app/chat/" + response.body().getId() + "/#cnpj=" + part2;

                        WebSettings webSettings = webView.getSettings();

                        webSettings.setJavaScriptEnabled(true);

                        webSettings.setDomStorageEnabled(true);

                        webView.setWebViewClient(new WebViewClient());

                        webView.loadUrl(url);
                    }
                }

                @Override
                public void onFailure(Call<ChatResponse> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
