package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.ActivityDailyTasksBinding;
import com.example.save.databinding.DialogAddTaskBinding;
import com.example.save.databinding.ItemCalendarDateBinding;
import com.example.save.databinding.ItemTimelineTaskBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.save.ui.viewmodels.MembersViewModel; // Added import
import com.example.save.data.models.Member;

public class DailyTasksActivity extends AppCompatActivity {

    private ActivityDailyTasksBinding binding;

    private TimelineAdapter timelineAdapter;
    private List<TaskModel> taskList;
    private List<TaskModel> allTasks;
    private Calendar selectedDate;

    // ViewModel and Data
    private MembersViewModel membersViewModel;
    private List<Member> memberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDailyTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        membersViewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);

        setupListeners();
        setupCalendar();
        setupTimeline();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.addTaskFab.setOnClickListener(v -> showAddTaskDialog());
        binding.monthContainer.setOnClickListener(v -> showMonthPicker());

        selectedDate = Calendar.getInstance();

        // Observe members
        membersViewModel.getMembers().observe(this, members -> {
            memberList = members; // Cache list for dialog
        });
    }

    private void setupCalendar() {
        binding.calendarRecyclerView
                .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<DateItem> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);

        binding.monthText.setText(monthFormat.format(calendar.getTime()));

        for (int i = 0; i < 30; i++) {
            String day = dayFormat.format(calendar.getTime()).toUpperCase();
            String date = dateFormat.format(calendar.getTime());
            // Select the 3rd item for demo purposes
            dates.add(new DateItem(day, date, i == 2, calendar));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        binding.calendarRecyclerView.setAdapter(new DailyCalendarAdapter(dates, item -> {
            // Update Month Header
            SimpleDateFormat mFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
            binding.monthText.setText(mFormat.format(item.calendar.getTime()));

            // Update selected date and load tasks
            selectedDate = item.calendar;
            loadTasksForDate(selectedDate);
        }));
    }

    private void setupTimeline() {
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        allTasks = new ArrayList<>();
        taskList = new ArrayList<>();

        // Create mock tasks with different dates for testing
        Calendar today = Calendar.getInstance();

        Calendar day1 = (Calendar) today.clone();
        day1.add(Calendar.DAY_OF_YEAR, 0);
        allTasks.add(new TaskModel("10:00 AM", "Web Design", "Sending to: John Doe", "Completed", "100%",
                Color.WHITE, R.drawable.ic_menu_grid, day1, "50000"));
        allTasks.add(new TaskModel("03:00 PM", "Family Program", "Sending to: Jane Smith", "Pending", "00%",
                Color.parseColor("#FFEBEE"), R.drawable.ic_people, day1, "25000"));

        Calendar day2 = (Calendar) today.clone();
        day2.add(Calendar.DAY_OF_YEAR, 1);
        allTasks.add(new TaskModel("11:00 AM", "Market Research", "Sending to: Bob Wilson", "Running", "72%",
                Color.parseColor("#E3F2FD"), R.drawable.ic_analytics, day2, "75000"));

        Calendar day3 = (Calendar) today.clone();
        day3.add(Calendar.DAY_OF_YEAR, 2);
        allTasks.add(new TaskModel("02:00 PM", "Maintenance", "Sending to: Alice Johnson", "Upcoming", "00%",
                Color.WHITE, R.drawable.ic_profile_placeholder, day3, "30000"));

        // Load tasks for today initially
        loadTasksForDate(selectedDate);
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this));
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Make Time EditText non-focusable and clickable
        dialogBinding.etTime.setFocusable(false);
        dialogBinding.etTime.setClickable(true);
        dialogBinding.etTime.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(this,
                    (timeView, selectedHour, selectedMinute) -> {
                        String timeStr = String.format(Locale.ENGLISH, "%02d:%02d %s",
                                (selectedHour % 12 == 0 ? 12 : selectedHour % 12),
                                selectedMinute,
                                (selectedHour < 12 ? "AM" : "PM"));
                        dialogBinding.etTime.setText(timeStr);
                    }, hour, minute, false);
            timePicker.show();
        });

        // Populate Members Spinner
        List<String> memberNames = new ArrayList<>();
        if (memberList != null) {
            for (Member m : memberList) {
                memberNames.add(m.getName());
            }
        } else {
            memberNames.add("Global"); // Fallback
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerMembers.setAdapter(adapter);

        // ... (rest of listeners)

        dialogBinding.btnSaveTask.setOnClickListener(v -> {
            String title = dialogBinding.etTaskTitle.getText().toString();
            String amount = dialogBinding.etAmount.getText().toString();
            String time = dialogBinding.etTime.getText().toString();
            String selectedMember = (String) dialogBinding.spinnerMembers.getSelectedItem();

            if (title.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new task with selected date and amount
            String subtitle = "Sending to: " + selectedMember;

            // Create Task Model
            TaskModel newTask = new TaskModel(
                    time,
                    title,
                    subtitle,
                    "Pending",
                    "0%",
                    Color.parseColor("#E1BEE7"),
                    R.drawable.ic_payments,
                    selectedDate,
                    amount);

            // Add to both lists
            allTasks.add(0, newTask);
            taskList.add(0, newTask);
            timelineAdapter.notifyItemInserted(0);
            binding.tasksRecyclerView.scrollToPosition(0);

            dialog.dismiss();
            Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void loadTasksForDate(Calendar date) {
        taskList.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String selectedDateStr = sdf.format(date.getTime());

        for (TaskModel task : allTasks) {
            if (task.dateAssigned != null) {
                String taskDateStr = sdf.format(task.dateAssigned.getTime());
                if (taskDateStr.equals(selectedDateStr)) {
                    taskList.add(task);
                }
            }
        }

        if (timelineAdapter == null) {
            timelineAdapter = new TimelineAdapter(taskList, (task, position) -> {
                showTaskOptionsDialog(task, position);
            });
            binding.tasksRecyclerView.setAdapter(timelineAdapter);
        } else {
            timelineAdapter.notifyDataSetChanged();
        }

        // Show message if no tasks
        if (taskList.isEmpty()) {
            Toast.makeText(this, "No tasks for this date", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMonthPicker() {
        Calendar cal = (Calendar) selectedDate.clone();

        android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar newCal = Calendar.getInstance();
                    newCal.set(year, month, dayOfMonth);
                    selectedDate = newCal;

                    // Update month text
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                    binding.monthText.setText(monthFormat.format(newCal.getTime()));

                    // Regenerate calendar
                    setupCalendar();

                    // Load tasks for new date
                    loadTasksForDate(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void showTaskOptionsDialog(TaskModel task, int position) {
        String[] options = { "Edit Amount", "Delete Task" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(task.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditAmountDialog(task, position);
                    } else {
                        showDeleteConfirmation(task, position);
                    }
                })
                .show();
    }

    private void showEditAmountDialog(TaskModel task, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this));

        // Pre-fill with existing data
        dialogBinding.etTaskTitle.setText(task.title);
        dialogBinding.etAmount.setText(task.amount);
        dialogBinding.etTime.setText(task.time);
        dialogBinding.etTaskTitle.setEnabled(false);
        dialogBinding.etTime.setEnabled(false);
        dialogBinding.spinnerMembers.setVisibility(View.GONE);

        dialogBinding.btnSaveTask.setText("Update");

        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogBinding.btnSaveTask.setOnClickListener(v -> {
            String newAmount = dialogBinding.etAmount.getText().toString();
            task.amount = newAmount;

            // Update subtitle
            String memberName = task.subtitle.substring(task.subtitle.indexOf(":") + 2);
            if (memberName.contains("(")) {
                memberName = memberName.substring(0, memberName.indexOf("(")).trim();
            }
            task.subtitle = "Sending to: " + memberName + " (" + newAmount + " UGX)";

            timelineAdapter.notifyItemChanged(position);
            dialog.dismiss();
            Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showDeleteConfirmation(TaskModel task, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    allTasks.remove(task);
                    taskList.remove(position);
                    timelineAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
