package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        binding.webView.setWebViewClient(new WebViewClient());
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
        // Show dashboard bars as requested
        setBarsVisible(true);
        
        // Ensure standard system UI visibility
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
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
            activity.setHeaderVisible(visible);
        } else if (getActivity() instanceof AdminMainActivity) {
            AdminMainActivity activity = (AdminMainActivity) getActivity();
            activity.setBottomNavVisible(visible);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
