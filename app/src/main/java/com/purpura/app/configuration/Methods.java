package com.purpura.app.configuration;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.purpura.app.R;
import com.purpura.app.model.micro.QrCode;
import com.purpura.app.remote.service.MongoService;
import com.purpura.app.ui.screens.accountFeatures.FirstFragment;

import java.text.NumberFormat;
import java.util.Locale;

public class Methods implements TextWatcher {
    MongoService mongoService;
    private EditText editText;
    private String current = "";

    public static boolean hasInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
    public void openActivityToMongoService(Context context, Class<?> nextScreen){
        Intent route = new Intent(context, nextScreen);
        context.startActivity(route);
    }

    public void openScreenActivity(Activity actualScreen, Class<?> nextScreen) {
        Intent route = new Intent(actualScreen, nextScreen);
        actualScreen.startActivity(route);
    }

    public void openScreenFirstFragment(FirstFragment actualScreen, Class<?> nextScreen) {
        Intent route = new Intent(actualScreen.getContext(), nextScreen);
        actualScreen.startActivity(route);
    }

    public void openScreenFragments(Fragment actualScreen, Class<?> nextScreen) {
        if (actualScreen.getContext() != null) {
            Intent route = new Intent(actualScreen.getContext(), nextScreen);
            actualScreen.startActivity(route);
        }
    }

    public void openScreenActivityWithBundle(Activity actualScreen, Class<?> nextScreen, Bundle bundle) {
        Intent route = new Intent(actualScreen, nextScreen);
        route.putExtras(bundle);
        actualScreen.startActivity(route);
    }

    public void openConfirmationPopUp(Context context, Runnable confirmAction, Runnable cancelAction) {
        View popupView = LayoutInflater.from(context)
                .inflate(R.layout.confirm_alterations_card, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(popupView)
                .create();

        popupView.findViewById(R.id.popUpConfirmAlterations).setOnClickListener(v -> {
            confirmAction.run();
            dialog.dismiss();
        });

        if (cancelAction == null){
            popupView.findViewById(R.id.popUpCancelAlterations).setVisibility(View.GONE);
        } else {
            popupView.findViewById(R.id.popUpCancelAlterations).setOnClickListener(v -> {
                cancelAction.run();
                dialog.dismiss();
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.85),
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        dialog.show();
    }


    public static void copyText(Context context, String texto) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("texto", texto);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "Copiado para a área de transferência!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(current)) {
            editText.removeTextChangedListener(this);

            String cleanString = s.toString().replaceAll("[R$,.]", "");
            double parsed = cleanString.isEmpty() ? 0 : Double.parseDouble(cleanString) / 100;
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            String formatted = formatter.format(parsed);

            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());

            editText.addTextChangedListener(this);
        }
    }

    public static QrCode generateQR(String key) {
        return new QrCode.Builder()
                .key(key)
                .foregroundHex("#000000")
                .backgroundHex("#FFFFFF")
                .size(200)
                .build();
    }

}
