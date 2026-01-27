package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.ui.adapters.NotificationAdapter;
import com.example.save.ui.viewmodels.NotificationsViewModel;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        RecyclerView rv = view.findViewById(R.id.notificationsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter();
        rv.setAdapter(adapter);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });

        view.findViewById(R.id.btnMarkAllRead).setOnClickListener(v -> {
            viewModel.markAllAsRead();
            android.widget.Toast.makeText(getContext(), "Marked as read", android.widget.Toast.LENGTH_SHORT).show();
        });
        adapter.setOnNotificationClickListener(n -> {
            // Handle click - expand or just mark read
            // For now just mark read
            // In real app, maybe open details dialog
            // We can't easily update just one item without ID match in list, but standard
            // LiveData flow handles it if DB updates.
        });

        observeViewModel();

        // Auto-generate a welcome message if empty (simple hack for demo)
        // viewModel.createTestNotification();

        // Check if Admin
        boolean isAdmin = false;
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean("IS_ADMIN", false);
        }

        View fab = view.findViewById(R.id.fabComposeAnnouncement);
        if (isAdmin) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> showComposeAnnouncementDialog());
        }

        return view;
    }
    
    private View emptyStateLayout;
    private android.widget.TextView tvEmptyTitle;
    private android.widget.TextView tvEmptyMessage;

    public static NotificationsFragment newInstance(boolean isAdmin) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putBoolean("IS_ADMIN", isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    private void showComposeAnnouncementDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_compose_announcement, null);
        com.google.android.material.textfield.TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        com.google.android.material.textfield.TextInputEditText etMessage = view.findViewById(R.id.etMessage);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Send Announcement")
                .setView(view)
                .setPositiveButton("Send", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String message = etMessage.getText().toString().trim();

                    if (!title.isEmpty() && !message.isEmpty()) {
                        viewModel.createAnnouncement(title, message);
                        android.widget.Toast
                                .makeText(getContext(), "Announcement Sent!", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        android.widget.Toast
                                .makeText(getContext(), "Title and message required", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            adapter.setNotifications(notifications);
            if (notifications == null || notifications.isEmpty()) {
                if (emptyStateLayout != null) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    if (tvEmptyTitle != null) tvEmptyTitle.setText("No notifications");
                    if (tvEmptyMessage != null) tvEmptyMessage.setText("You're all caught up!");
                }
            } else {
                 if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
            }
        });
    }
}
