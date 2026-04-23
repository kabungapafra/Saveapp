package com.example.save.utils;

import android.content.Context;
import android.util.TypedValue;

public class UIUtils {

    /**
     * Converts DP (Density-independent Pixels) to Pixels.
     * @param context Application context
     * @param dp Value in DP
     * @return Value in Pixels
     */
    public static float dpToPx(Context context, float dp) {
        if (context == null) return dp;
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}
