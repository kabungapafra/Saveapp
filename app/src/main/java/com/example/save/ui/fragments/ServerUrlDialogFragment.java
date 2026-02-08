package com.example.save.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.save.R;
import com.example.save.data.network.RetrofitClient;

public class ServerUrlDialogFragment extends DialogFragment {

    private EditText etServerUrl;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_server_url, null);

        etServerUrl = view.findViewById(R.id.etServerUrl);

        // Load current URL
        SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
        String currentUrl = prefs.getString("api_base_url", "http://10.0.2.2:8000/api/");
        etServerUrl.setText(currentUrl);

        builder.setView(view)
                .setTitle("Server Configuration")
                .setPositiveButton("Save", (dialog, id) -> {
                    String newUrl = etServerUrl.getText().toString().trim();
                    if (!newUrl.isEmpty()) {
                        RetrofitClient.updateBaseUrl(requireContext(), newUrl);
                        Toast.makeText(requireContext(), "Server URL updated. Please restart app if issues persist.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> matchParent());

        return builder.create();
    }

    private void matchParent() {
        if (getDialog() != null) {
            getDialog().cancel();
        }
    }
}
