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
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

public class LiveChatFragment extends Fragment {

    private WebView webView;
    private boolean isNavigatingToFeedback = false;

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

        @JavascriptInterface
        public void openFeedback() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    isNavigatingToFeedback = true; // Prevent nav bar restoration
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                            .replace(R.id.fragment_container, ChatFeedbackFragment.newInstance())
                            .commit();
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
                ((MemberMainActivity) getActivity()).setHeaderVisible(false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null && !isNavigatingToFeedback) {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
            } else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(true);
                ((MemberMainActivity) getActivity()).setHeaderVisible(true);
            }
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}
