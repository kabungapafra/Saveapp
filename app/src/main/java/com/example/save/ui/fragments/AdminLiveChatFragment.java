package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.activities.AdminMainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLiveChatFragment extends Fragment {

    private static final String ARG_CONV_ID = "conv_id";
    private static final String ARG_USER_NAME = "user_name";
    private static final long POLL_MS = 5000;

    private String convId;
    private String userName;
    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private EditText etMessage;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean chatClosed = false;

    private final Runnable pollRunnable = this::fetchMessages;

    public static AdminLiveChatFragment newInstance(String convId, String userName) {
        AdminLiveChatFragment f = new AdminLiveChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONV_ID, convId);
        args.putString(ARG_USER_NAME, userName != null ? userName : "Member");
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            convId = getArguments().getString(ARG_CONV_ID);
            userName = getArguments().getString(ARG_USER_NAME, "Member");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_live_chat, container, false);

        messagesContainer = view.findViewById(R.id.messagesContainer);
        scrollView = view.findViewById(R.id.svMessages);
        etMessage = view.findViewById(R.id.etMessage);

        TextView tvTitle = view.findViewById(R.id.tvChatTitle);
        if (tvTitle != null) tvTitle.setText("Chat with " + userName);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> promptEndChat());
        view.findViewById(R.id.btnEndChat).setOnClickListener(v -> promptEndChat());
        view.findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AdminMainActivity)
            ((AdminMainActivity) getActivity()).setBottomNavVisible(false);
        fetchMessages();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollRunnable);
        if (getActivity() instanceof AdminMainActivity)
            ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    // ── Backend ────────────────────────────────────────────────────────────────

    private void fetchMessages() {
        if (!isAdded() || getContext() == null || convId == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .getAdminChatMessages(convId).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            try {
                                if (response.isSuccessful() && response.body() != null) {
                                    JSONObject obj = new JSONObject(response.body().string());
                                    String status = obj.optString("status", "active");
                                    JSONArray msgs = obj.optJSONArray("messages");
                                    if (msgs != null) renderMessages(msgs);
                                    if ("closed".equals(status) && !chatClosed) {
                                        chatClosed = true;
                                        showMemberEndedBanner();
                                        return;
                                    }
                                }
                            } catch (Exception ignored) {}
                            if (!chatClosed) handler.postDelayed(pollRunnable, POLL_MS);
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded() && !chatClosed) handler.postDelayed(pollRunnable, POLL_MS);
                    }
                });
    }

    private void sendMessage() {
        if (getContext() == null || etMessage == null) return;
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;
        etMessage.setText("");
        Map<String, String> body = new HashMap<>();
        body.put("content", content);
        addBubble(content, true);
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .sendAdminMessage(convId, body).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (isAdded()) fetchMessages();
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {}
                });
    }

    private void endChat() {
        if (getContext() == null || convId == null) return;
        handler.removeCallbacks(pollRunnable);
        chatClosed = true;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .endChatAdmin(convId).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (isAdded() && getActivity() != null)
                            getActivity().runOnUiThread(() -> getActivity().onBackPressed());
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded() && getActivity() != null)
                            getActivity().runOnUiThread(() -> getActivity().onBackPressed());
                    }
                });
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    private void renderMessages(JSONArray msgs) {
        if (!isAdded() || messagesContainer == null) return;
        messagesContainer.removeAllViews();
        for (int i = 0; i < msgs.length(); i++) {
            try {
                JSONObject m = msgs.getJSONObject(i);
                String role = m.optString("sender_role", "user");
                addBubble(m.optString("content", ""), "admin".equals(role));
            } catch (Exception ignored) {}
        }
        scrollToBottom();
    }

    private void addBubble(String text, boolean isAdmin) {
        if (!isAdded() || messagesContainer == null || getContext() == null) return;
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setTextColor(isAdmin ? 0xFFFFFFFF : getResources().getColor(R.color.v_text_dark, null));
        tv.setBackgroundResource(isAdmin ? R.drawable.bg_bubble_admin : R.drawable.bg_bubble_user);
        tv.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        lp.gravity = isAdmin ? Gravity.END : Gravity.START;
        tv.setLayoutParams(lp);
        messagesContainer.addView(tv);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (scrollView != null)
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void promptEndChat() {
        if (!isAdded() || getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("End Chat")
                .setMessage("Are you sure you want to end this chat session?")
                .setPositiveButton("End Chat", (d, w) -> endChat())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMemberEndedBanner() {
        if (!isAdded() || getContext() == null) return;
        Toast.makeText(getContext(), userName + " has ended the chat", Toast.LENGTH_LONG).show();
        handler.postDelayed(() -> {
            if (isAdded() && getActivity() != null) getActivity().onBackPressed();
        }, 2500);
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
