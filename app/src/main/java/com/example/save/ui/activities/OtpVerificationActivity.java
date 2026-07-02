package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityOtpVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationActivity extends AppCompatActivity {

    private ActivityOtpVerificationBinding binding;
    private String verificationId;
    private String phone;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phone");
        groupName = getIntent().getStringExtra("groupName");

        binding.verifyButton.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = binding.otpInput.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loadingProgress.setVisibility(View.VISIBLE);
        binding.verifyButton.setEnabled(false);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Fetch the Firebase ID token so SetPasswordActivity can prove
                        // ownership of this phone number to the backend.
                        task.getResult().getUser().getIdToken(false)
                                .addOnCompleteListener(tokenTask -> {
                                    binding.loadingProgress.setVisibility(View.GONE);
                                    binding.verifyButton.setEnabled(true);
                                    if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                        String idToken = tokenTask.getResult().getToken();
                                        Intent intent = new Intent(this, SetPasswordActivity.class);
                                        intent.putExtra("phone", phone);
                                        intent.putExtra("idToken", idToken);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                        finish();
                                    } else {
                                        Toast.makeText(OtpVerificationActivity.this, "Verification failed, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        binding.loadingProgress.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);
                        Toast.makeText(OtpVerificationActivity.this, "Invalid OTP code", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
