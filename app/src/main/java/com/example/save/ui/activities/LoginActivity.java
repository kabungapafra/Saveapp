package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Animate Logo Image (Heartbeat) - NOT the container
        android.view.animation.Animation heartbeat = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);
    }

    /**
     * Handle Admin button click from XML
     */
    public void onAdminClick(View view) {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Handle Member button click from XML
     */
    public void onMemberClick(View view) {
        Intent intent = new Intent(this, MemberLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Handle Server Settings click
     */
    public void onServerSettingsClick(View view) {
        com.example.save.ui.fragments.ServerUrlDialogFragment dialog = new com.example.save.ui.fragments.ServerUrlDialogFragment();
        dialog.show(getSupportFragmentManager(), "ServerUrlDialog");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}