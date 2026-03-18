package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.databinding.ActivityResetSuccessBinding;

/**
 * Activity displayed after a successful password reset.
 */
public class PasswordResetSuccessActivity extends AppCompatActivity {

    private ActivityResetSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup click listeners
        binding.backToLoginButton.setOnClickListener(v -> navigateToLogin());
        binding.contactSupportText.setOnClickListener(v -> {
            // Simplified support action
            android.widget.Toast.makeText(this, "Contacting support...", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        binding.helpIcon.setOnClickListener(v -> {
            // Placeholder for help
        });

        // Start animation with a small delay to ensure visibility
        binding.successCheckAnim.postDelayed(() -> {
            android.graphics.drawable.Drawable drawable = binding.successCheckAnim.getDrawable();
            if (drawable instanceof android.graphics.drawable.AnimatedVectorDrawable) {
                ((android.graphics.drawable.AnimatedVectorDrawable) drawable).start();
            }
        }, 500); // 500ms delay for visual impact
    }

    private void navigateToLogin() {
        // Navigate back to the screen where user chooses Member or Admin
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
