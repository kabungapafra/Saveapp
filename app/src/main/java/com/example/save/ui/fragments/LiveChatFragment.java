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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;

public class LiveChatFragment extends Fragment {

    private WebView webView;

    public static LiveChatFragment newInstance() {
        return new LiveChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_chat, container, false);
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

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");
        
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl("file:///android_asset/support_live_chat.html");
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void navigateBack() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    getActivity().onBackPressed();
                });
            }
        }
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
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.navContainer);
            if (nav != null) nav.setVisibility(View.VISIBLE);
            
            View fab = getActivity().findViewById(R.id.fabAction);
            if (fab != null) fab.setVisibility(View.VISIBLE);
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}
