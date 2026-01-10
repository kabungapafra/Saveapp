package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.save.ui.adapters.OnboardingAdapter;

import com.example.save.R;
import com.example.save.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.fs1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_subtitle_1)));
        onboardingItems.add(new OnboardingItem(
                R.drawable.fs2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_subtitle_2)));
        onboardingItems.add(new OnboardingItem(
                R.drawable.fs3,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_subtitle_3)));

        adapter = new OnboardingAdapter(onboardingItems, new OnboardingAdapter.OnButtonClickListener() {
            @Override
            public void onNextClick(int position) {
                if (position < onboardingItems.size() - 1) {
                    // Go to next page
                    binding.viewPager.setCurrentItem(position + 1);
                } else {
                    // Last page - mark onboarding as completed and go to LoginActivity
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isFirstTime", false);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onBackClick(int position) {
                // Go to previous page
                if (position > 0) {
                    binding.viewPager.setCurrentItem(position - 1);
                }
            }
        });

        binding.viewPager.setAdapter(adapter);
    }
}