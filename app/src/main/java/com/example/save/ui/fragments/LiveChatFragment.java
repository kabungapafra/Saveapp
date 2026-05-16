package com.example.save.ui.fragments;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.save.R;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;
import com.example.save.utils.ThemeUtils;

public class LiveChatFragment extends Fragment {

    private WebView webView;
    private View waitingRoomOverlay;
    private View outerRing, midRing, dot1, dot2, dot3;
    private TextView tvHeading;
    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean isNavigatingToNextScreen = false;
    private int dotState = 0;

    private View btnEndChat;

    public static LiveChatFragment newInstance() {
        return new LiveChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_chat, container, false);
        webView = view.findViewById(R.id.webView);
        waitingRoomOverlay = view.findViewById(R.id.waitingRoomOverlay);
        btnEndChat = view.findViewById(R.id.btnEndChat);
        
        // Find views within the included original layout
        outerRing = waitingRoomOverlay.findViewById(R.id.outerRing);
        midRing = waitingRoomOverlay.findViewById(R.id.midRing);
        dot1 = waitingRoomOverlay.findViewById(R.id.dot1);
        dot2 = waitingRoomOverlay.findViewById(R.id.dot2);
        dot3 = waitingRoomOverlay.findViewById(R.id.dot3);
        tvHeading = waitingRoomOverlay.findViewById(R.id.tvHeading);

        setupWebView();
        startWaitingAnimations();
        
        waitingRoomOverlay.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        btnEndChat.setOnClickListener(v -> {
            webView.evaluateJavascript("Tawk_API.endChat();", null);
            if (getActivity() != null) getActivity().onBackPressed();
        });

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectTawkListener();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("https://tawk.to/chat/6a085942484e691c3892af02/1joo9pcb6");
    }

    private void injectTawkListener() {
        // 1. Detect if chat is ALREADY ongoing to skip waiting room
        // 2. Listen for agent joining
        String js = "var Tawk_API = Tawk_API || {}; " +
                   "Tawk_API.onLoad = function(){ " +
                   "  if(Tawk_API.isChatOngoing()){ " +
                   "    Android.onAgentJoined(); " +
                   "  } " +
                   "}; " +
                   "Tawk_API.onAgentJoinChat = function(data){ " +
                   "  Android.onAgentJoined(); " +
                   "}; " +
                   "Tawk_API.onChatMaximized = function(){ " +
                   "  Android.onAgentJoined(); " + 
                   "};";
        webView.evaluateJavascript(js, null);
    }

    private void startWaitingAnimations() {
        // Rotate outer ring
        android.animation.ObjectAnimator rotate = android.animation.ObjectAnimator.ofFloat(outerRing, "rotation", 0f, 360f);
        rotate.setDuration(10000);
        rotate.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        rotate.start();

        // Pulse mid ring
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(midRing, "scaleX", 1f, 1.1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(midRing, "scaleY", 1f, 1.1f);
        scaleX.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        scaleX.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        scaleY.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        scaleY.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.start();
        scaleY.start();

        // Dots wave
        handler.post(new Runnable() {
            @Override
            public void run() {
                int dark = R.drawable.bg_dot_dark;
                int mid = R.drawable.bg_dot_mid;
                int pale = R.drawable.bg_dot_pale;

                if (dotState == 0) {
                    dot1.setBackgroundResource(dark);
                    dot2.setBackgroundResource(mid);
                    dot3.setBackgroundResource(pale);
                } else if (dotState == 1) {
                    dot1.setBackgroundResource(mid);
                    dot2.setBackgroundResource(dark);
                    dot3.setBackgroundResource(pale);
                } else if (dotState == 2) {
                    dot1.setBackgroundResource(pale);
                    dot2.setBackgroundResource(dark);
                    dot3.setBackgroundResource(mid);
                } else {
                    dot1.setBackgroundResource(pale);
                    dot2.setBackgroundResource(mid);
                    dot3.setBackgroundResource(dark);
                }
                dotState = (dotState + 1) % 4;
                handler.postDelayed(this, 600);
            }
        });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onAgentJoined() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (waitingRoomOverlay != null && waitingRoomOverlay.getVisibility() == View.VISIBLE) {
                        waitingRoomOverlay.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                            waitingRoomOverlay.setVisibility(View.GONE);
                            webView.setVisibility(View.VISIBLE);
                            btnEndChat.setVisibility(View.VISIBLE);
                            webView.setAlpha(0f);
                            btnEndChat.setAlpha(0f);
                            webView.animate().alpha(1f).setDuration(500).start();
                            btnEndChat.animate().alpha(1f).setDuration(500).start();
                        }).start();
                    }
                });
            }
        }

        @JavascriptInterface
        public void navigateBack() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> getActivity().onBackPressed());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).setBottomNavVisible(false);
            } else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(false);
                ((MemberMainActivity) getActivity()).setHeaderVisible();
            }
            // Immersive
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.evaluateJavascript("Tawk_API.endChat();", null);
        }
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}
