package com.example.save.ui.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Utility class for common OTP input operations like auto-focus and backspace
 * handling.
 */
public class OtpUtils {

    /**
     * TextWatcher to move focus to the next EditText when a digit is entered.
     */
    public static class OtpTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText nextView;

        public OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }
    }

    /**
     * Setup backspace handler to move focus to the previous EditText when current
     * is empty.
     */
    public static void setupBackspaceHandler(EditText current, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty() && previous != null) {
                    previous.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }
}
