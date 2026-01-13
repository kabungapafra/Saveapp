package com.example.save.ui.utils;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * A simplified TextWatcher that provides empty implementations for
 * beforeTextChanged and afterTextChanged.
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
