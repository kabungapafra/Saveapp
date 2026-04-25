package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        View emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

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
            // Refresh mock data to show read state if needed
        });

        view.findViewById(R.id.btnThemeToggle).setOnClickListener(v -> com.example.save.utils.ThemeUtils.toggleTheme(requireContext()));

        adapter.setOnNotificationClickListener(n -> {
            // Placeholder for detail navigation
            android.widget.Toast.makeText(getContext(), "Opening: " + n.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
        });

        observeViewModel();

        // For the purpose of this UI redesign task, we will populate with the 
        // high-fidelity mock data from the design image.
        loadMockData();

        return view;
    }

    private void loadMockData() {
        List<Notification> mockList = new ArrayList<>();
        long now = System.currentTimeMillis();
        long hour = 3600000;
        long day = 86400000;

        // Today
        Notification n1 = new Notification("New Loan Request", 
            "Sarah Jenkins requested a loan of $500.00 from the Emergency Fund.", "LOAN_REQUEST");
        n1.setTimestamp(now - (5 * 60000)); // 5 mins ago
        n1.setRead(false);
        mockList.add(n1);

        Notification n2 = new Notification("Deposit Received", 
            "Monthly Contribution: $1,200.00 has been successfully deposited to the Master Fund.", "DEPOSIT");
        n2.setTimestamp(now - (2 * hour)); // 2 hours ago
        n2.setRead(false);
        mockList.add(n2);

        // Yesterday
        Notification n3 = new Notification("Meeting Reminder", 
            "Annual General Meeting scheduled for tomorrow at 10:00 AM.\n\u231A Oct 24, 2023 • 10:00 AM", "REMINDER");
        n3.setTimestamp(now - day);
        n3.setRead(true);
        mockList.add(n3);

        Notification n4 = new Notification("Financial Summary", 
            "Your Weekly Summary for October is now available. Your personal portfolio grew by 2.4% this week.", "SUMMARY");
        n4.setTimestamp(now - (day + 4 * hour));
        n4.setRead(true);
        mockList.add(n4);

        // Earlier
        Notification n5 = new Notification("New Member Joined", 
            "Michael Thorne has joined the 'Global Savers' group.", "MEMBER_JOINED");
        n5.setTimestamp(now - (3 * day));
        n5.setRead(true);
        mockList.add(n5);

        adapter.setNotifications(mockList);
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
            // We are using mock data for the visual redesign demonstration
            // If the user wants to revert to real data, they can uncomment this
            // adapter.setNotifications(notifications);
        });
    }
}
