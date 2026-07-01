package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.activities.AdminMainActivity;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatRequestsFragment extends Fragment {

    private static final String ARG_CONV_ID = "conv_id";
    private static final long REFRESH_MS = 8000;

    private LinearLayout requestsContainer;
    private TextView tvEmpty;
    private View progressBar;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String highlightConvId;

    private final Runnable refreshRunnable = this::loadRequests;

    public static AdminChatRequestsFragment newInstance(String convId) {
        AdminChatRequestsFragment f = new AdminChatRequestsFragment();
        Bundle args = new Bundle();
        if (convId != null) args.putString(ARG_CONV_ID, convId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) highlightConvId = getArguments().getString(ARG_CONV_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat_requests, container, false);
        requestsContainer = view.findViewById(R.id.requestsContainer);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AdminMainActivity)
            ((AdminMainActivity) getActivity()).setBottomNavVisible(false);
        loadRequests();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
        if (getActivity() instanceof AdminMainActivity)
            ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void loadRequests() {
        if (!isAdded() || getContext() == null) return;
        if (progressBar != null && requestsContainer.getChildCount() == 0)
            progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .getAdminChatRequests().enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            try {
                                if (response.isSuccessful() && response.body() != null) {
                                    JSONArray arr = new JSONArray(response.body().string());
                                    renderRequests(arr);
                                }
                            } catch (Exception ignored) {}
                            handler.postDelayed(refreshRunnable, REFRESH_MS);
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded()) handler.postDelayed(refreshRunnable, REFRESH_MS);
                    }
                });
    }

    private void renderRequests(JSONArray arr) {
        if (!isAdded() || requestsContainer == null) return;
        requestsContainer.removeAllViews();
        if (arr.length() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject item = arr.getJSONObject(i);
                String convId = item.optString("id");
                String userName = item.optString("user_name", "Member");
                String userPhone = item.optString("user_phone", "");
                String createdAt = item.optString("created_at", "");

                View row = inflater.inflate(R.layout.item_chat_request, requestsContainer, false);
                ((TextView) row.findViewById(R.id.tvUserName)).setText(userName);
                ((TextView) row.findViewById(R.id.tvUserPhone)).setText(userPhone);
                ((TextView) row.findViewById(R.id.tvTime)).setText(formatTime(createdAt));

                MaterialButton btnAccept = row.findViewById(R.id.btnAccept);
                MaterialButton btnDecline = row.findViewById(R.id.btnDecline);

                btnAccept.setOnClickListener(v -> acceptChat(convId, userName, row));
                btnDecline.setOnClickListener(v -> declineChat(convId, row));

                // Highlight the card that triggered the notification
                if (convId.equals(highlightConvId)) {
                    row.setBackgroundResource(R.color.brand_blue_light);
                }
                requestsContainer.addView(row);
            } catch (Exception ignored) {}
        }
    }

    private void acceptChat(String convId, String userName, View row) {
        if (getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .acceptChat(convId).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                handler.removeCallbacks(refreshRunnable);
                                ((AdminMainActivity) getActivity()).loadFragment(
                                        AdminLiveChatFragment.newInstance(convId, userName), true);
                            } else {
                                Toast.makeText(getContext(), "Failed to accept chat", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                        if (isAdded())
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void declineChat(String convId, View row) {
        if (getContext() == null) return;
        RetrofitClient.getClient(requireContext()).create(ApiService.class)
                .declineChat(convId).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<okhttp3.ResponseBody> call,
                                           @NonNull Response<okhttp3.ResponseBody> response) {
                        if (!isAdded() || getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            if (response.isSuccessful()) {
                                requestsContainer.removeView(row);
                                if (requestsContainer.getChildCount() == 0)
                                    tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getContext(), "Failed to decline", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {}
                });
    }

    private String formatTime(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date d = in.parse(iso);
            return new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(d);
        } catch (Exception e) {
            return "";
        }
    }
}
