package com.example.save.ui.fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.databinding.DialogAddMemberBinding;
import com.example.save.databinding.FragmentMembersBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.ValidationUtils;
import com.example.save.ui.adapters.MemberAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.save.ui.viewmodels.LoansViewModel;
import com.example.save.data.models.Loan;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private MembersViewModel viewModel;
    private MemberAdapter adapter;
    private List<Member> currentMembersList = new ArrayList<>();
    private String currentTab = "All";
    private String searchQuery = "";
    private boolean isSearching = false;

    // Animation views
    private LottieAnimationView successAnimation;
    private View animationOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);

        // Initialize ViewModels
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        LoansViewModel loansViewModel = new ViewModelProvider(requireActivity()).get(LoansViewModel.class);

        // Check permissions
        String userRole = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserRole();
        boolean isAdmin = "admin".equalsIgnoreCase(userRole) || "Administrator".equalsIgnoreCase(userRole);
        
        // observeViewModel(); // Assuming this is called later or elsewhere
        
        setupRecyclerView(isAdmin);
        observeViewModel();
        setupSearchView();

        // Sync data with backend
        viewModel.syncMembers();

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        String groupName = requireContext()
                .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                .getString("group_name", "YOUR GROUP");
        binding.tvGroupSubtitle.setText(groupName.toUpperCase());

        // Check for auto-open argument
        if (getArguments() != null && getArguments().getBoolean("SHOW_ADD_DIALOG", false)) {
            binding.getRoot().post(this::showAddMemberDialog);
        }


        // Initialize animation views
        successAnimation = binding.successAnimation;
        animationOverlay = binding.animationOverlay;

        if (isAdmin) {
            binding.btnAddMemberCard.setOnClickListener(v -> showAddMemberDialog());
        } else {
            binding.btnAddMemberCard.setVisibility(View.GONE);
        }

        // Make Poll Card Click (Today's Work Restoration)
        binding.cardMakePoll.setOnClickListener(v -> {
            if (getActivity() != null) {
                Fragment pollFragment = PollsFragment.newInstance();
                if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                    ((com.example.save.ui.activities.AdminMainActivity) getActivity()).loadFragment(pollFragment, true);
                } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                    ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(pollFragment, true);
                }
            }
        });

        startEntryAnimations();

        return binding.getRoot();
    }

    private void startEntryAnimations() {
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_fade);
        android.view.animation.Animation slideDown = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.fragment_enter); // Using existing fragment_enter for slide down feel

        // 1. Header slides down
        binding.headerLayout.startAnimation(slideDown);

        // 2. Search card slides up after delay
        slideUp.setStartOffset(200);
        binding.searchCard.startAnimation(slideUp);

        // 3. Tabs slide up after more delay
        android.view.animation.Animation tabsSlide = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_fade);
        tabsSlide.setStartOffset(400);
        // Find the LinearLayout containing tabs (line 138 in fragment_members.xml)
        // It's the child after searchCard. Since it has no ID, we'll animate its parent's child or just add an ID.
        // Wait, line 138 has no ID. I'll animate membersRecyclerView instead or find the view.
        // Actually, I'll just animate searchCard and membersRecyclerView.
        
        // 4. RecyclerView Layout Animation
        android.view.animation.Animation itemAnim = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_fade);
        android.view.animation.LayoutAnimationController controller = new android.view.animation.LayoutAnimationController(itemAnim);
        controller.setDelay(0.15f);
        controller.setOrder(android.view.animation.LayoutAnimationController.ORDER_NORMAL);
        binding.membersRecyclerView.setLayoutAnimation(controller);
    }


    private void filterAndApply() {
        if (currentMembersList == null || adapter == null)
            return;

        List<Member> filtered = new ArrayList<>();
        for (Member m : currentMembersList) {
            if (currentTab.equals("All")) {
                filtered.add(m);
            } else if (currentTab.equals("Active")) {
                if (m.isActive())
                    filtered.add(m);
            } else if (currentTab.equals("Inactive")) {
                if (!m.isActive())
                    filtered.add(m);
            }
        }
        adapter.updateList(filtered);
    }

    private void setupSearchView() {
        binding.etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    isSearching = false;
                    filterAndApply();
                } else {
                    isSearching = true;
                    // Search members
                    viewModel.searchMembers(query).observe(getViewLifecycleOwner(), members -> {
                        if (members != null && adapter != null) {
                            adapter.updateList(members);
                            binding.membersRecyclerView.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
                            binding.emptyStateLayout.setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadMembers() {
        // Show loading initially if not refreshing
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // Trigger backend sync
        viewModel.refreshMembers((success, message) -> {
        });

        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            // Hide loading when data arrives
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }

            if (members != null && adapter != null) {
                currentMembersList = members;
                
                if (isSearching) {
                    // Re-apply current search query to fresh data
                    String query = binding.etSearchMember.getText().toString().trim();
                    if (!query.isEmpty()) {
                        List<Member> filteredResults = new ArrayList<>();
                        String lowerQuery = query.toLowerCase();
                        for (Member m : members) {
                            if (m.getName().toLowerCase().contains(lowerQuery) || 
                                (m.getPhone() != null && m.getPhone().contains(lowerQuery))) {
                                filteredResults.add(m);
                            }
                        }
                        adapter.updateList(filteredResults);
                    } else {
                        filterAndApply();
                    }
                } else {
                    filterAndApply();
                }

                // Update summary strip (counts reflect the full group, not the filtered view)
                updateSummary(members);

                binding.membersRecyclerView.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                binding.emptyStateLayout.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void observeViewModel() {
        loadMembers();
    }

    private void setupRecyclerView(boolean isAdmin) {
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MemberAdapter(new MemberAdapter.OnMemberClickListener() {
            @Override
            public void onMemberClick(Member member) {
                showProfileDialog(member);
            }

            @Override
            public void onMoreActionsClick(View view, Member member, int position) {
                if (isAdmin) {
                    showPopupMenu(view, member, position);
                }
            }

            @Override
            public void onDeleteClick(Member member, int position) {
                if (isAdmin) {
                    showRemoveMemberConfirmation(member);
                }
            }

            @Override
            public void onPromoteDemoteClick(Member member, int position) {
                if (isAdmin) {
                    boolean isAdminMember = member.getRole().equalsIgnoreCase("Administrator") || member.getRole().equalsIgnoreCase("Admin");
                    member.setRole(isAdminMember ? "Member" : "Administrator");
                    viewModel.updateMember(position, member);
                }
            }
        });
        adapter.setAdmin(isAdmin);
        binding.membersRecyclerView.setAdapter(adapter);
    }

    /** Populates the blue summary strip with live group counts. */
    private void updateSummary(List<Member> members) {
        if (binding == null || members == null) return;
        int total = members.size();
        int active = 0;
        int admins = 0;
        for (Member m : members) {
            if (m.isActive()) active++;
            String role = m.getRole();
            if (role != null && (role.equalsIgnoreCase("Administrator") || role.equalsIgnoreCase("Admin"))) {
                admins++;
            }
        }
        binding.tvStatTotal.setText(String.valueOf(total));
        binding.tvStatActive.setText(String.valueOf(active));
        binding.tvStatAdmins.setText(String.valueOf(admins));
    }

    public void showAddMemberDialog() {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogAddMemberBinding dialogBinding = DialogAddMemberBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());

        final String[] selectedRole = {"Member"};
        updateRoleUI(dialogBinding, true); // Initialize with Member selected

        // Role Selection Logic
        dialogBinding.cardRoleMember.setOnClickListener(v -> {
            selectedRole[0] = "Member";
            updateRoleUI(dialogBinding, true);
        });

        dialogBinding.cardRoleAdmin.setOnClickListener(v -> {
            selectedRole[0] = "Administrator";
            updateRoleUI(dialogBinding, false);
        });


        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        
        // Ensure dialog takes proper width
        int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.setOnDismissListener(d -> {
            if (getArguments() != null && getArguments().getBoolean("SHOW_ADD_DIALOG", false)) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        dialogBinding.btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnCreateMember.setOnClickListener(v -> {
            String name = dialogBinding.etMemberName.getText().toString().trim();
            String phoneInput = dialogBinding.etMemberPhone.getText().toString().trim();
            String pin = dialogBinding.etMemberPin.getText().toString().trim();

            // Prepend +256 if it's just the local number; use a final var for lambda capture
            final String phone = com.example.save.utils.ValidationUtils.normalizePhone(phoneInput);
            String email = null; // Email removed from UI
            String role = selectedRole[0];

            if (!com.example.save.utils.ValidationUtils.isNotEmpty(name)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberName, "Name is required");
                return;
            }

            if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberPhone, "Invalid phone number");
                return;
            }

            Member newMember = new Member(name, role, true, phone);
            newMember.setId(java.util.UUID.randomUUID().toString());
            newMember.setPassword(""); // Member will set PIN during onboarding
            newMember.setFirstLogin(true);

            dialogBinding.btnCreateMember.setEnabled(false);
            dialogBinding.btnCreateMember.setText("Creating...");

            viewModel.addMember(newMember, (boolean success, String message, String generatedOtp) -> {
                if (getActivity() == null || getContext() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (dialog.isShowing()) {
                        dialogBinding.btnCreateMember.setEnabled(true);
                        dialogBinding.btnCreateMember.setText("CONTINUE TO PERMISSIONS");
                    }

                    if (success) {
                        dialog.dismiss();
                        // Refresh members list to show the newly added member
                        viewModel.syncMembers();
                        // Directly navigate to success screen without OTP/Invite dialog
                        navigateToSuccess(newMember.getName());
                    } else {
                        Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void updateRoleUI(DialogAddMemberBinding binding, boolean isMemberSelected) {
        Context context = binding.getRoot().getContext();
        int brandBlue = ContextCompat.getColor(context, R.color.brand_blue);
        int surfaceColor = ContextCompat.getColor(context, R.color.card_surface);
        int inputBgColor = ContextCompat.getColor(context, R.color.v_input_bg);
        int textDark = ContextCompat.getColor(context, R.color.v_text_dark);
        int textMuted = ContextCompat.getColor(context, R.color.v_text_muted);
        int dividerColor = ContextCompat.getColor(context, R.color.divider_color);

        float density = context.getResources().getDisplayMetrics().density;

        // Member card — blue stroke + white bg when selected; grey bg + no stroke when not
        binding.cardRoleMember.setCardBackgroundColor(isMemberSelected ? surfaceColor : inputBgColor);
        binding.cardRoleMember.setStrokeColor(isMemberSelected ? brandBlue : dividerColor);
        binding.cardRoleMember.setStrokeWidth(isMemberSelected ? (int)(2 * density) : (int)(1 * density));
        binding.cardRoleMember.setCardElevation(isMemberSelected ? 2f : 0f);
        binding.checkMember.setVisibility(isMemberSelected ? View.VISIBLE : View.GONE);
        binding.titleMember.setTextColor(isMemberSelected ? brandBlue : textDark);

        // Admin card — blue stroke + white bg when selected; grey bg + no stroke when not
        binding.cardRoleAdmin.setCardBackgroundColor(!isMemberSelected ? surfaceColor : inputBgColor);
        binding.cardRoleAdmin.setStrokeColor(!isMemberSelected ? brandBlue : dividerColor);
        binding.cardRoleAdmin.setStrokeWidth(!isMemberSelected ? (int)(2 * density) : (int)(1 * density));
        binding.cardRoleAdmin.setCardElevation(!isMemberSelected ? 2f : 0f);
        binding.checkAdmin.setVisibility(!isMemberSelected ? View.VISIBLE : View.GONE);
        binding.titleAdmin.setTextColor(!isMemberSelected ? brandBlue : textDark);
    }

    private void showSuccessAnimation(String name, String otp, String phone) {
        if (successAnimation != null && animationOverlay != null) {
            animationOverlay.setVisibility(View.VISIBLE);
            successAnimation.setProgress(0f);
            successAnimation.playAnimation();

            new android.os.Handler().postDelayed(() -> {
                animationOverlay.setVisibility(View.GONE);
                showInviteDialog(name, phone);
            }, 2000);
        } else {
            showInviteDialog(name, phone);
        }
    }

    private void navigateToSuccess(String memberName) {
        if (getView() == null) return;
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(((ViewGroup) getView().getParent()).getId(), MemberSuccessFragment.newInstance(memberName))
                .addToBackStack(null)
                .commit();
    }

    /**
     * Shows the member invite dialog after admin adds a new member.
     * No PIN/OTP is generated — the member self-registers via Firebase OTP
     * using the OtpRequestActivity onboarding flow.
     */
    private void showInviteDialog(String memberName, String phone) {
        if (getContext() == null) return;

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs",
                Context.MODE_PRIVATE);
        String groupName = prefs.getString("group_name", "Your Group");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        com.example.save.databinding.DialogMemberCredentialsBinding credBinding =
                com.example.save.databinding.DialogMemberCredentialsBinding
                        .inflate(LayoutInflater.from(getContext()));
        builder.setView(credBinding.getRoot());

        credBinding.tvCredGroupName.setText(groupName);
        credBinding.tvCredMemberName.setText(memberName);
        credBinding.tvCredPhone.setText(phone);
        // tvCredOTP is hidden in layout — leave blank

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.95);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Share message — tells the member exactly how to onboard
        String shareMessage = String.format(
                "🎉 You've been added to *%s* on Save!\n\n"
                + "To join:\n"
                + "1. Download the Save app\n"
                + "2. Tap \"New member? Join Your Group\"\n"
                + "3. Enter group name: *%s*\n"
                + "4. Enter your phone: *%s*\n"
                + "5. Verify via OTP and set your PIN\n\n"
                + "See you inside! 🚀",
                groupName, groupName, phone);

        credBinding.btnShareDetails.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(intent, "Share invite via"));
        });

        credBinding.btnDone.setOnClickListener(v -> {
            dialog.dismiss();
            if (getView() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(((ViewGroup) getView().getParent()).getId(), MemberSuccessFragment.newInstance(memberName))
                        .addToBackStack(null)
                        .commit();
            }
        });
        credBinding.btnCloseCredentials.setOnClickListener(v -> dialog.dismiss());
    }

    private void showProfileDialog(Member member) {
        if (member.getPhone() == null)
            return;
        getParentFragmentManager().beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(),
                        com.example.save.ui.fragments.MemberProfileFragment.newInstance(member.getPhone()))
                .addToBackStack(null).commit();
    }

    private void showPopupMenu(View view, Member member, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        boolean isAdmin = member.getRole().equalsIgnoreCase("Administrator")
                || member.getRole().equalsIgnoreCase("Admin");
        if (isAdmin)
            popup.getMenu().add(0, 1, 0, "Demote to Member");
        else
            popup.getMenu().add(0, 1, 0, "Promote to Admin");
        popup.getMenu().add(0, 2, 0, "Reset Password");
        if (member.isActive())
            popup.getMenu().add(0, 4, 0, "Suspend Member");
        else
            popup.getMenu().add(0, 4, 0, "Activate Member");
        popup.getMenu().add(0, 3, 0, "Remove Member");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                member.setRole(isAdmin ? "Member" : "Administrator");
                viewModel.updateMember(position, member);
                return true;
            } else if (item.getItemId() == 2) {
                showResetPasswordConfirmation(member);
                return true;
            } else if (item.getItemId() == 3) {
                showRemoveMemberConfirmation(member);
                return true;
            } else if (item.getItemId() == 4) {
                member.setActive(!member.isActive());
                viewModel.updateMember(position, member);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showResetPasswordConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext()).setTitle("Reset PIN")
                .setMessage(member.getName() + " will need to use the \"Forgot PIN\" flow in the app to reset their PIN via OTP.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showRemoveMemberConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to completely remove " + member.getName() + "? This action cannot be undone.")
                .setPositiveButton("Remove", (d, w) -> {
                    viewModel.removeMember(member, (success, message) -> {
                        if (getActivity() == null) return;
                        if (success) {
                            Toast.makeText(getContext(), "Member removed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String formatCurrencyCompact(double amount) {
        if (amount >= 1000000) return String.format(Locale.getDefault(), "UGX %.1fM", amount / 1000000);
        if (amount >= 1000) return String.format(Locale.getDefault(), "UGX %.1fK", amount / 1000);
        return String.format(Locale.getDefault(), "UGX %.0f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
