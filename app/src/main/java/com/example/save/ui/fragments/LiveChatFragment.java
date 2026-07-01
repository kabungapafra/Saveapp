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
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;
import com.example.save.utils.ThemeUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveChatFragment extends Fragment {

    private WebView webView;
    private boolean isNavigatingToNextScreen = false;
    private final android.os.Handler statusHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private static final long STATUS_POLL_MS = 8000;
    private static final long MSG_POLL_MS = 5000;

    private final Runnable statusPollRunnable = this::checkChatStillActive;
    private final Runnable msgPollRunnable = this::fetchAndScheduleMessages;

    private void fetchAndScheduleMessages() {
        fetchMessages();
        if (!isNavigatingToNextScreen)
            statusHandler.postDelayed(msgPollRunnable, MSG_POLL_MS);
    }

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

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyTheme();
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/support_live_chat.html");
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
        public void loadChatMessages() {
            fetchMessages();
        }

        @JavascriptInterface
        public void onTyping() {
            postTypingPing();
        }

        @JavascriptInterface
        public void sendChatMessage(String content) {
            postMessage(content);
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

    /** Pull the member's real conversation (incl. admin replies) and render it in the WebView. */
    private void fetchMessages() {
        if (getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .getSupportMessages().enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || webView == null) return;
                        String messagesJson = "[]";
                        boolean adminTyping = false;
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                                org.json.JSONArray arr = obj.optJSONArray("messages");
                                if (arr != null) messagesJson = arr.toString();
                                adminTyping = obj.optBoolean("admin_typing", false);
                            }
                        } catch (Exception ignored) { }
                        final String js = "renderMessages(" + messagesJson + "," + adminTyping + ");";
                        if (getActivity() != null) {
                            final String finalJs = js;
                            getActivity().runOnUiThread(() -> {
                                if (webView != null) webView.evaluateJavascript(finalJs, null);
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) { }
                });
    }

    private void postTypingPing() {
        if (getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .sendMemberTyping().enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                                    @NonNull Response<okhttp3.ResponseBody> response) {}
                    @Override public void onFailure(@NonNull Call<okhttp3.ResponseBody> call,
                                                   @NonNull Throwable t) {}
                });
    }

    /** Persist a member's outgoing message, then refresh from the server. */
    private void postMessage(String content) {
        if (getContext() == null || content == null || content.trim().isEmpty()) return;
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("content", content);
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .sendSupportMessage(body).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (isAdded()) fetchMessages();
                    }

                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) { }
                });
    }

    private void checkChatStillActive() {
        if (!isAdded() || isNavigatingToNextScreen || getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .getChatStatus().enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || isNavigatingToNextScreen) return;
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                                String status = obj.optString("status", "active");
                                if ("closed".equals(status)) {
                                    // Admin ended the chat — navigate member to feedback
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            if (!isAdded() || isNavigatingToNextScreen) return;
                                            isNavigatingToNextScreen = true;
                                            getParentFragmentManager().beginTransaction()
                                                    .setCustomAnimations(
                                                            R.anim.transition_fade_in_slow,
                                                            R.anim.transition_fade_out_slow,
                                                            R.anim.transition_fade_in_slow,
                                                            R.anim.transition_fade_out_slow)
                                                    .replace(R.id.fragment_container,
                                                            ChatFeedbackFragment.newInstance())
                                                    .commitAllowingStateLoss();
                                        });
                                    }
                                    return;
                                }
                            }
                        } catch (Exception ignored) {}
                        statusHandler.postDelayed(statusPollRunnable, STATUS_POLL_MS);
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded()) statusHandler.postDelayed(statusPollRunnable, STATUS_POLL_MS);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMessages();
        statusHandler.postDelayed(statusPollRunnable, STATUS_POLL_MS);
        // Poll for new admin messages every 5 seconds.
        statusHandler.postDelayed(msgPollRunnable, MSG_POLL_MS);
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
        statusHandler.removeCallbacks(statusPollRunnable);
        statusHandler.removeCallbacks(msgPollRunnable);
        if (getActivity() != null) {
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
