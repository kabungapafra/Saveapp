package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;

public class ActiveStandingsFragment extends Fragment {

    public static ActiveStandingsFragment newInstance() {
        return new ActiveStandingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_standings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupTopNav(view);
    }

    private void setupTopNav(View view) {
        View btnBell = view.findViewById(R.id.btnBell);
        if (btnBell != null) {
            btnBell.setOnClickListener(v -> 
                Toast.makeText(getContext(), "Notifications", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
