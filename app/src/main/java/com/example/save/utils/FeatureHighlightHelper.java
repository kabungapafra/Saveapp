package com.example.save.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.save.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FeatureHighlightHelper {

    private static final String PREF_NAME = "FeatureHighlights";
    private static final String KEY_HIGHLIGHTS_SHOWN = "highlights_shown";

    private final Activity activity;
    private final SharedPreferences prefs;
    private final List<HighlightStep> steps;
    private int currentStep = 0;
    private PopupWindow currentPopup;
    private View overlayView;

    public static class HighlightStep {
        public View targetView;
        public String title;
        public String description;

        public HighlightStep(View targetView, String title, String description) {
            this.targetView = targetView;
            this.title = title;
            this.description = description;
        }
    }

    public FeatureHighlightHelper(Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.steps = new ArrayList<>();
    }

    public void addStep(View targetView, String title, String description) {
        steps.add(new HighlightStep(targetView, title, description));
    }

    public void start() {
        if (hasShownHighlights()) {
            return; // Already shown
        }

        if (steps.isEmpty()) {
            return;
        }

        currentStep = 0;
        showStep(currentStep);
    }

    private void showStep(int step) {
        if (step >= steps.size()) {
            finish();
            return;
        }

        HighlightStep highlightStep = steps.get(step);

        // Create overlay
        createOverlay(highlightStep.targetView);

        // Show tooltip
        showTooltip(highlightStep);
    }

    private void createOverlay(View targetView) {
        // Create semi-transparent overlay
        overlayView = new View(activity);
        overlayView.setBackgroundColor(Color.argb(180, 0, 0, 0));
        overlayView.setClickable(true);

        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(overlayView, params);

        // Highlight target view (simplified - would need more complex drawing)
        targetView.bringToFront();
    }

    private void showTooltip(HighlightStep step) {
        View tooltipView = LayoutInflater.from(activity).inflate(R.layout.layout_feature_tooltip, null);

        TextView tvTitle = tooltipView.findViewById(R.id.tvTooltipTitle);
        TextView tvDescription = tooltipView.findViewById(R.id.tvTooltipDescription);
        MaterialButton btnNext = tooltipView.findViewById(R.id.btnTooltipNext);
        MaterialButton btnSkip = tooltipView.findViewById(R.id.btnTooltipSkip);

        tvTitle.setText(step.title);
        tvDescription.setText(step.description);

        if (currentStep == steps.size() - 1) {
            btnNext.setText("Got it!");
        } else {
            btnNext.setText("Next");
        }

        btnNext.setOnClickListener(v -> {
            dismissCurrentTooltip();
            currentStep++;
            showStep(currentStep);
        });

        btnSkip.setOnClickListener(v -> {
            dismissCurrentTooltip();
            finish();
        });

        // Show popup
        currentPopup = new PopupWindow(tooltipView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Position tooltip below target view
        int[] location = new int[2];
        step.targetView.getLocationOnScreen(location);

        currentPopup.showAtLocation(step.targetView, Gravity.NO_GRAVITY,
                location[0], location[1] + step.targetView.getHeight() + 20);
    }

    private void dismissCurrentTooltip() {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.dismiss();
        }

        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
        }
    }

    private void finish() {
        dismissCurrentTooltip();
        markHighlightsAsShown();
    }

    private boolean hasShownHighlights() {
        return prefs.getBoolean(KEY_HIGHLIGHTS_SHOWN, false);
    }

    private void markHighlightsAsShown() {
        prefs.edit().putBoolean(KEY_HIGHLIGHTS_SHOWN, true).apply();
    }

    public void reset() {
        prefs.edit().putBoolean(KEY_HIGHLIGHTS_SHOWN, false).apply();
    }
}
