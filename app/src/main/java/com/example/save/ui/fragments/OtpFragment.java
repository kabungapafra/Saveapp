package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.save.databinding.FragmentOtpBinding;
import com.example.save.ui.activities.AdminSetupWizardActivity;

/**
 * OtpFragment - Fully Static Version
 * No Database, No Network.
 */
public class OtpFragment extends Fragment {
    private FragmentOtpBinding binding;

    public static OtpFragment newInstanceForRegistration(String name, String groupName, String phone, String email, String password) {
        OtpFragment fragment = new OtpFragment();
        Bundle args = new Bundle();
        args.putString("admin_name", name);
        args.putString("group_name", groupName);
        args.putString("phone", phone);
        args.putString("email", email);
        args.putString("password", password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOtpBinding.inflate(inflater, container, false);
        
        binding.verifyButton.setOnClickListener(v -> {
            String otpCode = getOtpFromInputs();
            if (otpCode.length() < 6) {
                Toast.makeText(getContext(), "Please enter the 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }

            if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
                onVerificationSuccess();
                return;
            }

            binding.verifyButton.setEnabled(false);
            binding.verifyButton.setText("Verifying...");

            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
            if (activity == null) return;

            com.example.save.data.network.OtpVerificationRequest request = new com.example.save.data.network.OtpVerificationRequest(
                activity.getAdminEmail(),
                otpCode,
                activity.getAdminName(),
                activity.getAdminPassword(),
                activity.getGroupName()
            );

            com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(getContext()).create(com.example.save.data.network.ApiService.class);

            apiService.verifyAdminOtp(request).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call, 
                                       retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                    binding.verifyButton.setEnabled(true);
                    binding.verifyButton.setText("Verify & Continue");

                    if (response.isSuccessful() && response.body() != null) {
                        // Save token and session
                        com.example.save.utils.SessionManager.getInstance(getContext()).saveJwtToken(response.body().getToken());
                        Toast.makeText(getContext(), "OTP Verified! Account Created.", Toast.LENGTH_SHORT).show();
                        onVerificationSuccess();
                    } else {
                        String error = "Invalid or expired OTP";
                        if (response.errorBody() != null) {
                            try { error = response.errorBody().string(); } catch (Exception e) {}
                        }
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                    binding.verifyButton.setEnabled(true);
                    binding.verifyButton.setText("Verify & Continue");
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.resendOtp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "OTP Resent!", Toast.LENGTH_SHORT).show();
        });

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        return binding.getRoot();
    }

    private String getOtpFromInputs() {
        return binding.otpDigit1.getText().toString() +
               binding.otpDigit2.getText().toString() +
               binding.otpDigit3.getText().toString() +
               binding.otpDigit4.getText().toString() +
               binding.otpDigit5.getText().toString() +
               binding.otpDigit6.getText().toString();
    }

    private void onVerificationSuccess() {
        if (getActivity() instanceof AdminSetupWizardActivity) {
            ((AdminSetupWizardActivity) getActivity()).nextStep();
        }
    }
}