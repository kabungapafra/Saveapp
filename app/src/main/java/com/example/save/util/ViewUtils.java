package com.example.save.util;

import android.view.View;
import androidx.annotation.DrawableRes;

public class ViewUtils {
    /**
     * Safely set a background resource on a view. If the provided resource id is 0 (invalid),
     * the call is ignored to avoid "Invalid resource ID 0x00000000" errors.
     */
    public static void safeSetBackground(@DrawableRes int resId, View view) {
        if (view == null) return;
        if (resId != 0) {
            view.setBackgroundResource(resId);
        }
    }
}
