package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentPollsBinding;
import android.webkit.JavascriptInterface;

public class PollsFragment extends Fragment {

    private FragmentPollsBinding binding;

    public static PollsFragment newInstance() {
        return new PollsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPollsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        setupWebView();
    }

    private void setupWebView() {
        android.webkit.WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);

        binding.webView.setWebViewClient(new android.webkit.WebViewClient());
        binding.webView.addJavascriptInterface(new WebAppInterface(), "Android");
        binding.webView.loadUrl("file:///android_asset/voting_hub.html");
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onCreatePollClicked() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                                .replace(R.id.fragment_container, CreatePollFragment.newInstance())
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
        }
        @JavascriptInterface
        public void onCastVoteClicked() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                                .replace(R.id.fragment_container, CastVoteFragment.newInstance())
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
        }
        @JavascriptInterface
        public void onActiveStandingsClicked() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                                .replace(R.id.fragment_container, ActiveStandingsFragment.newInstance())
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
