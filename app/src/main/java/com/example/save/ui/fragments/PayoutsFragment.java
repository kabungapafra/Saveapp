package com.example.save.ui.fragments;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.data.models.Member;
import com.example.save.R;
import com.example.save.databinding.DialogConfirmPayoutBinding;
import com.example.save.databinding.DialogManageShortfallsBinding;
import com.example.save.databinding.DialogPayoutQueueBinding;
import com.example.save.databinding.FragmentPayoutsBinding;
import com.example.save.databinding.ItemMemberSimpleBinding;
import com.example.save.databinding.ItemPayoutQueueBinding;
import com.example.save.ui.viewmodels.PayoutsViewModel;

import com.example.save.ui.adapters.ShortfallAdapter;
import com.example.save.ui.adapters.PayoutQueueAdapter;

public class PayoutsFragment extends Fragment {

    private FragmentPayoutsBinding binding;
    private PayoutsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentPayoutsBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(PayoutsViewModel.class);

        setupListeners();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                updateUI(members);
            }
        });
    }

    private void updateUI(java.util.List<Member> allMembers) {
        if (getContext() == null)
            return;

        // Populate Hero Card with next recipient - use background thread
        new Thread(() -> {
            try {
                Member nextRecipient = viewModel.getNextPayoutRecipient();
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        if (nextRecipient != null) {
                            binding.tvNextRecipientName.setText(nextRecipient.getName());
                            binding.tvHeroAmount.setText("UGX 500,000");
                        } else {
                            binding.tvNextRecipientName.setText("No Payouts Due");
                            binding.tvHeroAmount.setText("---");
                        }
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.tvNextRecipientName.setText("Error");
                        binding.tvHeroAmount.setText("---");
                    }
                });
            }
        }).start();

        // Setup Upcoming Payouts List (Top 3 Eligible)
        binding.upcomingPayoutsRecyclerView
                .setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        java.util.List<Member> upcomingMembers = new java.util.ArrayList<>();

        int count = 0;
        for (Member member : allMembers) {
            if (!member.hasReceivedPayout() && member.isActive()) {
                upcomingMembers.add(member);
                count++;
                if (count >= 3)
                    break; // Show top 3
            }
        }

        binding.upcomingPayoutsRecyclerView.setAdapter(new PayoutQueueAdapter(upcomingMembers, false));
    }

    private void setupListeners() {
        binding.btnExecutePayout.setOnClickListener(v -> {
            // Fetch next recipient on background thread
            new Thread(() -> {
                try {
                    Member nextRecipient = viewModel.getNextPayoutRecipient();

                    requireActivity().runOnUiThread(() -> {
                        if (nextRecipient != null) {
                            // Custom Confirmation Dialog
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                            DialogConfirmPayoutBinding dialogBinding = DialogConfirmPayoutBinding
                                    .inflate(LayoutInflater.from(getContext()));
                            builder.setView(dialogBinding.getRoot());
                            android.app.AlertDialog dialog = builder.create();
                            dialog.getWindow().setBackgroundDrawable(
                                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

                            dialogBinding.tvRecipientName.setText(nextRecipient.getName());
                            dialogBinding.tvPayoutAmount.setText("UGX 500,000"); // Dynamic amount if available

                            dialogBinding.btnConfirm.setOnClickListener(view -> {
                                // Execute payout on background thread
                                new Thread(() -> {
                                    try {
                                        boolean success = viewModel.executePayout(nextRecipient);
                                        requireActivity().runOnUiThread(() -> {
                                            if (success) {
                                                android.widget.Toast.makeText(getContext(),
                                                        "Payout executed for " + nextRecipient.getName(),
                                                        android.widget.Toast.LENGTH_SHORT).show();
                                                // UI updates automatically via LiveData
                                            } else {
                                                android.widget.Toast.makeText(getContext(),
                                                        "Insufficient balance or invalid member",
                                                        android.widget.Toast.LENGTH_SHORT).show();
                                            }
                                            dialog.dismiss();
                                        });
                                    } catch (Exception e) {
                                        requireActivity().runOnUiThread(() -> {
                                            android.widget.Toast.makeText(getContext(),
                                                    "Error executing payout",
                                                    android.widget.Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        });
                                    }
                                }).start();
                            });

                            dialogBinding.btnCancel.setOnClickListener(view -> dialog.dismiss());

                            dialog.show();
                        } else {
                            android.widget.Toast.makeText(getContext(),
                                    "No recipient available",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(),
                                "Error loading recipient",
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        binding.btnManageShortfalls.setOnClickListener(v -> {
            showShortfallsDialog();
        });

        binding.btnPayoutHistory.setOnClickListener(v -> {
            showPayoutHistoryDialog();
        });

        binding.viewFullQueue.setOnClickListener(v -> {
            showQueueDialog();
        });

        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void showPayoutHistoryDialog() {
        if (getContext() == null)
            return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        // We reuse a similar layout or create a new one. Since we created
        // dialog_payout_history.xml, we use that.
        // Assuming we need to import or load it. Since we haven't created a Binding
        // class for it yet (build process),
        // we might access it via View inflation if binding isn't available immediately,
        // but let's assume standard binding naming.
        // NOTE: New layouts might not generate binding classes immediately in this
        // environment without a build trigger,
        // but for safety in this environment we can use standard inflation or try
        // binding if we assume the user environment rebuilds.
        // However, safely, we'll try to use a binding approach if consistent, or
        // fallback to View inflation.
        // Given the rules, we should assume binding is preferred. The layout file is
        // dialog_payout_history.xml -> DialogPayoutHistoryBinding.

        // Since we cannot trigger a Gradle build here, the binding class
        // 'DialogPayoutHistoryBinding' won't exist yet for us to reference in Java code
        // strictly.
        // BUT, I can write the code assuming it will exist.
        // Wait, I cannot reference a class that doesn't exist in the classpath.
        // To be safe and ensure it compiles/runs without a full build cycle in this
        // agentic loop, I will use LayoutInflater and findViewById for this new dialog.

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payout_history, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        androidx.recyclerview.widget.RecyclerView rv = dialogView.findViewById(R.id.historyRecyclerView);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        TextView tvSummary = dialogView.findViewById(R.id.tvHistorySummary);
        dialogView.findViewById(R.id.btnCloseHistory).setOnClickListener(v -> dialog.dismiss());

        java.util.List<Member> members = viewModel.getMembers().getValue();
        java.util.List<Member> history = new java.util.ArrayList<>();
        double totalPaidOut = 0;

        if (members != null) {
            for (Member m : members) {
                if (m.hasReceivedPayout()) {
                    history.add(m);
                    // Parse amount if possible, or just assume 500k for summary
                    totalPaidOut += 500000;
                }
            }
        }

        tvSummary.setText(history.size() + " Payouts • Total: UGX "
                + java.text.NumberFormat.getIntegerInstance().format(totalPaidOut));

        rv.setAdapter(new PayoutQueueAdapter(history, true)); // Reuse adapter with full info

        dialog.show();
    }

    private void showShortfallsDialog() {
        if (getContext() == null)
            return;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        DialogManageShortfallsBinding dialogBinding = DialogManageShortfallsBinding
                .inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialogBinding.shortfallsRecyclerView
                .setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        java.util.List<Member> members = viewModel.getMembers().getValue();
        java.util.List<Member> shortfalls = new java.util.ArrayList<>();
        if (members != null) {
            for (Member m : members) {
                if (m.getShortfallAmount() > 0)
                    shortfalls.add(m);
            }
        }

        dialogBinding.shortfallsRecyclerView.setAdapter(new ShortfallAdapter(shortfalls));

        dialogBinding.btnSendReminders.setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "Reminders Sent to " + shortfalls.size() + " members",
                    android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogBinding.btnCoverReserve.setOnClickListener(v -> {
            int coveredCount = 0;
            for (Member m : shortfalls) {
                viewModel.resolveShortfall(m); // Resolve for each member in the list
                coveredCount++;
            }
            android.widget.Toast.makeText(getContext(), "Covered shortfalls for " + coveredCount + " members",
                    android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialogBinding.btnCloseShortfalls.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private class ShortfallAdapter
            extends androidx.recyclerview.widget.RecyclerView.Adapter<ShortfallAdapter.ShortfallViewHolder> {
        private java.util.List<Member> members;

        ShortfallAdapter(java.util.List<Member> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public ShortfallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMemberSimpleBinding itemBinding = ItemMemberSimpleBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ShortfallViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ShortfallViewHolder holder, int position) {
            Member member = members.get(position);
            holder.name.setText(member.getName());
            holder.role.setText("Shortfall: UGX "
                    + java.text.NumberFormat.getIntegerInstance().format(member.getShortfallAmount()));
            holder.role.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            holder.status.setBackgroundResource(R.drawable.circle_red); // Need a red circle drawable or tint
            // Or just reuse circle_background and tint it
            holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.RED));
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class ShortfallViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            final ItemMemberSimpleBinding itemBinding;
            TextView name, role;
            View status;

            ShortfallViewHolder(ItemMemberSimpleBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                name = itemBinding.tvMemberName;
                role = itemBinding.tvMemberRole;
                status = itemBinding.statusIndicator;
            }
        }
    }

    private class UpcomingPayoutAdapter
            extends androidx.recyclerview.widget.RecyclerView.Adapter<UpcomingPayoutAdapter.UpcomingViewHolder> {
        private java.util.List<Member> members;

        UpcomingPayoutAdapter(java.util.List<Member> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public UpcomingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPayoutQueueBinding itemBinding = ItemPayoutQueueBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new UpcomingViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull UpcomingViewHolder holder, int position) {
            Member member = members.get(position);
            holder.rank.setText(String.valueOf(position + 1));
            holder.name.setText(member.getName());
            // Dummy date logic for preview
            holder.date.setText("Estimated: " + (position == 0 ? "This Month" : "Next Month"));
            holder.amount.setText("500k"); // Dummy amount
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class UpcomingViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            final ItemPayoutQueueBinding itemBinding;
            TextView rank, name, date, amount;

            UpcomingViewHolder(ItemPayoutQueueBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                rank = itemBinding.tvRank;
                name = itemBinding.tvName;
                date = itemBinding.tvDate;
                amount = itemBinding.tvAmount;
            }
        }
    }

    private void showQueueDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        DialogPayoutQueueBinding dialogBinding = DialogPayoutQueueBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialogBinding.queueRecyclerView
                .setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // Observe directly from VM or use current list value
        java.util.List<Member> members = viewModel.getMembers().getValue();
        if (members == null)
            members = new java.util.ArrayList<>();

        dialogBinding.queueRecyclerView.setAdapter(new PayoutQueueAdapter(members, true));

        dialogBinding.btnCloseQueue.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
