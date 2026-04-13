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

public class CreatePollFragment extends Fragment {

    // Role selection state
    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_TREASURER = 1;
    private static final int ROLE_SECRETARY = 2;
    private static final int ROLE_MODERATOR = 3;
    private int selectedRole = ROLE_ADMIN;

    // Member selection state
    private boolean jordanSelected = true;
    private boolean alexSelected = true;
    private boolean sarahSelected = false;
    private boolean marcusSelected = false;

    // Views
    private View cardAdmin, cardTreasurer;
    private View badgeAdminSelected, badgeTreasurerSelected;
    private View checkJordan, checkAlex, checkSarah, checkMarcus;
    private TextView btnJordan, btnAlex, btnSarah, btnMarcus;
    private TextView tvSelectedCount;
    private TextView chipSecretary, chipModerator, chipCustom;

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
        setupRoleCards();
        setupMemberRows(view);
        setupLaunchButton(view);
    }

    private void initViews(View view) {
        cardAdmin = view.findViewById(R.id.cardAdmin);
        cardTreasurer = view.findViewById(R.id.cardTreasurer);
        badgeAdminSelected = view.findViewById(R.id.badgeAdminSelected);
        badgeTreasurerSelected = view.findViewById(R.id.badgeTreasurerSelected);

        checkJordan = view.findViewById(R.id.checkJordan);
        checkAlex = view.findViewById(R.id.checkAlex);
        checkSarah = view.findViewById(R.id.checkSarah);
        checkMarcus = view.findViewById(R.id.checkMarcus);

        btnJordan = view.findViewById(R.id.btnJordan);
        btnAlex = view.findViewById(R.id.btnAlex);
        btnSarah = view.findViewById(R.id.btnSarah);
        btnMarcus = view.findViewById(R.id.btnMarcus);

        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        chipSecretary = view.findViewById(R.id.chipSecretary);
        chipModerator = view.findViewById(R.id.chipModerator);
        chipCustom = view.findViewById(R.id.chipCustom);
    }

    // ── Role Card Selection ──────────────────────────────────────────────────

    private void setupRoleCards() {
        cardAdmin.setOnClickListener(v -> selectRole(ROLE_ADMIN));
        cardTreasurer.setOnClickListener(v -> selectRole(ROLE_TREASURER));
        chipSecretary.setOnClickListener(v -> selectRoleChip(chipSecretary, "Secretary"));
        chipModerator.setOnClickListener(v -> selectRoleChip(chipModerator, "Moderator"));
        chipCustom.setOnClickListener(v -> selectRoleChip(chipCustom, "Custom"));

        // Apply initial state
        updateRoleUI();
    }

    private void selectRole(int role) {
        selectedRole = role;
        pulseCard(role == ROLE_ADMIN ? cardAdmin : cardTreasurer);
        updateRoleUI();
    }

    private void selectRoleChip(TextView chip, String roleName) {
        Toast.makeText(getContext(), roleName + " role selected", Toast.LENGTH_SHORT).show();
    }

    private void updateRoleUI() {
        // Admin card
        cardAdmin.setBackgroundResource(selectedRole == ROLE_ADMIN
                ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card_unselected);
        badgeAdminSelected.setVisibility(selectedRole == ROLE_ADMIN ? View.VISIBLE : View.GONE);

        // Treasurer card
        cardTreasurer.setBackgroundResource(selectedRole == ROLE_TREASURER
                ? R.drawable.bg_role_card_selected : R.drawable.bg_role_card_unselected);
        badgeTreasurerSelected.setVisibility(selectedRole == ROLE_TREASURER ? View.VISIBLE : View.GONE);
    }

    private void pulseCard(View card) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(200);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    // ── Member Selection ─────────────────────────────────────────────────────

    private void setupMemberRows(View view) {
        View rowJordan = view.findViewById(R.id.rowJordan);
        View rowAlex = view.findViewById(R.id.rowAlex);
        View rowSarah = view.findViewById(R.id.rowSarah);
        View rowMarcus = view.findViewById(R.id.rowMarcus);

        rowJordan.setOnClickListener(v -> toggleMember(0));
        btnJordan.setOnClickListener(v -> toggleMember(0));

        rowAlex.setOnClickListener(v -> toggleMember(1));
        btnAlex.setOnClickListener(v -> toggleMember(1));

        rowSarah.setOnClickListener(v -> toggleMember(2));
        btnSarah.setOnClickListener(v -> toggleMember(2));

        rowMarcus.setOnClickListener(v -> toggleMember(3));
        btnMarcus.setOnClickListener(v -> toggleMember(3));

        refreshMemberUI();
    }

    private void toggleMember(int index) {
        switch (index) {
            case 0: jordanSelected = !jordanSelected; break;
            case 1: alexSelected = !alexSelected; break;
            case 2: sarahSelected = !sarahSelected; break;
            case 3: marcusSelected = !marcusSelected; break;
        }
        refreshMemberUI();
    }

    private void refreshMemberUI() {
        applyMemberState(jordanSelected, checkJordan, btnJordan);
        applyMemberState(alexSelected, checkAlex, btnAlex);
        applyMemberState(sarahSelected, checkSarah, btnSarah);
        applyMemberState(marcusSelected, checkMarcus, btnMarcus);
        updateSelectedCount();
    }

    private void applyMemberState(boolean selected, View checkBadge, TextView button) {
        checkBadge.setVisibility(selected ? View.VISIBLE : View.GONE);
        button.setText(selected ? "Selected" : "Select");
        button.setBackgroundResource(selected
                ? R.drawable.bg_btn_member_selected : R.drawable.bg_btn_member_unselected);
        button.setTextColor(selected ? 0xFF2563EB : 0xFF475569);
    }

    private void updateSelectedCount() {
        int count = 0;
        if (jordanSelected) count++;
        if (alexSelected) count++;
        if (sarahSelected) count++;
        if (marcusSelected) count++;
        tvSelectedCount.setText(count + " selected");
        tvSelectedCount.setVisibility(count > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    // ── Launch Button ────────────────────────────────────────────────────────

    private void setupLaunchButton(View view) {
        view.findViewById(R.id.btnLaunch).setOnClickListener(v -> {
            int count = 0;
            if (jordanSelected) count++;
            if (alexSelected) count++;
            if (sarahSelected) count++;
            if (marcusSelected) count++;

            if (count == 0) {
                Toast.makeText(getContext(), "Please nominate at least one member", Toast.LENGTH_SHORT).show();
                return;
            }

            String roleName = selectedRole == ROLE_ADMIN ? "Admin"
                    : selectedRole == ROLE_TREASURER ? "Treasurer" : "Role";

            // Copy to final vars for lambda capture
            final int finalCount = count;
            final String finalRoleName = roleName;

            // Animate the button
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction(() ->
                            Toast.makeText(getContext(),
                                    "✅ Nomination Poll for " + finalRoleName + " launched! " + finalCount + " nominee(s) notified.",
                                    Toast.LENGTH_LONG).show()
                    ).start()
            ).start();
        });
    }
}
