package com.example.save.ui.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.data.models.Poll;
import com.example.save.data.network.PollCreateRequest;
import com.example.save.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePollFragment extends Fragment {

    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_TREASURER = 1;
    private int selectedRole = ROLE_ADMIN;

    private boolean[] memberSelected;
    private View cardAdmin, cardTreasurer;
    private View badgeAdminSelected, badgeTreasurerSelected;
    private View[] checkViews;
    private TextView[] btnViews;
    private View[] rowViews;
    private TextView tvSelectedCount;
    private TextView chipSecretary, chipModerator, chipCustom;
    private TextView tvJordanName, tvJordanRole, tvJordanAvatar;
    private TextView tvAlexName, tvAlexRole, tvAlexAvatar;
    private TextView tvSarahName, tvSarahRole, tvSarahAvatar;
    private TextView tvMarcusName, tvMarcusRole, tvMarcusAvatar;
    private java.util.List<com.example.save.data.models.Member> activeMembers = new java.util.ArrayList<>();

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

        // Guard: members-only — admin check
        if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
            View btnLaunch = view.findViewById(R.id.btnLaunch);
            if (btnLaunch != null) {
                btnLaunch.setOnClickListener(v ->
                        Toast.makeText(getContext(), "Only admins can create polls", Toast.LENGTH_SHORT).show());
            }
        }

        memberSelected = new boolean[]{true, true, false, false};

        initViews(view);
        setupRoleCards();
        setupMemberRows(view);
        setupLaunchButton(view);

        com.example.save.ui.viewmodels.MembersViewModel membersViewModel =
                new androidx.lifecycle.ViewModelProvider(requireActivity())
                        .get(com.example.save.ui.viewmodels.MembersViewModel.class);
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null && !members.isEmpty()) {
                activeMembers = new java.util.ArrayList<>(members);
                memberSelected = new boolean[Math.min(activeMembers.size(), 4)];
                java.util.Arrays.fill(memberSelected, false);
                if (memberSelected.length > 0) memberSelected[0] = true;
                if (memberSelected.length > 1) memberSelected[1] = true;
                bindRealMembers();
                refreshMemberUI();
            }
        });
    }

    private void initViews(View view) {
        cardAdmin = view.findViewById(R.id.cardAdmin);
        cardTreasurer = view.findViewById(R.id.cardTreasurer);
        badgeAdminSelected = view.findViewById(R.id.badgeAdminSelected);
        badgeTreasurerSelected = view.findViewById(R.id.badgeTreasurerSelected);

        rowViews = new View[]{
                view.findViewById(R.id.rowJordan),
                view.findViewById(R.id.rowAlex),
                view.findViewById(R.id.rowSarah),
                view.findViewById(R.id.rowMarcus)
        };
        checkViews = new View[]{
                view.findViewById(R.id.checkJordan),
                view.findViewById(R.id.checkAlex),
                view.findViewById(R.id.checkSarah),
                view.findViewById(R.id.checkMarcus)
        };
        btnViews = new TextView[]{
                view.findViewById(R.id.btnJordan),
                view.findViewById(R.id.btnAlex),
                view.findViewById(R.id.btnSarah),
                view.findViewById(R.id.btnMarcus)
        };

        tvJordanName = view.findViewById(R.id.tvJordanName);
        tvJordanRole = view.findViewById(R.id.tvJordanRole);
        tvJordanAvatar = view.findViewById(R.id.tvJordanAvatar);
        tvAlexName = view.findViewById(R.id.tvAlexName);
        tvAlexRole = view.findViewById(R.id.tvAlexRole);
        tvAlexAvatar = view.findViewById(R.id.tvAlexAvatar);
        tvSarahName = view.findViewById(R.id.tvSarahName);
        tvSarahRole = view.findViewById(R.id.tvSarahRole);
        tvSarahAvatar = view.findViewById(R.id.tvSarahAvatar);
        tvMarcusName = view.findViewById(R.id.tvMarcusName);
        tvMarcusRole = view.findViewById(R.id.tvMarcusRole);
        tvMarcusAvatar = view.findViewById(R.id.tvMarcusAvatar);

        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        chipSecretary = view.findViewById(R.id.chipSecretary);
        chipModerator = view.findViewById(R.id.chipModerator);
        chipCustom = view.findViewById(R.id.chipCustom);
    }

    private void bindRealMembers() {
        TextView[][] nameRoleAvatar = {
                {tvJordanName, tvJordanRole, tvJordanAvatar},
                {tvAlexName, tvAlexRole, tvAlexAvatar},
                {tvSarahName, tvSarahRole, tvSarahAvatar},
                {tvMarcusName, tvMarcusRole, tvMarcusAvatar}
        };
        for (int i = 0; i < rowViews.length; i++) {
            if (i < activeMembers.size()) {
                rowViews[i].setVisibility(View.VISIBLE);
                com.example.save.data.models.Member m = activeMembers.get(i);
                nameRoleAvatar[i][0].setText(m.getName());
                nameRoleAvatar[i][1].setText(m.getRole());
                nameRoleAvatar[i][2].setText(getInitials(m.getName()));
            } else {
                rowViews[i].setVisibility(View.GONE);
            }
        }
    }

    // ── Role card selection ───────────────────────────────────────────────────

    private void setupRoleCards() {
        cardAdmin.setOnClickListener(v -> selectRole(ROLE_ADMIN));
        cardTreasurer.setOnClickListener(v -> selectRole(ROLE_TREASURER));
        chipSecretary.setOnClickListener(v -> selectRoleChip("Secretary"));
        chipModerator.setOnClickListener(v -> selectRoleChip("Moderator"));
        chipCustom.setOnClickListener(v -> selectRoleChip("Custom"));
        updateRoleUI();
    }

    private void selectRole(int role) {
        selectedRole = role;
        pulseCard(role == ROLE_ADMIN ? cardAdmin : cardTreasurer);
        updateRoleUI();
    }

    private void selectRoleChip(String roleName) {
        Toast.makeText(getContext(), roleName + " role selected", Toast.LENGTH_SHORT).show();
    }

    private void updateRoleUI() {
        cardAdmin.setBackgroundResource(selectedRole == ROLE_ADMIN
                ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card_unselected);
        badgeAdminSelected.setVisibility(selectedRole == ROLE_ADMIN ? View.VISIBLE : View.GONE);
        cardTreasurer.setBackgroundResource(selectedRole == ROLE_TREASURER
                ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card_unselected);
        badgeTreasurerSelected.setVisibility(selectedRole == ROLE_TREASURER ? View.VISIBLE : View.GONE);
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

    // ── Member row selection ──────────────────────────────────────────────────

    private void setupMemberRows(View view) {
        for (int i = 0; i < rowViews.length; i++) {
            final int idx = i;
            rowViews[i].setOnClickListener(v -> toggleMember(idx));
            btnViews[i].setOnClickListener(v -> toggleMember(idx));
        }
        refreshMemberUI();
    }

    private void toggleMember(int index) {
        if (index >= memberSelected.length) return;
        memberSelected[index] = !memberSelected[index];
        refreshMemberUI();
    }

    private void refreshMemberUI() {
        for (int i = 0; i < Math.min(memberSelected.length, btnViews.length); i++) {
            boolean sel = memberSelected[i];
            checkViews[i].setVisibility(sel ? View.VISIBLE : View.GONE);
            btnViews[i].setText(sel ? "Selected" : "Select");
            btnViews[i].setBackgroundResource(sel
                    ? R.drawable.bg_btn_member_selected : R.drawable.bg_btn_member_unselected);
            btnViews[i].setTextColor(sel ? 0xFF2563EB : 0xFF475569);
        }
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        int count = 0;
        for (boolean s : memberSelected) if (s) count++;
        tvSelectedCount.setText(count + " selected");
        tvSelectedCount.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    // ── Launch button ─────────────────────────────────────────────────────────

    private void setupLaunchButton(View view) {
        view.findViewById(R.id.btnLaunch).setOnClickListener(v -> {
            List<String> selectedIds = new ArrayList<>();
            for (int i = 0; i < memberSelected.length; i++) {
                if (memberSelected[i] && i < activeMembers.size()) {
                    selectedIds.add(activeMembers.get(i).getId());
                }
            }

            if (selectedIds.isEmpty()) {
                Toast.makeText(getContext(), "Please nominate at least one member", Toast.LENGTH_SHORT).show();
                return;
            }

            String roleName = selectedRole == ROLE_ADMIN ? "Admin" : "Treasurer";
            v.setEnabled(false);

            String role = roleName;
            PollCreateRequest request = new PollCreateRequest(role, selectedIds);

            RetrofitClient.getClient(requireContext())
                    .create(com.example.save.data.network.ApiService.class)
                    .createPoll(request)
                    .enqueue(new Callback<Poll>() {
                        @Override
                        public void onResponse(@NonNull Call<Poll> call, @NonNull Response<Poll> response) {
                            if (!isAdded()) return;
                            v.setEnabled(true);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(),
                                        "Poll launched! Members notified. Expires in 24 hours.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(),
                                        "Failed to create poll. Try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Poll> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            v.setEnabled(true);
                            Toast.makeText(getContext(), "Network error. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
