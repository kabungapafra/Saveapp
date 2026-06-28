package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.network.ApiResponse;
import com.example.save.data.models.ApprovalRequest;
import com.example.save.data.models.PayoutQueueEntry;
import com.example.save.data.models.PayoutQueueResponse;
import com.example.save.data.models.ReorderRequest;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.databinding.FragmentQueueBinding;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.ui.adapters.PayoutQueueAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueueFragment extends Fragment {

    private FragmentQueueBinding binding;
    private PayoutQueueAdapter adapter;
    private ApprovalsAdapter approvalsAdapter;
    private MembersViewModel viewModel;
    private ItemTouchHelper touchHelper;
    private boolean isAdmin;

    public static QueueFragment newInstance() {
        return new QueueFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentQueueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        isAdmin = getActivity() instanceof com.example.save.ui.activities.AdminMainActivity;

        setupRecyclerView();
        setupApprovals();
        setupInteractions();
        loadQueue();
    }

    private void setupInteractions() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        if (!isAdmin) {
            binding.llPendingApprovals.setVisibility(View.GONE);
        }

        binding.btnApprovalsHeader.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                        .loadFragment(new ApprovalsFragment(), true);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new PayoutQueueAdapter(new ArrayList<>(), 0, isAdmin);
        binding.rvQueue.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQueue.setAdapter(adapter);

        // Drag-and-drop (admin only, unpaid items only via drag handle)
        if (isAdmin) {
            ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

                @Override
                public boolean onMove(@NonNull RecyclerView rv,
                        @NonNull RecyclerView.ViewHolder from,
                        @NonNull RecyclerView.ViewHolder to) {
                    PayoutQueueEntry fromEntry = adapter.getEntries().get(from.getAdapterPosition());
                    PayoutQueueEntry toEntry = adapter.getEntries().get(to.getAdapterPosition());
                    // Only allow moving unpaid items
                    if (fromEntry.hasReceivedPayout() || toEntry.hasReceivedPayout()) return false;
                    adapter.onItemMoved(from.getAdapterPosition(), to.getAdapterPosition());
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

                @Override
                public void clearView(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                    super.clearView(rv, vh);
                    adapter.onDropFinished();
                }
            };
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(binding.rvQueue);
            adapter.setTouchHelper(touchHelper);

            adapter.setOnReorderListener(newOrder -> sendReorderToServer(newOrder));
        }
    }

    private void setupApprovals() {
        approvalsAdapter = new ApprovalsAdapter(new ApprovalsAdapter.OnApprovalClickListener() {
            @Override
            public void onApproveClick(ApprovalsAdapter.ApprovalItem item) {
                viewModel.processApproval(item, true, (success, message) -> {
                    if (isVisible()) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onItemClick(ApprovalsAdapter.ApprovalItem item) {
                if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                    String amountStr = "UGX " + String.format(java.util.Locale.US, "%,.0f", item.getAmount());
                    ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                            .loadFragment(PayoutConfirmationFragment.newInstance(item.getId(), amountStr), true);
                }
            }
        });
        binding.rvPendingApprovals.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPendingApprovals.setAdapter(approvalsAdapter);

        viewModel.getPendingApprovals().observe(getViewLifecycleOwner(), approvals -> {
            List<ApprovalsAdapter.ApprovalItem> payoutApprovals = new ArrayList<>();
            if (approvals != null) {
                for (ApprovalRequest a : approvals) {
                    if ("PAYOUT".equalsIgnoreCase(a.getType())) payoutApprovals.add(a);
                }
            }
            if (payoutApprovals.isEmpty()) {
                binding.rvPendingApprovals.setVisibility(View.GONE);
                binding.tvNoApprovals.setVisibility(View.VISIBLE);
            } else {
                binding.rvPendingApprovals.setVisibility(View.VISIBLE);
                binding.tvNoApprovals.setVisibility(View.GONE);
                approvalsAdapter.updateList(payoutApprovals);
            }
        });
    }

    private void loadQueue() {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.getPayoutQueue().enqueue(new Callback<PayoutQueueResponse>() {
            @Override
            public void onResponse(@NonNull Call<PayoutQueueResponse> call,
                    @NonNull Response<PayoutQueueResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    bindQueueData(response.body());
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PayoutQueueResponse> call, @NonNull Throwable t) {
                if (isAdded()) showEmpty();
            }
        });

        viewModel.fetchSystemConfig((success, config, message) -> {
            if (success && config != null && isAdded()) {
                double contribution = config.getContributionAmount();
                double retention = config.getRetentionPercentage();
                // Store for use once queue size is known
                binding.tvPayoutPerMember.setTag(new double[]{contribution, retention});
            }
        });
    }

    private void bindQueueData(PayoutQueueResponse data) {
        List<PayoutQueueEntry> queue = data.getQueue();
        if (queue == null || queue.isEmpty()) {
            showEmpty();
            return;
        }

        binding.emptyState.setVisibility(View.GONE);
        binding.rvQueue.setVisibility(View.VISIBLE);

        adapter.updateEntries(queue);

        // Hero stats
        binding.tvQueueCount.setText(String.valueOf(data.getTotal()));
        binding.tvNextPayoutDate.setText("Cycle " + data.getCycleNumber());
        binding.tvCycleLabel.setText("Round " + data.getCycleNumber());

        // Next 5 banner
        int pending = data.getPending();
        int paid = data.getPaid();
        binding.tvPendingCount.setText(pending + " pending");
        List<PayoutQueueEntry> nextBatch = data.getNextBatch();
        if (nextBatch != null && !nextBatch.isEmpty()) {
            StringBuilder names = new StringBuilder("Up next: ");
            for (int i = 0; i < nextBatch.size(); i++) {
                if (i > 0) names.append(", ");
                String name = nextBatch.get(i).getMemberName();
                names.append(name != null && name.length() > 0
                        ? name.split(" ")[0] : "?");
            }
            binding.tvNextBatchLabel.setText(names.toString());
        } else if (pending == 0) {
            binding.tvNextBatchLabel.setText("All members paid! Queue resets soon.");
        }

        // Payout amount — prefer values fetched from API (stored in tag), fall back to SharedPreferences
        double contribution = 0, retentionPct = 0;
        Object tag = binding.tvPayoutPerMember.getTag();
        if (tag instanceof double[]) {
            double[] cr = (double[]) tag;
            contribution = cr[0];
            retentionPct = cr[1];
        }
        if (contribution == 0) {
            android.content.SharedPreferences prefs = requireContext()
                    .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            String raw = prefs.getString("rule_contribution_amount", "0").replaceAll("[^0-9.]", "");
            try { contribution = raw.isEmpty() ? 0 : Double.parseDouble(raw); } catch (Exception ignored) {}
            try { retentionPct = Double.parseDouble(prefs.getString("rule_retention_pct", "0")); } catch (Exception ignored) {}
        }
        double payout = contribution * data.getTotal() * (1.0 - retentionPct / 100.0);
        if (payout > 0) {
            adapter.setPayoutAmount(payout);
            binding.tvPayoutPerMember.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(payout));
        }
    }

    private void sendReorderToServer(List<PayoutQueueEntry> ordered) {
        List<ReorderRequest.PositionEntry> positions = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            positions.add(new ReorderRequest.PositionEntry(ordered.get(i).getMemberId(), i + 1));
        }
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.reorderPayoutQueue(new ReorderRequest(positions)).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (isAdded() && (!response.isSuccessful())) {
                    Toast.makeText(getContext(), "Failed to save order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "No connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmpty() {
        binding.rvQueue.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
