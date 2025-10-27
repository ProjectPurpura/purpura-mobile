package com.purpura.app.configuration;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class CNPJMask implements TextWatcher {
    private boolean isUpdating = false;
    private final TextView textView;

    public CNPJMask(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = s.toString().replaceAll("[^\\d]", "");
        String formatted = "";

        if (isUpdating) {
            isUpdating = false;
            return;
        }

        int length = str.length();
        if (length > 14)
            str = str.substring(0, 14);

        if (length <= 2)
            formatted = str;
        else if (length <= 5)
            formatted = str.substring(0, 2) + "." + str.substring(2);
        else if (length <= 8)
            formatted = str.substring(0, 2) + "." + str.substring(2, 5) + "." + str.substring(5);
        else if (length <= 12)
            formatted = str.substring(0, 2) + "." + str.substring(2, 5) + "." + str.substring(5, 8) + "/" + str.substring(8);
        else
            formatted = str.substring(0, 2) + "." + str.substring(2, 5) + "." + str.substring(5, 8) + "/" + str.substring(8, 12) + "-" + str.substring(12);

        isUpdating = true;
        textView.setText(formatted);
        if (textView instanceof EditText) {
            ((EditText) textView).setSelection(formatted.length());
        }
    }

    @Override
    public void afterTextChanged(Editable s) { }
}
