package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.data.network.ApiResponse;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.OnboardingSetPasswordRequest;
import com.example.save.data.network.RetrofitClient;
import com.example.save.databinding.ActivitySetPasswordBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetPasswordActivity extends AppCompatActivity {

    private ActivitySetPasswordBinding binding;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        phone = getIntent().getStringExtra("phone");

        binding.submitButton.setOnClickListener(v -> submitPassword());
    }

    private void submitPassword() {
        String password = binding.passwordInput.getText().toString().trim();
        String confirm = binding.confirmPasswordInput.getText().toString().trim();

        if (password.length() < 4) {
            Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loadingProgress.setVisibility(View.VISIBLE);
        binding.submitButton.setEnabled(false);

        ApiService api = RetrofitClient.getClient(this).create(ApiService.class);
        api.setOnboardingPassword(new OnboardingSetPasswordRequest(phone, password))
                .enqueue(new Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.network.LoginResponse> call, Response<com.example.save.data.network.LoginResponse> response) {
                binding.loadingProgress.setVisibility(View.GONE);
                binding.submitButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse body = response.body();
                    
                    // Save Session
                    com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(SetPasswordActivity.this);
                    session.createLoginSession(
                            body.getName(),
                            body.getEmail(),
                            phone,
                            body.getRole(),
                            false,
                            body.isCreator());
                    session.saveJwtToken(body.getToken());
                    
                    // Update Retrofit Client Token
                    com.example.save.data.network.RetrofitClient.getInstance(getApplicationContext()).updateToken(body.getToken());

                    Toast.makeText(SetPasswordActivity.this, "Registration complete! Welcome " + body.getName(), Toast.LENGTH_LONG).show();
                    
                    // Go to Dashboard
                    Intent intent = new Intent(SetPasswordActivity.this, MemberMainActivity.class);
                    intent.putExtra("member_name", body.getName());
                    intent.putExtra("member_email", body.getEmail());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SetPasswordActivity.this, "Failed to activate account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                binding.loadingProgress.setVisibility(View.GONE);
                binding.submitButton.setEnabled(true);
                Toast.makeText(SetPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
