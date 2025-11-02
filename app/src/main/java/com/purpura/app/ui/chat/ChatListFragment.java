package com.purpura.app.ui.chat;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.EnvironmentVariables;
import com.purpura.app.configuration.Methods;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.errors.GenericError;

public class ChatListFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    String hash;
    private WebView chatListWebView;
    private Methods methods = new Methods();
    private MongoService mongoService = new MongoService();

    public ChatListFragment() {
    }

    public static ChatListFragment newInstance(String param1, String param2) {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatListWebView = view.findViewById(R.id.chatListWebView);
        chatListWebView.getSettings().setJavaScriptEnabled(true);
        chatListWebView.setWebViewClient(new WebViewClient());

        try{

            FirebaseFirestore.getInstance()
                    .collection("empresa")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String cnpj = document.getString("cnpj");

                            String url = "https://purpura-react-site.vercel.app/#cnpj=" + cnpj;

                            WebSettings webSettings = chatListWebView.getSettings();

                            webSettings.setJavaScriptEnabled(true);

                            webSettings.setDomStorageEnabled(true);

                            chatListWebView.setWebViewClient(new WebViewClient());

                            chatListWebView.loadUrl(url);
                        }
                    }).addOnFailureListener(v -> methods.openScreenFragments(ChatListFragment.this, GenericError.class));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
