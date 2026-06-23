package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.PayoutEntity;
import com.example.save.data.models.PaginatedResponse;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayoutAuditFragment extends Fragment {

    public static PayoutAuditFragment newInstance() {
        return new PayoutAuditFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payout_audit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvPayoutHistory);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        PayoutAuditAdapter adapter = new PayoutAuditAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        TextView tvEmpty = view.findViewById(R.id.tvEmptyPayouts);
        TextView tvDisbursed = view.findViewById(R.id.tvTotalDisbursed);
        TextView tvCount = view.findViewById(R.id.tvPayoutCount);
        TextView tvRetained = view.findViewById(R.id.tvRetentionTotal);

        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.getPayouts(100, 0).enqueue(new Callback<PaginatedResponse<PayoutEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<PayoutEntity>> call,
                    Response<PaginatedResponse<PayoutEntity>> response) {
                if (!isAdded() || getView() == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        List<PayoutEntity> payouts = response.body().getData();
                        adapter.setData(payouts);

                        double totalNet = 0, totalRetained = 0;
                        for (PayoutEntity p : payouts) {
                            totalNet += p.getNetAmount();
                            totalRetained += p.getRetentionAmount();
                        }

                        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
                        tvDisbursed.setText(ugFormat.format(totalNet).replace("UGX", "UGX "));
                        tvCount.setText(String.valueOf(payouts.size()));
                        tvRetained.setText(ugFormat.format(totalRetained).replace("UGX", "UGX "));
                        tvEmpty.setVisibility(payouts.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(Call<PaginatedResponse<PayoutEntity>> call, Throwable t) {
                if (!isAdded() || getView() == null) return;
                requireActivity().runOnUiThread(() -> tvEmpty.setVisibility(View.VISIBLE));
            }
        });
    }

    private static class PayoutAuditAdapter extends RecyclerView.Adapter<PayoutAuditAdapter.VH> {
        private List<PayoutEntity> data;
        private final NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        PayoutAuditAdapter(List<PayoutEntity> data) {
            this.data = data;
        }

        void setData(List<PayoutEntity> newData) {
            this.data = newData;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction_ledger, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            PayoutEntity p = data.get(position);
            holder.tvName.setText(p.getMemberName() != null ? p.getMemberName() : "Unknown");

            String dateStr = "";
            if (p.getExecutedAt() != null && !p.getExecutedAt().isEmpty()) {
                dateStr = p.getExecutedAt().substring(0, Math.min(10, p.getExecutedAt().length()));
            } else if (p.getCreatedAt() != null && !p.getCreatedAt().isEmpty()) {
                dateStr = p.getCreatedAt().substring(0, Math.min(10, p.getCreatedAt().length()));
            }
            holder.tvDate.setText(dateStr);

            String status = p.getStatus() != null ? p.getStatus() : "PENDING";
            holder.tvBadge.setText(status);
            if ("APPROVED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                holder.tvBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                holder.tvBadge.setTextColor(android.graphics.Color.parseColor("#16A34A"));
            } else if ("PENDING".equalsIgnoreCase(status)) {
                holder.tvBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEF9C3")));
                holder.tvBadge.setTextColor(android.graphics.Color.parseColor("#CA8A04"));
            } else {
                holder.tvBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FEE2E2")));
                holder.tvBadge.setTextColor(android.graphics.Color.parseColor("#DC2626"));
            }

            String amountStr = ugFormat.format(p.getNetAmount()).replace("UGX", "UGX ");
            holder.tvAmount.setText(amountStr);
            holder.tvAmount.setTextColor(android.graphics.Color.parseColor("#2563EB"));
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDate, tvBadge, tvAmount;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvTxMemberName);
                tvDate = v.findViewById(R.id.tvTxDateRef);
                tvBadge = v.findViewById(R.id.tvTxTypeBadge);
                tvAmount = v.findViewById(R.id.tvTxAmount);
            }
        }
    }
}
