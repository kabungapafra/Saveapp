package com.example.save.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.data.models.Poll;
import com.example.save.data.network.PollCreateRequest;
import com.example.save.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePollFragment extends Fragment {

    // selectedRole is a String to support cards ("Admin", "Treasurer") and chips
    private String selectedRole = "Admin";
    // Tracks which chip (if any) is currently selected: "Secretary", "Moderator", or custom name
    private String selectedChipRole = null;

    private View cardAdmin, cardTreasurer;
    private View badgeAdminSelected, badgeTreasurerSelected;
    private TextView tvSelectedCount;
    private TextView chipSecretary, chipModerator, chipCustom;
    private RecyclerView rvNominees;
    private TextView tvNoMembers;
    private ImageView ivLaunchIcon;
    private TextView tvLaunchText;
    private ProgressBar launchProgress;
    private MemberNomineeAdapter nomineeAdapter;

    public static CreatePollFragment newInstance() {
        return new CreatePollFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_poll, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupBackButton(view);
        setupRoleCards();
        setupSearch(view);
        setupLaunchButton(view);

        // Guard: member-only — disable launch for non-admins
        if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
            view.findViewById(R.id.btnLaunch).setOnClickListener(v ->
                    Toast.makeText(getContext(), "Only admins can create polls", Toast.LENGTH_SHORT).show());
        }

        // Observe members from shared ViewModel
        com.example.save.ui.viewmodels.MembersViewModel membersViewModel =
                new androidx.lifecycle.ViewModelProvider(requireActivity())
                        .get(com.example.save.ui.viewmodels.MembersViewModel.class);
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                nomineeAdapter.setMembers(members);
            }
        });
    }

    private void initViews(View view) {
        cardAdmin = view.findViewById(R.id.cardAdmin);
        cardTreasurer = view.findViewById(R.id.cardTreasurer);
        badgeAdminSelected = view.findViewById(R.id.badgeAdminSelected);
        badgeTreasurerSelected = view.findViewById(R.id.badgeTreasurerSelected);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        chipSecretary = view.findViewById(R.id.chipSecretary);
        chipModerator = view.findViewById(R.id.chipModerator);
        chipCustom = view.findViewById(R.id.chipCustom);
        rvNominees = view.findViewById(R.id.rvNominees);
        tvNoMembers = view.findViewById(R.id.tvNoMembers);
        ivLaunchIcon = view.findViewById(R.id.ivLaunchIcon);
        tvLaunchText = view.findViewById(R.id.tvLaunchText);
        launchProgress = view.findViewById(R.id.launchProgress);

        nomineeAdapter = new MemberNomineeAdapter();
        rvNominees.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNominees.setAdapter(nomineeAdapter);
        rvNominees.setNestedScrollingEnabled(false);
    }

    private void setupBackButton(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }
    }

    // ── Role card/chip selection ──────────────────────────────────────────────

    private void setupRoleCards() {
        cardAdmin.setOnClickListener(v -> {
            selectedRole = "Admin";
            selectedChipRole = null;
            pulseCard(cardAdmin);
            updateRoleUI();
        });
        cardTreasurer.setOnClickListener(v -> {
            selectedRole = "Treasurer";
            selectedChipRole = null;
            pulseCard(cardTreasurer);
            updateRoleUI();
        });

        chipSecretary.setOnClickListener(v -> selectChipRole("Secretary"));
        chipModerator.setOnClickListener(v -> selectChipRole("Moderator"));
        chipCustom.setOnClickListener(v -> openCustomRoleDialog());

        updateRoleUI();
    }

    private void selectChipRole(String role) {
        selectedRole = role;
        selectedChipRole = role;
        updateRoleUI();
    }

    private void openCustomRoleDialog() {
        if (getContext() == null) return;
        EditText input = new EditText(getContext());
        input.setHint("Enter role name");
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(getContext())
                .setTitle("Custom Role")
                .setView(input)
                .setPositiveButton("Set", (dialog, which) -> {
                    String custom = input.getText().toString().trim();
                    if (!custom.isEmpty()) {
                        chipCustom.setText(custom);
                        selectedChipRole = custom;
                        selectedRole = custom;
                        updateRoleUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateRoleUI() {
        boolean adminSelected = "Admin".equals(selectedRole);
        boolean treasurerSelected = "Treasurer".equals(selectedRole);
        boolean secretarySelected = "Secretary".equals(selectedRole);
        boolean moderatorSelected = "Moderator".equals(selectedRole);
        boolean chipCustomSelected = selectedChipRole != null
                && !"Secretary".equals(selectedChipRole)
                && !"Moderator".equals(selectedChipRole);

        // Role cards
        cardAdmin.setBackgroundResource(adminSelected
                ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card_unselected);
        badgeAdminSelected.setVisibility(adminSelected ? View.VISIBLE : View.GONE);
        cardTreasurer.setBackgroundResource(treasurerSelected
                ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card_unselected);
        badgeTreasurerSelected.setVisibility(treasurerSelected ? View.VISIBLE : View.GONE);

        // Chips — selected state uses bg_pill_light_blue + brand_blue text
        applyChipState(chipSecretary, secretarySelected);
        applyChipState(chipModerator, moderatorSelected);
        applyChipState(chipCustom, chipCustomSelected && selectedChipRole != null
                && selectedRole.equals(selectedChipRole)
                && !"Secretary".equals(selectedChipRole)
                && !"Moderator".equals(selectedChipRole));
    }

    private void applyChipState(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_pill_light_blue : R.drawable.bg_pill_white_border);
        chip.setTextColor(selected ? 0xFF2563EB : 0xFF64748B);
    }

    private void pulseCard(View card) {
        ObjectAnimator sx = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy);
        set.setDuration(200);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private void setupSearch(View view) {
        EditText etSearch = view.findViewById(R.id.etSearchMembers);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    nomineeAdapter.filter(s != null ? s.toString() : "");
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    // ── Selected count ────────────────────────────────────────────────────────

    void updateSelectedCount() {
        int count = nomineeAdapter.getSelectedCount();
        tvSelectedCount.setText(count + " selected");
        tvSelectedCount.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);

        boolean hasResults = nomineeAdapter.getItemCount() > 0;
        rvNominees.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        tvNoMembers.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }

    // ── Launch button ─────────────────────────────────────────────────────────

    private void setupLaunchButton(View view) {
        // Guard for member activity is set in onViewCreated — only wire up real logic for admin
        if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
            view.findViewById(R.id.btnLaunch).setOnClickListener(v -> launchPoll(v));
        }
    }

    private void launchPoll(View btnLaunch) {
        ArrayList<String> selectedIds = nomineeAdapter.getSelectedMemberIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(getContext(), "Please nominate at least one member", Toast.LENGTH_SHORT).show();
            return;
        }

        setLaunchLoading(true);

        PollCreateRequest request = new PollCreateRequest(selectedRole, selectedIds);
        RetrofitClient.getClient(requireContext())
                .create(com.example.save.data.network.ApiService.class)
                .createPoll(request)
                .enqueue(new Callback<Poll>() {
                    @Override
                    public void onResponse(@NonNull Call<Poll> call, @NonNull Response<Poll> response) {
                        if (!isAdded()) return;
                        setLaunchLoading(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Poll launched! Members notified. Expires in 24 hours.",
                                    Toast.LENGTH_LONG).show();
                            if (getActivity() != null) getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getContext(),
                                    "Failed to create poll. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Poll> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setLaunchLoading(false);
                        Toast.makeText(getContext(), "Network error. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLaunchLoading(boolean loading) {
        View btnLaunch = getView() != null ? getView().findViewById(R.id.btnLaunch) : null;
        if (btnLaunch != null) btnLaunch.setEnabled(!loading);
        if (launchProgress != null) launchProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (ivLaunchIcon != null) ivLaunchIcon.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (tvLaunchText != null) tvLaunchText.setText(loading ? "Launching…" : "Launch Nomination Poll");
    }

    // ── Static helpers ────────────────────────────────────────────────────────

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // ── Inner adapter ─────────────────────────────────────────────────────────

    class MemberNomineeAdapter extends RecyclerView.Adapter<MemberNomineeAdapter.VH> {

        private List<Member> allMembers = new ArrayList<>();
        private List<Member> filtered = new ArrayList<>();
        private final Set<String> selectedIds = new HashSet<>();

        void setMembers(List<Member> members) {
            allMembers = new ArrayList<>(members);
            filter("");
        }

        void filter(String query) {
            filtered.clear();
            if (query == null || query.trim().isEmpty()) {
                filtered.addAll(allMembers);
            } else {
                String q = query.toLowerCase().trim();
                for (Member m : allMembers) {
                    if (m.getName() != null && m.getName().toLowerCase().contains(q)) {
                        filtered.add(m);
                    }
                }
            }
            notifyDataSetChanged();
            updateSelectedCount();
        }

        int getSelectedCount() {
            return selectedIds.size();
        }

        ArrayList<String> getSelectedMemberIds() {
            return new ArrayList<>(selectedIds);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member_nominate, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Member m = filtered.get(pos);
            boolean selected = m.getId() != null && selectedIds.contains(m.getId());

            h.tvInitials.setText(initials(m.getName()));
            h.tvMemberName.setText(m.getName());
            h.tvMemberRole.setText(m.getRole() != null ? m.getRole() : "Member");
            h.ivCheck.setVisibility(selected ? View.VISIBLE : View.GONE);
            h.btnSelect.setText(selected ? "Selected" : "Select");
            h.btnSelect.setBackgroundResource(selected
                    ? R.drawable.bg_btn_member_selected : R.drawable.bg_btn_member_unselected);
            h.btnSelect.setTextColor(selected ? 0xFF2563EB : 0xFF475569);

            View.OnClickListener toggle = v -> {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                Member clicked = filtered.get(adapterPos);
                if (clicked.getId() == null) return;
                if (selectedIds.contains(clicked.getId())) {
                    selectedIds.remove(clicked.getId());
                } else {
                    selectedIds.add(clicked.getId());
                }
                notifyItemChanged(adapterPos);
                updateSelectedCount();
            };
            h.itemView.setOnClickListener(toggle);
            h.btnSelect.setOnClickListener(toggle);
        }

        @Override
        public int getItemCount() {
            return filtered.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvInitials, tvMemberName, tvMemberRole, btnSelect;
            View ivCheck;

            VH(View v) {
                super(v);
                tvInitials = v.findViewById(R.id.tvInitials);
                tvMemberName = v.findViewById(R.id.tvMemberName);
                tvMemberRole = v.findViewById(R.id.tvMemberRole);
                btnSelect = v.findViewById(R.id.btnSelect);
                ivCheck = v.findViewById(R.id.ivCheck);
            }
        }
    }
}
