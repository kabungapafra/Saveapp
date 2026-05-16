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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.save.R;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;
import com.example.save.utils.ThemeUtils;

public class LiveChatFragment extends Fragment {

    private WebView webView;
    private android.widget.ProgressBar loadingSpinner;
    private boolean isNavigatingToNextScreen = false;

    public static LiveChatFragment newInstance() {
        return new LiveChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_chat, container, false);
        webView = view.findViewById(R.id.webView);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        setupWebView();
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        
        // Ensure standard desktop-like behavior for chat
        webSettings.setUserAgentString(webSettings.getUserAgentString().replace("wv", ""));

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (loadingSpinner != null) loadingSpinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (loadingSpinner != null) loadingSpinner.setVisibility(View.GONE);
                applyTheme();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Keep all navigation inside the WebView
                return false;
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("https://tawk.to/chat/6a085942484e691c3892af02/1joo9pcb6");
    }

    private void applyTheme() {
        if (getContext() == null || webView == null) return;
        String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
        boolean isDark = ThemeUtils.isDarkMode(getContext(), role);
        String theme = isDark ? "dark" : "light";
        webView.evaluateJavascript("document.documentElement.setAttribute('data-theme', '" + theme + "');", null);
    }

    public class WebAppInterface {
        @JavascriptInterface
        public boolean isDarkMode() {
            if (getContext() == null) return false;
            String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
            return ThemeUtils.isDarkMode(getContext(), role);
        }

        @JavascriptInterface
        public void navigateBack() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    getActivity().onBackPressed();
                });
            }
        }

        @JavascriptInterface
        public void openFeedback() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    isNavigatingToNextScreen = true; // Prevent nav bar restoration during transition
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow,
                                               R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow)
                            .replace(R.id.fragment_container, ChatFeedbackFragment.newInstance())
                            .commitAllowingStateLoss();
                });
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
            applyTheme();

            // Immersive Zero-Bar Mode
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            // Restore System UI
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null && !isNavigatingToNextScreen) {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
            } else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(true);
                ((MemberMainActivity) getActivity()).setHeaderVisible();
            }
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}
