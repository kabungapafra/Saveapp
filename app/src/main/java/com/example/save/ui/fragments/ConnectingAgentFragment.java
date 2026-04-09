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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;

public class ConnectingAgentFragment extends Fragment {

    private View dot1, dot2, dot3;
    private TextView tvHeading, tvBodyText;
    private LinearLayout btnCancel;
    private View outerRing, midRing;
    private RelativeLayout badgePeach, badgeLavender;

    private Handler animationHandler = new Handler(Looper.getMainLooper());
    private int dotStateIndex = 0;
    private int ellipsisCount = 0;
    private Runnable connectTimeoutRunnable = this::navigateToLiveChat;
    private boolean isNavigatingToLiveChat = false;

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
        // 1. Entrance Animations
        // We'll use simple translate/alpha animations via view.animate() or
        // ObjectAnimator

        // 2. Slow Rotate Outer Ring (12s linear)
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(outerRing, "rotation", 0f, 360f);
        rotateAnim.setDuration(12000);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.start();

        // 3. Pulse Mid Ring
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(midRing, "scaleX", 1f, 1.25f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(midRing, "scaleY", 1f, 1.25f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(midRing, "alpha", 1f, 0f);

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, alpha);
        pulseSet.setDuration(2000);
        pulseSet.setInterpolator(new DecelerateInterpolator());

        // Loop pulse
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

        // 4. Dot Wave Animation (every 600ms)
        animationHandler.post(dotWaveRunnable);

        // 5. Floating Badges
        startFloatAnimation(badgePeach, 0);
        startFloatAnimation(badgeLavender, 1250); // Offset phase

        // 6. Ellipsis Animation
        animationHandler.post(ellipsisRunnable);

        // 7. Timeout to connect
        animationHandler.postDelayed(connectTimeoutRunnable, 3500);
    }

    private void navigateToLiveChat() {
        if (!isAdded() || getActivity() == null)
            return;

        isNavigatingToLiveChat = true;

        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow,
                        R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow)
                .replace(R.id.fragment_container, LiveChatFragment.newInstance())
                .commit();
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

    private Runnable dotWaveRunnable = new Runnable() {
        @Override
        public void run() {
            int dark = R.drawable.bg_dot_dark;
            int mid = R.drawable.bg_dot_mid;
            int pale = R.drawable.bg_dot_pale;

            if (dotStateIndex == 0) {
                dot1.setBackgroundResource(dark);
                dot2.setBackgroundResource(mid);
                dot3.setBackgroundResource(pale);
            } else if (dotStateIndex == 1) {
                dot1.setBackgroundResource(mid);
                dot2.setBackgroundResource(dark);
                dot3.setBackgroundResource(pale);
            } else if (dotStateIndex == 2) {
                dot1.setBackgroundResource(pale);
                dot2.setBackgroundResource(dark);
                dot3.setBackgroundResource(mid);
            } else if (dotStateIndex == 3) {
                dot1.setBackgroundResource(pale);
                dot2.setBackgroundResource(mid);
                dot3.setBackgroundResource(dark);
            }

            dotStateIndex = (dotStateIndex + 1) % 4;
            animationHandler.postDelayed(this, 600);
        }
    };

    private Runnable ellipsisRunnable = new Runnable() {
        @Override
        public void run() {
            ellipsisCount = (ellipsisCount + 1) % 4;
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < ellipsisCount; i++) {
                dots.append(".");
            }
            tvHeading.setText("Connecting you to an agent" + dots.toString());
            animationHandler.postDelayed(this, 500);
        }
    };

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> {
            // Apply shake effect
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() -> {
                ObjectAnimator shake = ObjectAnimator.ofFloat(v, "translationX", 0, -10, 10, -10, 10, 0);
                shake.setDuration(300);
                shake.start();

                // Navigate back after shake
                v.postDelayed(() -> {
                    if (isAdded()) {
                        requireActivity().onBackPressed();
                    }
                }, 400);
            }).start();
        });

        // Touch listener for scale effect on button down
        btnCancel.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setScaleX(0.99f);
                    v.setScaleY(0.99f);
                    v.setBackgroundResource(R.drawable.bg_cancel_button); // We could use a selector here, but doing it
                                                                          // in code for precision
                    v.setAlpha(0.8f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    v.setAlpha(1f);
                    break;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.navContainer);
            if (nav != null) nav.setVisibility(View.GONE);

            View fab = getActivity().findViewById(R.id.fabAction);
            if (fab != null) fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        animationHandler.removeCallbacksAndMessages(null);
        if (getActivity() != null && !isNavigatingToLiveChat) {
            View nav = getActivity().findViewById(R.id.navContainer);
            if (nav != null) nav.setVisibility(View.VISIBLE);

            View fab = getActivity().findViewById(R.id.fabAction);
            if (fab != null) fab.setVisibility(View.VISIBLE);
        }
    }
}
