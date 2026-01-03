package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import Data.MemberRepository;
import Models.Member;
import com.example.save.R;

public class PayoutsFragment extends Fragment {

    private CardView btnExecutePayout;
    private CardView btnManageShortfalls;
    private TextView viewFullQueue;
    private androidx.recyclerview.widget.RecyclerView upcomingPayoutsRecyclerView;
    // private TextView nextRecipientName; // Removed
    // private TextView payoutAmount; // Removed

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payouts, container, false);

        initializeViews(view);
        setupListeners();
        updateUI();

        return view;
    }

    private void initializeViews(View view) {
        btnExecutePayout = view.findViewById(R.id.btnExecutePayout);
        btnManageShortfalls = view.findViewById(R.id.btnManageShortfalls);
        viewFullQueue = view.findViewById(R.id.viewFullQueue);
        // nextRecipientName = view.findViewById(R.id.nextRecipientName); // Removed
        // payoutAmount = view.findViewById(R.id.payoutAmount); // Removed
        upcomingPayoutsRecyclerView = view.findViewById(R.id.upcomingPayoutsRecyclerView);
    }

    private void updateUI() {
        MemberRepository repo = MemberRepository.getInstance();

        // Setup Upcoming Payouts List (Top 3 Eligible)
        upcomingPayoutsRecyclerView
                .setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        java.util.List<Models.Member> allMembers = repo.getAllMembers();
        java.util.List<Models.Member> upcomingMembers = new java.util.ArrayList<>();

        int count = 0;
        for (Models.Member member : allMembers) {
            if (!member.hasReceivedPayout() && member.isActive()) {
                upcomingMembers.add(member);
                count++;
                if (count >= 3)
                    break; // Show top 3
            }
        }

        upcomingPayoutsRecyclerView.setAdapter(new UpcomingPayoutAdapter(upcomingMembers));
    }

    private void setupListeners() {
        if (btnExecutePayout != null) {
            btnExecutePayout.setOnClickListener(v -> {
                MemberRepository repo = MemberRepository.getInstance();
                Models.Member nextRecipient = repo.getNextPayoutRecipient();

                if (nextRecipient != null) {
                    // Custom Confirmation Dialog
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm_payout, null);
                    builder.setView(dialogView);
                    android.app.AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(
                            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

                    TextView tvRecipientName = dialogView.findViewById(R.id.tvRecipientName);
                    TextView tvPayoutAmount = dialogView.findViewById(R.id.tvPayoutAmount);
                    android.widget.Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
                    android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);

                    tvRecipientName.setText(nextRecipient.getName());
                    tvPayoutAmount.setText("UGX 500,000"); // Dynamic amount if available

                    btnConfirm.setOnClickListener(view -> {
                        boolean success = repo.executePayout(nextRecipient);
                        if (success) {
                            android.widget.Toast.makeText(getContext(),
                                    "Payout executed for " + nextRecipient.getName(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                            updateUI();
                        } else {
                            android.widget.Toast.makeText(getContext(),
                                    "Insufficient balance: UGX " + repo.getGroupBalance(),
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    });

                    btnCancel.setOnClickListener(view -> dialog.dismiss());

                    dialog.show();
                } else {
                    android.widget.Toast.makeText(getContext(),
                            "No recipient available",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnManageShortfalls != null) {
            btnManageShortfalls.setOnClickListener(v -> {
                showShortfallsDialog();
            });
        }

        if (viewFullQueue != null) {
            viewFullQueue.setOnClickListener(v -> {
                showQueueDialog();
            });
        }
    }

    private void showShortfallsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_manage_shortfalls, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.shortfallsRecyclerView);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        MemberRepository repo = MemberRepository.getInstance();
        java.util.List<Models.Member> shortfalls = repo.getMembersWithShortfalls();
        recyclerView.setAdapter(new ShortfallAdapter(shortfalls));

        android.widget.Button btnSendReminders = dialogView.findViewById(R.id.btnSendReminders);
        if (btnSendReminders != null) {
            btnSendReminders.setOnClickListener(v -> {
                android.widget.Toast.makeText(getContext(), "Reminders Sent to " + shortfalls.size() + " members",
                        android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        android.widget.Button btnCoverReserve = dialogView.findViewById(R.id.btnCoverReserve);
        if (btnCoverReserve != null) {
            btnCoverReserve.setOnClickListener(v -> {
                int coveredCount = 0;
                for (Models.Member m : shortfalls) {
                    repo.resolveShortfall(m); // Resolve for each member in the list
                    coveredCount++;
                }
                android.widget.Toast.makeText(getContext(), "Covered shortfalls for " + coveredCount + " members",
                        android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // Ideally refresh the shortfall list if we claim to cover them
            });
        }

        TextView btnClose = dialogView.findViewById(R.id.btnCloseShortfalls);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private class ShortfallAdapter
            extends androidx.recyclerview.widget.RecyclerView.Adapter<ShortfallAdapter.ShortfallViewHolder> {
        private java.util.List<Models.Member> members;

        ShortfallAdapter(java.util.List<Models.Member> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public ShortfallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_simple, parent, false);
            return new ShortfallViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ShortfallViewHolder holder, int position) {
            Models.Member member = members.get(position);
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
            TextView name, role;
            View status;

            ShortfallViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvMemberName);
                role = itemView.findViewById(R.id.tvMemberRole);
                status = itemView.findViewById(R.id.statusIndicator);
            }
        }
    }

    private class UpcomingPayoutAdapter
            extends androidx.recyclerview.widget.RecyclerView.Adapter<UpcomingPayoutAdapter.UpcomingViewHolder> {
        private java.util.List<Models.Member> members;

        UpcomingPayoutAdapter(java.util.List<Models.Member> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public UpcomingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payout_queue, parent, false);
            return new UpcomingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UpcomingViewHolder holder, int position) {
            Models.Member member = members.get(position);
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
            TextView rank, name, date, amount;

            UpcomingViewHolder(View itemView) {
                super(itemView);
                rank = itemView.findViewById(R.id.tvRank);
                name = itemView.findViewById(R.id.tvName);
                date = itemView.findViewById(R.id.tvDate);
                amount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }

    private void showQueueDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_payout_queue, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow()
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.queueRecyclerView);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        MemberRepository repo = MemberRepository.getInstance();
        java.util.List<Models.Member> members = repo.getAllMembers();
        recyclerView.setAdapter(new QueueAdapter(members));

        android.widget.ImageView btnClose = dialogView.findViewById(R.id.btnCloseQueue);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private class QueueAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {
        private java.util.List<Models.Member> members;

        QueueAdapter(java.util.List<Models.Member> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Using detailed item layout now
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payout_queue, parent, false);
            return new QueueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
            Models.Member member = members.get(position);
            holder.rank.setText(String.valueOf(position + 1));
            holder.name.setText(member.getName());

            if (member.hasReceivedPayout()) {
                holder.date.setText("Paid on: " + member.getPayoutDate());
                holder.date.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                holder.amount.setText("Paid");
            } else {
                holder.date.setText("Estimated: Future Date");
                holder.date.setTextColor(getResources().getColor(android.R.color.darker_gray));
                holder.amount.setText("500k");
            }
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class QueueViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView rank, name, date, amount;

            QueueViewHolder(View itemView) {
                super(itemView);
                // Bind to item_payout_queue IDs
                rank = itemView.findViewById(R.id.tvRank);
                name = itemView.findViewById(R.id.tvName);
                date = itemView.findViewById(R.id.tvDate);
                amount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
}
