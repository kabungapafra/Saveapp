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
        
        outerRing = view.findViewById(R.id.outerRingOverlay);
        midRing = view.findViewById(R.id.midRingOverlay);
        dot1 = view.findViewById(R.id.dot1Overlay);
        dot2 = view.findViewById(R.id.dot2Overlay);
        dot3 = view.findViewById(R.id.dot3Overlay);
        tvHeading = view.findViewById(R.id.tvHeadingOverlay);

        setupWebView();
        startWaitingAnimations();
        
        view.findViewById(R.id.btnCancelOverlay).setOnClickListener(v -> {
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
                // Inject Tawk.to API listener
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
        // This JS snippet tells Tawk.to to notify our Android interface when an agent joins
        String js = "var Tawk_API = Tawk_API || {}; " +
                   "Tawk_API.onAgentJoinChat = function(data){ " +
                   "  Android.onAgentJoined(); " +
                   "}; " +
                   "Tawk_API.onChatMaximized = function(){ " +
                   "  Android.onAgentJoined(); " + // Also trigger if chat opens directly
                   "};";
        webView.evaluateJavascript(js, null);
    }

    private void startWaitingAnimations() {
        // Rotate outer ring
        android.animation.ObjectAnimator.ofFloat(outerRing, "rotation", 0f, 360f)
                .setDuration(10000)
                .setRepeatCount(android.animation.ValueAnimator.INFINITE)
                .start();

        // Pulse mid ring
        android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(midRing, "scaleX", 1f, 1.1f);
        android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(midRing, "scaleY", 1f, 1.1f);
        scaleX.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        scaleX.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        scaleY.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        scaleY.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        scaleX.setDuration(2000).start();
        scaleY.setDuration(2000).start();

        // Dots wave
        handler.post(new Runnable() {
            @Override
            public void run() {
                dot1.setAlpha(dotState == 0 ? 1f : 0.3f);
                dot2.setAlpha(dotState == 1 ? 1f : 0.3f);
                dot3.setAlpha(dotState == 2 ? 1f : 0.3f);
                dotState = (dotState + 1) % 3;
                handler.postDelayed(this, 500);
            }
        });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onAgentJoined() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (waitingRoomOverlay != null) {
                        waitingRoomOverlay.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                            waitingRoomOverlay.setVisibility(View.GONE);
                            webView.setVisibility(View.VISIBLE);
                            webView.setAlpha(0f);
                            webView.animate().alpha(1f).setDuration(500).start();
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
