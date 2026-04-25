package com.example.save.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;
import com.example.save.R;
import com.example.save.databinding.ActivityMemberInvitationOnboardingBinding;
import com.example.save.ui.adapters.OnboardingPagerAdapter;

public class MemberInvitationActivity extends AppCompatActivity {
    
    private ActivityMemberInvitationOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always show onboarding in light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityMemberInvitationOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setupListeners();
    }

    private void setupViewPager() {
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter();
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                if (position == 2) {
                    binding.btnNext.setText("Get Started");
                    binding.btnNext.setIcon(null);
                } else {
                    binding.btnNext.setText("Next");
                    if (position == 1) {
                        binding.btnNext.setIconResource(R.drawable.ic_arrow_right);
                        binding.btnNext.setIconGravity(com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_END);
                        binding.btnNext.setIconPadding(8);
                    } else {
                        binding.btnNext.setIcon(null);
                    }
                }
            }
        });
    }

    private void updateIndicators(int position) {
        // Dot 1
        animateIndicator(binding.indicator1, position == 0);
        // Dot 2
        animateIndicator(binding.indicator2, position == 1);
        // Dot 3
        animateIndicator(binding.indicator3, position == 2);
    }

    private void animateIndicator(View indicator, boolean isSelected) {
        int targetWidth = isSelected ? dpToPx(24) : dpToPx(6);
        int color = isSelected ? 0xFF2563EB : 0xFFE2E8F0;

        ViewGroup.LayoutParams lp = indicator.getLayoutParams();
        lp.width = targetWidth;
        indicator.setLayoutParams(lp);
        indicator.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupListeners() {
        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < 2) {
                binding.viewPager.setCurrentItem(current + 1, true);
            } else {
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MemberMainActivity.class);
        if (getIntent().hasExtra("member_name")) {
            intent.putExtra("member_name", getIntent().getStringExtra("member_name"));
        }
        if (getIntent().hasExtra("member_email")) {
            intent.putExtra("member_email", getIntent().getStringExtra("member_email"));
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
