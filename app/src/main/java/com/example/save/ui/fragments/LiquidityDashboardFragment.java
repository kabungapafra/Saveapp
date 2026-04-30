package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.databinding.FragmentLiquidityDashboardBinding;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

public class LiquidityDashboardFragment extends Fragment {

    private FragmentLiquidityDashboardBinding binding;

    public static LiquidityDashboardFragment newInstance() {
        return new LiquidityDashboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLiquidityDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupWebView();
        setupNavigation();
    }

    private void setupWebView() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        binding.webView.addJavascriptInterface(new WebAppInterface(), "Android");
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
                boolean isDark = com.example.save.utils.ThemeUtils.isDarkMode(requireContext(), role);
                String theme = isDark ? "dark" : "light";
                view.evaluateJavascript("document.documentElement.setAttribute('data-theme','" + theme + "');", null);
            }
        });
        binding.webView.loadUrl("file:///android_asset/liquidity_dashboard.html");
    }

    private void setupNavigation() {
        binding.fabBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setBarsVisible(true);
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        // Re-inject theme
        if (binding != null && getContext() != null) {
            String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
            boolean isDark = com.example.save.utils.ThemeUtils.isDarkMode(getContext(), role);
            binding.webView.evaluateJavascript("document.documentElement.setAttribute('data-theme','" + (isDark ? "dark" : "light") + "');", null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setBarsVisible(boolean visible) {
        if (getActivity() instanceof MemberMainActivity) {
            MemberMainActivity activity = (MemberMainActivity) getActivity();
            activity.setBottomNavVisible(visible);
            activity.setHeaderVisible();
        } else if (getActivity() instanceof AdminMainActivity) {
            AdminMainActivity activity = (AdminMainActivity) getActivity();
            activity.setBottomNavVisible(visible);
        }
    }

    private class WebAppInterface {
        @JavascriptInterface
        public boolean isDarkMode() {
            String role = (getActivity() instanceof AdminMainActivity) ? "admin" : "member";
            return com.example.save.utils.ThemeUtils.isDarkMode(requireContext(), role);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
