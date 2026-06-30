package com.example.save.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.databinding.FragmentProfileInfoBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class ProfileInfoFragment extends Fragment {

    private FragmentProfileInfoBinding binding;
    private com.example.save.ui.viewmodels.MembersViewModel viewModel;
    private androidx.activity.result.ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> googleLinkLauncher;
    private GoogleSignInClient mGoogleSignInClient;
    private SessionManager session;
    private String currentMemberId;
    // The loaded member's own phone — the key the avatar cache + card adapters use.
    // session.getUserPhone() can be stored in a different format (local vs international),
    // so we must key the avatar off this, not the session phone.
    private String avatarPhone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileInfoBinding.inflate(inflater, container, false);
        session = SessionManager.getInstance(requireContext());
        
        imagePickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String localPath = saveImageToInternalStorage(uri);
                        if (localPath != null) {
                            String key = (avatarPhone != null && !avatarPhone.isEmpty())
                                    ? avatarPhone : session.getUserPhone();
                            session.saveProfileImage(key, localPath);
                            loadAvatar(localPath);
                            uploadAvatar(localPath);
                        }
                    }
                }
        );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleLinkLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String idToken = account.getIdToken();
                        if (idToken != null) {
                            firebaseSignInAndLink(idToken, account.getEmail());
                        } else {
                            Toast.makeText(getContext(), "Google sign-in failed: no token", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        if (e.getStatusCode() != com.google.android.gms.common.api.CommonStatusCodes.CANCELED) {
                            Toast.makeText(getContext(), "Google sign-in error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        return binding.getRoot();
    }

    private String saveImageToInternalStorage(android.net.Uri uri) {
        try {
            android.content.Context context = requireContext();
            String fileName = "profile_image_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(context.getFilesDir(), fileName);
            
            java.io.InputStream inputStream = context.getContentResolver().openInputStream(uri);
            java.io.OutputStream outputStream = new java.io.FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        setupListeners();
        loadProfileInfo();
        refreshAvatar();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAvatar();
    }

    private void refreshAvatar() {
        // Prefer the loaded member's phone (matches the card/rehydration cache key);
        // fall back to the session phone before the member has loaded.
        String phone = (avatarPhone != null && !avatarPhone.isEmpty()) ? avatarPhone : session.getUserPhone();
        String savedImage = session.getProfileImage(phone);
        // Mirror the card adapters: load any non-empty value and let Glide resolve it
        // (file path, content:// URI, or remote URL). The earlier File.exists() gate
        // silently skipped valid content URIs, which is why the photo never appeared here.
        if (savedImage != null && !savedImage.isEmpty()) {
            loadAvatar(savedImage);
        }
    }

    /**
     * Compresses the picked image and uploads it (base64) to the server so the avatar
     * survives uninstall/reinstall. The local copy is already shown immediately.
     */
    private void uploadAvatar(String localPath) {
        if (viewModel == null || currentMemberId == null || currentMemberId.isEmpty()) return;
        try {
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(localPath);
            if (bmp == null) return;
            int max = 512;
            int w = bmp.getWidth();
            int h = bmp.getHeight();
            float scale = Math.min(1f, (float) max / Math.max(w, h));
            if (scale < 1f) {
                bmp = android.graphics.Bitmap.createScaledBitmap(
                        bmp, Math.round(w * scale), Math.round(h * scale), true);
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
            String base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP);
            viewModel.updateProfileImage(currentMemberId, base64, (success, message) -> {
                // Best-effort: the avatar is already cached/shown locally.
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAvatar(String uriString) {
        if (isAdded() && getContext() != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(uriString)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.imgProfile);
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnChangePhoto.setOnClickListener(v -> {
            applyClickAnimation(v);
            imagePickerLauncher.launch("image/*");
        });

        if (binding.btnSaveProfile != null) {
            binding.btnSaveProfile.setOnClickListener(v -> {
                applyClickAnimation(v);
                Toast.makeText(getContext(), "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        if (binding.btnLinkGoogle != null) {
            binding.btnLinkGoogle.setOnClickListener(v -> {
                applyClickAnimation(v);
                mGoogleSignInClient.signOut().addOnCompleteListener(task ->
                        googleLinkLauncher.launch(mGoogleSignInClient.getSignInIntent()));
            });
        }
    }

    private void firebaseSignInAndLink(String googleIdToken, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), authTask -> {
                    if (authTask.isSuccessful() && authTask.getResult().getUser() != null) {
                        authTask.getResult().getUser().getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                sendLinkToBackend(tokenTask.getResult().getToken(), email);
                            } else {
                                Toast.makeText(getContext(), "Google authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Google authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendLinkToBackend(String firebaseToken, String email) {
        com.example.save.data.network.ApiService api = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);
        com.example.save.data.network.GoogleLinkRequest req =
                new com.example.save.data.network.GoogleLinkRequest(firebaseToken);

        if (binding.btnLinkGoogle != null) binding.btnLinkGoogle.setEnabled(false);

        api.linkGoogleAccount(req).enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                                   retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                if (binding == null) return;
                if (binding.btnLinkGoogle != null) binding.btnLinkGoogle.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (binding.tvLinkGoogleLabel != null) {
                        binding.tvLinkGoogleLabel.setText("Google Linked ✓");
                    }
                    Toast.makeText(getContext(),
                            "Google account linked" + (email != null ? " (" + email + ")" : ""),
                            Toast.LENGTH_LONG).show();
                } else {
                    String msg = "Failed to link Google account";
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            String[] parts = raw.split("\"");
                            for (int i = 0; i + 2 < parts.length; i++) {
                                if ("detail".equals(parts[i].trim())) { msg = parts[i + 2].trim(); break; }
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                if (binding == null) return;
                if (binding.btnLinkGoogle != null) binding.btnLinkGoogle.setEnabled(true);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileInfo() {
        if (getContext() == null) return;

        final String sessionName = session.getUserName();
        String phone = session.getUserPhone();
        // Some login paths store an empty phone — fall back to the persisted last-phone.
        if (phone == null || phone.isEmpty()) phone = session.getLastPhone();

        // Always show what the session already knows, so the screen is never blank.
        binding.etFullName.setText(sessionName);
        binding.etPhone.setText(phone);
        binding.tvDisplayName.setText(sessionName != null && !sessionName.isEmpty() ? sessionName : "—");

        if (phone != null && !phone.isEmpty()) {
            // Preferred: resolve the member reactively by phone.
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    bindMember(member);
                } else {
                    viewModel.syncMembers();
                }
            });
        } else if (sessionName != null && !sessionName.isEmpty()) {
            // No phone on file — resolve the member by name from the synced list instead.
            viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
                if (members == null) return;
                for (com.example.save.data.models.Member m : members) {
                    if (sessionName.equalsIgnoreCase(m.getName())) {
                        bindMember(m);
                        break;
                    }
                }
            });
            viewModel.syncMembers();
        } else {
            // Truly nothing to go on.
            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
        }
    }

    /** Binds a resolved member's details (and avatar key) into the profile UI. */
    private void bindMember(com.example.save.data.models.Member member) {
        if (binding == null || member == null) return;
        binding.etFullName.setText(member.getName());
        binding.etPhone.setText(member.getPhone());
        binding.tvDisplayName.setText(member.getName() != null ? member.getName() : "—");

        String role = member.getRole() != null ? member.getRole().toUpperCase() : "MEMBER";
        binding.tvRole.setText(role);

        String id = member.getId();
        binding.tvMemberId.setText("MEMBER ID #" + (id != null && !id.isEmpty()
                ? id.substring(0, Math.min(8, id.length())).toUpperCase()
                : "—"));

        currentMemberId = member.getId();
        avatarPhone = member.getPhone();
        binding.tvMemberSince.setText(formatJoinDate(member.getJoinedDate()));
        binding.tvStatus.setText(member.isActive() ? "Active" : "Inactive");
        refreshAvatar();
    }

    /** Best-effort formatting of the backend join date (ISO/date string) to "MMM yyyy". */
    private String formatJoinDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        String datePart = raw.contains("T") ? raw.substring(0, raw.indexOf('T')) : raw;
        try {
            java.util.Date d = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(datePart);
            if (d != null) return new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(d);
        } catch (java.text.ParseException ignored) {
            // fall through to raw value
        }
        return datePart;
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
