package com.example.save.ui.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.save.R;
import com.example.save.utils.SessionManager;

public class MemberProfileDialogFragment extends DialogFragment {

    public static MemberProfileDialogFragment newInstance() {
        return new MemberProfileDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set transparent background for the dialog window to show the card's rounded corners
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_member_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnClose = view.findViewById(R.id.btnCloseProfile);
        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);

        tvName.setTextColor(getResources().getColor(R.color.v_text_dark, null));
        tvEmail.setTextColor(getResources().getColor(R.color.v_text_mid, null));

        // Mock data or fetch from SessionManager
        tvName.setText("Pafra Francis");
        tvEmail.setText("pafra.francis@save.app");

        btnClose.setOnClickListener(v -> dismiss());

        btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance(getContext()).logoutUser();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
