package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

public class StashFragment extends Fragment {

    private WebView webView;

    public static StashFragment newInstance() {
        return new StashFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stash, container, false);

        webView = view.findViewById(R.id.webView);
        setupWebView();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity || getActivity() instanceof MemberMainActivity) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.btnThemeToggle).setOnClickListener(v -> com.example.save.utils.ThemeUtils.toggleTheme(requireContext()));

        return view;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);

        // Register the bridge
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Potential integration points for data feeding
            }
        });

        webView.loadUrl("file:///android_asset/stash_dashboard.html");
    }

    // JS Bridge class
    public class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void navigateToPayment() {
            navigateTo(MakeContributionFragment.newInstance());
        }

        @android.webkit.JavascriptInterface
        public void navigateToLoanRequest() {
            navigateTo(LoanApplicationFragment.newInstance());
        }

        @android.webkit.JavascriptInterface
        public void navigateToLoanRepayment() {
            navigateTo(LoanPaymentFragment.newInstance());
        }

        private void navigateTo(final Fragment fragment) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() instanceof AdminMainActivity) {
                    ((AdminMainActivity) getActivity()).loadFragment(fragment);
                } else if (getActivity() instanceof MemberMainActivity) {
                    ((MemberMainActivity) getActivity()).loadFragment(fragment, true);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AdminMainActivity) {
            ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
        } else if (getActivity() instanceof MemberMainActivity) {
            // Member side visibility handling if needed
        }
    }
}
