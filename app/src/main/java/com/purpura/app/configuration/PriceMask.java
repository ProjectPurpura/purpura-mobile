package com.purpura.app.configuration;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class PriceMask implements TextWatcher {
    private final TextView textView;
    private boolean isUpdating = false;
    private String oldString = "";

    public PriceMask(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isUpdating) {
            isUpdating = false;
            return;
        }

        String str = s.toString().replaceAll("[^\\d]", "");
        if (str.equals(oldString) || str.isEmpty()) return;

        String formatted;
        try {
            double parsed = Double.parseDouble(str) / 100;
            formatted = String.format(Locale.getDefault(), "%,.2f", parsed);
        } catch (NumberFormatException e) {
            formatted = "";
        }

        isUpdating = true;
        textView.setText(formatted);
        if (textView instanceof EditText) {
            ((EditText) textView).setSelection(formatted.length());
        }
        oldString = str;
    }

    @Override
    public void afterTextChanged(Editable s) { }

    public static class PhoneMask implements TextWatcher {
        private final TextView textView;
        private boolean isUpdating = false;

        public PhoneMask(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isUpdating) {
                isUpdating = false;
                return;
            }
            String str = s.toString().replaceAll("[^\\d]", "");
            String formatted;

            int length = str.length();
            if (length > 11)
                str = str.substring(0, 11);

            if (length <= 2)
                formatted = "(" + str;
            else if (length <= 7)
                formatted = "(" + str.substring(0, 2) + ") " + str.substring(2);
            else
                formatted = "(" + str.substring(0, 2) + ") " + str.substring(2, 7) + "-" + str.substring(7);

            isUpdating = true;
            textView.setText(formatted);
            if (textView instanceof EditText) {
                ((EditText) textView).setSelection(formatted.length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) { }
    }

}
