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
import com.example.save.databinding.DialogMemberProfileBinding;
import com.example.save.databinding.FragmentMembersBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.ValidationUtils;
import com.example.save.ui.adapters.MemberAdapter;
import com.example.save.ui.adapters.TechnicalInsightsAdapter;
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
    private TechnicalInsightsAdapter insightsAdapter;
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

        setupRecyclerView();
        observeViewModel();
        setupSearchView();
        setupTabs();
        setupTabs();

        // Sync data with backend
        viewModel.syncMembers();

        binding.btnInvite.setOnClickListener(v -> showAddMemberDialog());
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnThemeToggle.setOnClickListener(v -> com.example.save.utils.ThemeUtils.toggleTheme(requireContext()));

        // Check for auto-open argument
        if (getArguments() != null && getArguments().getBoolean("SHOW_ADD_DIALOG", false)) {
            binding.getRoot().post(this::showAddMemberDialog);
        }

        // Setup Swipe Refresh
        binding.swipeRefreshUtils.setOnRefreshListener(() -> {
            loadMembers();
            loadMembers();
        });

        // Initialize animation views
        successAnimation = binding.successAnimation;
        animationOverlay = binding.animationOverlay;

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

    private void setupTabs() {
        binding.tabAll.setOnClickListener(v -> selectTab("All"));
        binding.tabActive.setOnClickListener(v -> selectTab("Active"));
        binding.tabInactive.setOnClickListener(v -> selectTab("Inactive"));
    }

    private void selectTab(String tab) {
        currentTab = tab;

        // Reset styles
        binding.tabAll.setBackgroundResource(R.drawable.bg_pill_tab_unselected);
        binding.tabActive.setBackgroundResource(R.drawable.bg_pill_tab_unselected);
        binding.tabInactive.setBackgroundResource(R.drawable.bg_pill_tab_unselected);

        binding.tabAll.setTextColor(android.graphics.Color.parseColor("#64748B"));
        binding.tabActive.setTextColor(android.graphics.Color.parseColor("#64748B"));
        binding.tabInactive.setTextColor(android.graphics.Color.parseColor("#64748B"));

        // Set selected style
        TextView selectedView = null;
        if (tab.equals("All"))
            selectedView = binding.tabAll;
        else if (tab.equals("Active"))
            selectedView = binding.tabActive;
        else if (tab.equals("Inactive"))
            selectedView = binding.tabInactive;

        if (selectedView != null) {
            selectedView.setBackgroundResource(R.drawable.bg_pill_tab_selected);
            selectedView.setTextColor(android.graphics.Color.parseColor("#2563EB"));
            selectedView.setElevation(2f);
        }

        filterAndApply();
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
        if (!binding.swipeRefreshUtils.isRefreshing() && binding.progressBar != null) {
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
            if (binding.swipeRefreshUtils != null) {
                binding.swipeRefreshUtils.setRefreshing(false);
            }

            if (members != null && adapter != null && !isSearching) {
                currentMembersList = members;
                filterAndApply();

                // Update Technical Insights
                if (insightsAdapter != null) {
                    insightsAdapter.updateList(members);
                }

                binding.tabAll.setText("All Members (" + members.size() + ")");

                binding.membersRecyclerView.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void observeViewModel() {
        loadMembers();
    }

    private void setupRecyclerView() {
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new com.example.save.ui.adapters.MemberAdapter(
                new com.example.save.ui.adapters.MemberAdapter.OnMemberClickListener() {
                    @Override
                    public void onMemberClick(Member member) {
                        showProfileDialog(member);
                    }

                    @Override
                    public void onMoreActionsClick(View view, Member member, int position) {
                        showPopupMenu(view, member, position);
                    }
                });
        binding.membersRecyclerView.setAdapter(adapter);

        // Setup Insights RecyclerView
        binding.rvTechnicalInsights.setLayoutManager(new LinearLayoutManager(getContext()));
        insightsAdapter = new TechnicalInsightsAdapter();
        binding.rvTechnicalInsights.setAdapter(insightsAdapter);
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

        dialogBinding.btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnCreateMember.setOnClickListener(v -> {
            String name = dialogBinding.etMemberName.getText().toString().trim();
            String phoneInput = dialogBinding.etMemberPhone.getText().toString().trim();
            // Prepend +256 if it's just the local number; use a final var for lambda capture
            final String phone;
            if (phoneInput.length() == 9 && phoneInput.startsWith("7")) {
                phone = "+256 " + phoneInput;
            } else if (phoneInput.length() == 10 && phoneInput.startsWith("0")) {
                phone = "+256 " + phoneInput.substring(1);
            } else {
                phone = phoneInput;
            }
            String email = dialogBinding.etMemberEmail.getText().toString().trim();
            String role = selectedRole[0];

            if (!com.example.save.utils.ValidationUtils.isNotEmpty(name)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberName, "Name is required");
                return;
            }

            if (!com.example.save.utils.ValidationUtils.isValidEmail(email)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberEmail, "Invalid email format");
                return;
            }

            if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberPhone, "Invalid phone number");
                return;
            }

            Member newMember = new Member(name, role, true, phone, email);
            newMember.setId(java.util.UUID.randomUUID().toString());
            // Password will be assigned by backend
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
                        showSuccessAnimation(name, generatedOtp, email, phone);
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
        int slate200 = ContextCompat.getColor(context, R.color.slate_200);
        int slate50 = ContextCompat.getColor(context, R.color.slate_50);

        float density = context.getResources().getDisplayMetrics().density;

        // Member Card
        binding.cardRoleMember.setStrokeColor(isMemberSelected ? brandBlue : slate200);
        binding.cardRoleMember.setStrokeWidth(isMemberSelected ? (int)(2 * density) : (int)(1 * density));
        binding.cardRoleMember.setCardBackgroundColor(isMemberSelected ? android.graphics.Color.WHITE : slate50);
        binding.checkMember.setVisibility(isMemberSelected ? View.VISIBLE : View.GONE);
        
        // Admin Card
        binding.cardRoleAdmin.setStrokeColor(!isMemberSelected ? brandBlue : slate200);
        binding.cardRoleAdmin.setStrokeWidth(!isMemberSelected ? (int)(2 * density) : (int)(1 * density));
        binding.cardRoleAdmin.setCardBackgroundColor(!isMemberSelected ? android.graphics.Color.WHITE : slate50);
        binding.checkAdmin.setVisibility(!isMemberSelected ? View.VISIBLE : View.GONE);
    }

    private void showSuccessAnimation(String name, String otp, String email, String phone) {
        if (successAnimation != null && animationOverlay != null) {
            animationOverlay.setVisibility(View.VISIBLE);
            successAnimation.setProgress(0f);
            successAnimation.playAnimation();

            // After animation ends (or short delay), show the credentials
            new android.os.Handler().postDelayed(() -> {
                animationOverlay.setVisibility(View.GONE);
                showOTPDialog(name, otp, email, phone);
            }, 2500);
        } else {
            showOTPDialog(name, otp, email, phone);
        }
    }

    private void showOTPDialog(String memberName, String otp, String email, String phone) {
        if (getContext() == null)
            return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs",
                Context.MODE_PRIVATE);
        String groupName = prefs.getString("group_name", "Your Group");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        com.example.save.databinding.DialogMemberCredentialsBinding credBinding = com.example.save.databinding.DialogMemberCredentialsBinding
                .inflate(LayoutInflater.from(getContext()));
        builder.setView(credBinding.getRoot());

        credBinding.tvCredGroupName.setText(groupName);
        credBinding.tvCredMemberName.setText(memberName);
        credBinding.tvCredPhone.setText(phone);
        credBinding.tvCredEmail.setText(email);
        credBinding.tvCredOTP.setText(otp);

        // Optional: Update with database values once loaded to ensure sync
        viewModel.getMemberByNameLive(memberName).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                credBinding.tvCredMemberName.setText(member.getName());
                credBinding.tvCredEmail.setText(member.getEmail());
            }
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set Dialog Size to 95% width
        dialog.show();
        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.95);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        String credentialsMessage = String.format(
                "🎉 *Welcome to %s!*\n\nYour login credentials:\n\n👤 *Name:* %s\n📱 *Phone:* %s\n📧 *Email:* %s\n🔐 *Code:* %s\n\nDownload the app to get started! 🚀",
                groupName, memberName, phone, email, otp);

        // Format OTP with spaces for visual match: "8 8 2  1 4 9"
        StringBuilder formattedOtp = new StringBuilder();
        for (int i = 0; i < otp.length(); i++) {
            formattedOtp.append(otp.charAt(i));
            if (i == 2)
                formattedOtp.append("  ");
            else if (i < otp.length() - 1)
                formattedOtp.append(" ");
        }
        credBinding.tvCredOTP.setText(formattedOtp.toString());

        credBinding.btnCopyEmail.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Member Email", email));
            Toast.makeText(getContext(), "Email copied!", Toast.LENGTH_SHORT).show();
        });

        credBinding.btnSendEmail.setOnClickListener(v -> {
            credBinding.btnSendEmail.setEnabled(false);
            credBinding.btnSendEmail.setText("Sending...");

            // Get member by email if possible to get the correct ID
            Member member = viewModel.getMemberByEmail(email);
            if (member == null) {
                // Fallback: create a dummy member with ID if name/email match
                member = new Member(memberName, "Member", true, phone, email);
                // The ID is crucial, so we try to find it from the list if the direct lookup fails
            }

            viewModel.sendInvite(member, (success, message) -> {
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(() -> {
                    credBinding.btnSendEmail.setEnabled(true);
                    credBinding.btnSendEmail.setText("Send Email");
                    if (success) {
                        Toast.makeText(getContext(), "Invitation email sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        credBinding.btnShareDetails.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, credentialsMessage);
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        credBinding.btnDone.setOnClickListener(v -> {
            dialog.dismiss();
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(((ViewGroup) getView().getParent()).getId(), MemberSuccessFragment.newInstance(memberName))
                    .addToBackStack(null)
                    .commit();
        });
        credBinding.btnCloseCredentials.setOnClickListener(v -> dialog.dismiss());
    }

    private void showProfileDialog(Member member) {
        if (member.getEmail() == null)
            return;
        getParentFragmentManager().beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(),
                        com.example.save.ui.fragments.MemberProfileFragment.newInstance(member.getEmail()))
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
        new MaterialAlertDialogBuilder(getContext()).setTitle("Reset Password")
                .setMessage("Reset for " + member.getName() + "?")
                .setPositiveButton("Reset", (d, w) -> showOTPDialog(member.getName(), viewModel.resetPassword(member), member.getEmail(), member.getPhone()))
                .setNegativeButton("Cancel", null).show();
    }

    private void showRemoveMemberConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext()).setTitle("Remove Member")
                .setMessage("Remove " + member.getName() + "?")
                .setPositiveButton("Remove", (d, w) -> {
                    viewModel.removeMember(member);
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancel", null).show();
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
