package com.purpura.app.configuration;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class ZipCodeMask implements TextWatcher {
    private boolean isUpdating = false;
    private final TextView textView;

    public ZipCodeMask(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = s.toString().replaceAll("[^\\d]", "");
        String formatted;

        if (isUpdating) {
            isUpdating = false;
            return;
        }

        if (str.length() > 8) {
            str = str.substring(0, 8);
        }

        if (str.length() > 5) {
            formatted = str.substring(0, 5) + "-" + str.substring(5);
        } else {
            formatted = str;
        }

        isUpdating = true;
        textView.setText(formatted);
        if (textView instanceof EditText) {
            ((EditText) textView).setSelection(formatted.length());
        }
    }

    @Override
    public void afterTextChanged(Editable s) { }
}
