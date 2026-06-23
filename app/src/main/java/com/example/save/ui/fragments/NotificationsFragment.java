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
import com.example.save.data.models.Notification;
import com.example.save.ui.adapters.NotificationAdapter;
import com.example.save.ui.viewmodels.NotificationsViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;
    private View emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

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
            android.widget.Toast.makeText(getContext(), "All notifications marked as read", android.widget.Toast.LENGTH_SHORT).show();
        });

        adapter.setOnNotificationClickListener(n -> {
            android.widget.Toast.makeText(getContext(), "Opening: " + n.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
        });

        observeViewModel();
        viewModel.fetchNotifications();
        return view;
    }

    public static NotificationsFragment newInstance(boolean isAdmin) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putBoolean("IS_ADMIN", isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    private void observeViewModel() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                // Show clean empty state — no demo data
                if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
                adapter.setNotifications(new ArrayList<>());
            } else {
                if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
                adapter.setNotifications(notifications);
            }
        });
    }
}
