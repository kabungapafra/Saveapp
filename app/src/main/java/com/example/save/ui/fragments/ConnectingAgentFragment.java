package com.example.save.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectingAgentFragment extends Fragment {

    private View dot1, dot2, dot3;
    private TextView tvHeading, tvBodyText;
    private LinearLayout btnCancel;
    private View outerRing, midRing;
    private RelativeLayout badgePeach, badgeLavender;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int dotStateIndex = 0;
    private int ellipsisCount = 0;
    private boolean requestSent = false;
    private boolean navigating = false;

    // Poll every 4 seconds while waiting for admin acceptance
    private static final long POLL_INTERVAL_MS = 4000;

    private final Runnable pollRunnable = this::pollChatStatus;

    public static ConnectingAgentFragment newInstance() {
        return new ConnectingAgentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connecting_agent, container, false);
        initViews(view);
        setupRichText();
        setupAnimations();
        setupListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            if (getActivity() instanceof AdminMainActivity)
                ((AdminMainActivity) getActivity()).setBottomNavVisible(false);
            else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(false);
                ((MemberMainActivity) getActivity()).setHeaderVisible();
            }
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        if (!requestSent) {
            requestSent = true;
            sendChatRequest();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollRunnable);
        if (getActivity() != null)
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    // ── Backend interaction ────────────────────────────────────────────────────

    private void sendChatRequest() {
        if (getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .requestChat().enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            // Request sent — start polling for admin response
                            handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                        } else {
                            showUnavailable("Couldn't reach support. Please try again.");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded()) showUnavailable("No internet connection. Please try again.");
                    }
                });
    }

    private void pollChatStatus() {
        if (!isAdded() || navigating || getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .getChatStatus().enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || navigating) return;
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                                String status = obj.optString("status", "pending");
                                handleStatusUpdate(status);
                                return;
                            }
                        } catch (Exception ignored) {}
                        // On parse error, keep polling
                        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded()) handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                    }
                });
    }

    private void handleStatusUpdate(String status) {
        if (!isAdded() || getActivity() == null) return;
        switch (status) {
            case "active":
            case "open":
                navigating = true;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow,
                                    R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow)
                            .replace(R.id.fragment_container, LiveChatFragment.newInstance())
                            .commit();
                });
                break;
            case "closed":
                getActivity().runOnUiThread(() ->
                        showUnavailable("The admin is currently unavailable. Please try again later."));
                break;
            default:
                // Still pending — keep polling
                handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                break;
        }
    }

    private void showUnavailable(String message) {
        if (!isAdded() || tvBodyText == null || tvHeading == null) return;
        handler.removeCallbacks(pollRunnable);
        tvHeading.setText("Unavailable");
        tvBodyText.setText(message);
        if (btnCancel != null) {
            TextView cancelLabel = btnCancel.findViewWithTag("cancel_label");
            if (cancelLabel != null) cancelLabel.setText("Go Back");
        }
    }

    // ── UI setup (unchanged from original) ────────────────────────────────────

    private void initViews(View view) {
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
        tvHeading = view.findViewById(R.id.tvHeading);
        tvBodyText = view.findViewById(R.id.tvBodyText);
        btnCancel = view.findViewById(R.id.btnCancel);
        outerRing = view.findViewById(R.id.outerRing);
        midRing = view.findViewById(R.id.midRing);
        badgePeach = view.findViewById(R.id.badgePeach);
        badgeLavender = view.findViewById(R.id.badgeLavender);
    }

    private void setupRichText() {
        String bodyTextHtml = "Our average wait time is <b><font color='#2563EB'>less than 2 minutes</font></b>.<br/>Please stay on this screen to keep your position in the queue.";
        tvBodyText.setText(Html.fromHtml(bodyTextHtml, Html.FROM_HTML_MODE_COMPACT));
    }

    private void setupAnimations() {
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(outerRing, "rotation", 0f, 360f);
        rotateAnim.setDuration(12000);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.start();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(midRing, "scaleX", 1f, 1.25f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(midRing, "scaleY", 1f, 1.25f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(midRing, "alpha", 1f, 0f);
        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, alpha);
        pulseSet.setDuration(2000);
        pulseSet.setInterpolator(new DecelerateInterpolator());
        pulseSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                midRing.setScaleX(1f);
                midRing.setScaleY(1f);
                midRing.setAlpha(1f);
                pulseSet.start();
            }
        });
        pulseSet.start();

        handler.post(dotWaveRunnable);
        startFloatAnimation(badgePeach, 0);
        startFloatAnimation(badgeLavender, 1250);
        handler.post(ellipsisRunnable);
    }

    private void startFloatAnimation(View view, long delay) {
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(view, "translationY", 0f, -15f, 0f);
        floatAnim.setDuration(2500);
        floatAnim.setStartDelay(delay);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.setRepeatMode(ValueAnimator.RESTART);
        floatAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        floatAnim.start();
    }

    private final Runnable dotWaveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) return;
            int dark = R.drawable.bg_dot_dark, mid = R.drawable.bg_dot_mid, pale = R.drawable.bg_dot_pale;
            if (dotStateIndex == 0) { dot1.setBackgroundResource(dark); dot2.setBackgroundResource(mid); dot3.setBackgroundResource(pale); }
            else if (dotStateIndex == 1) { dot1.setBackgroundResource(mid); dot2.setBackgroundResource(dark); dot3.setBackgroundResource(pale); }
            else if (dotStateIndex == 2) { dot1.setBackgroundResource(pale); dot2.setBackgroundResource(dark); dot3.setBackgroundResource(mid); }
            else { dot1.setBackgroundResource(pale); dot2.setBackgroundResource(mid); dot3.setBackgroundResource(dark); }
            dotStateIndex = (dotStateIndex + 1) % 4;
            handler.postDelayed(this, 600);
        }
    };

    private final Runnable ellipsisRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || tvHeading == null) return;
            String current = tvHeading.getText().toString();
            // Don't overwrite the "Unavailable" state
            if (current.startsWith("Connecting") || current.isEmpty()) {
                ellipsisCount = (ellipsisCount + 1) % 4;
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < ellipsisCount; i++) dots.append(".");
                tvHeading.setText("Connecting you to an agent" + dots);
            }
            handler.postDelayed(this, 500);
        }
    };

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> {
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() -> {
                ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX", 0, -10, 10, -10, 10, 0);
                shake.setDuration(300);
                shake.start();
                v.postDelayed(() -> {
                    if (!isAdded()) return;
                    handler.removeCallbacks(pollRunnable);
                    // Cancel the pending request on the backend
                    if (getContext() != null) {
                        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                                .endChatMember().enqueue(new Callback<okhttp3.ResponseBody>() {
                                    @Override public void onResponse(@NonNull Call<okhttp3.ResponseBody> c, @NonNull Response<okhttp3.ResponseBody> r) {}
                                    @Override public void onFailure(@NonNull Call<okhttp3.ResponseBody> c, @NonNull Throwable t) {}
                                });
                    }
                    requireActivity().onBackPressed();
                }, 400);
            }).start();
        });

        btnCancel.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.99f); v.setScaleY(0.99f);
                    v.setBackgroundResource(R.drawable.bg_cancel_button);
                    v.setAlpha(0.8f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1f); v.setScaleY(1f); v.setAlpha(1f);
                    break;
            }
            return false;
        });
    }
}
