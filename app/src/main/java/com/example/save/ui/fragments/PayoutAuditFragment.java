package com.example.save.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;

public class PayoutAuditFragment extends Fragment {

    private WebView webView;

    public static PayoutAuditFragment newInstance() {
        return new PayoutAuditFragment();
    }

    private class WebAppInterface {
        @JavascriptInterface
        public boolean isDarkMode() {
            if (getContext() == null) return false;
            String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
            return com.example.save.utils.ThemeUtils.isDarkMode(getContext(), role);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payout_audit, container, false);
        webView = view.findViewById(R.id.webView);
        setupWebView();
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (getContext() == null) return;
                String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
                boolean isDark = com.example.save.utils.ThemeUtils.isDarkMode(getContext(), role);
                String theme = isDark ? "dark" : "light";
                view.evaluateJavascript("document.documentElement.setAttribute('data-theme','" + theme + "');", null);
            }
        });
        // Load the highly optimized local HTML prototype
        webView.loadUrl("file:///android_asset/payout_audit.html");
    }

    @Override
    public void onResume() {
        super.onResume();
        applyTheme();
    }

    private void applyTheme() {
        if (getContext() == null || webView == null) return;
        String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
        boolean isDark = com.example.save.utils.ThemeUtils.isDarkMode(getContext(), role);
        String theme = isDark ? "dark" : "light";
        webView.evaluateJavascript("document.documentElement.setAttribute('data-theme', '" + theme + "');", null);
    }
}
