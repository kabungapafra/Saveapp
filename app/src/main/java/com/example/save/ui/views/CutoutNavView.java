package com.example.save.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CutoutNavView extends FrameLayout {
    private Paint clearPaint;
    private float cutoutRadius;

    public CutoutNavView(Context context) {
        super(context);
        init();
    }

    public CutoutNavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CutoutNavView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clearPaint.setColor(Color.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        // The outer ring is 72dp, we want the cutout to be exactly that 72dp size 
        // to erase the nav background behind the transparent dashed ring!
        // Increased to 42dp to guarantee the hole swallows the entire gap and stroked ring area
        cutoutRadius = 42 * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        float cx = getWidth() / 2f;
        // The dashed ring overlaps the top edge. 
        // It's placed with marginTop="-16dp". 
        // Nav container is 72dp high. 
        // Center of the 84dp navAction container is at -16dp + 42dp = 26dp from the top.
        // Wait, NO!
        // navAction container is 84dp high. top to top.
        // It has marginTop="-16dp".
        // Top of navAction = -16dp.
        // Center of navAction = -16dp + 42dp = 26dp.
        // So the center of our cutout circle (Y axis) MUST be exactly 26dp from the top of this view!
        float cy = 26 * getResources().getDisplayMetrics().density;
        
        canvas.drawCircle(cx, cy, cutoutRadius, clearPaint);
    }
}
