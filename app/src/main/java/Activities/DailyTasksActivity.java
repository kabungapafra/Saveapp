package Activities;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Data.MemberRepository;
import Models.Member;

public class DailyTasksActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView;
    private RecyclerView tasksRecyclerView;
    private TextView monthText;
    private LinearLayout monthContainer;
    private ImageView backButton;
    private FloatingActionButton addTaskFab;

    private TimelineAdapter timelineAdapter;
    private List<TaskModel> taskList;
    private List<TaskModel> allTasks;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tasks);

        initializeViews();
        setupCalendar();
        setupTimeline();
    }

    private void initializeViews() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        monthText = findViewById(R.id.monthText);
        monthContainer = findViewById(R.id.monthContainer);
        backButton = findViewById(R.id.backButton);
        addTaskFab = findViewById(R.id.addTaskFab);

        backButton.setOnClickListener(v -> finish());
        addTaskFab.setOnClickListener(v -> showAddTaskDialog());
        monthContainer.setOnClickListener(v -> showMonthPicker());

        selectedDate = Calendar.getInstance();
    }

    private void setupCalendar() {
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<DateItem> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Pass selected date from Intent if available
        String selectedDateStr = getIntent().getStringExtra("selected_date");

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);

        monthText.setText(monthFormat.format(calendar.getTime()));

        for (int i = 0; i < 30; i++) {
            String day = dayFormat.format(calendar.getTime()).toUpperCase();
            String date = dateFormat.format(calendar.getTime());
            // Select the 3rd item for demo purposes
            dates.add(new DateItem(day, date, i == 2, calendar));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        calendarRecyclerView.setAdapter(new CalendarAdapter(dates));
    }

    private void setupTimeline() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        Spinner spinnerMembers = view.findViewById(R.id.spinnerMembers);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etTime = view.findViewById(R.id.etTime);
        Button btnSave = view.findViewById(R.id.btnSaveTask);

        // Make Time EditText non-focusable and clickable
        etTime.setFocusable(false);
        etTime.setClickable(true);
        etTime.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(this,
                    (timeView, selectedHour, selectedMinute) -> {
                        String timeStr = String.format(Locale.ENGLISH, "%02d:%02d %s",
                                (selectedHour % 12 == 0 ? 12 : selectedHour % 12),
                                selectedMinute,
                                (selectedHour < 12 ? "AM" : "PM"));
                        etTime.setText(timeStr);
                    }, hour, minute, false);
            timePicker.show();
        });

        // Populate Members Spinner
        MemberRepository repository = MemberRepository.getInstance();
        List<Member> members = repository.getAllMembers();
        List<String> memberNames = new ArrayList<>();
        for (Member m : members) {
            memberNames.add(m.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, memberNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMembers.setAdapter(adapter);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String amount = etAmount.getText().toString();
            String time = etTime.getText().toString();
            String selectedMember = (String) spinnerMembers.getSelectedItem();

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
            tasksRecyclerView.scrollToPosition(0);

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
            timelineAdapter = new TimelineAdapter(taskList);
            tasksRecyclerView.setAdapter(timelineAdapter);
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
                    monthText.setText(monthFormat.format(newCal.getTime()));

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
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etTime = view.findViewById(R.id.etTime);
        Spinner spinnerMembers = view.findViewById(R.id.spinnerMembers);
        Button btnSave = view.findViewById(R.id.btnSaveTask);

        // Pre-fill with existing data
        etTitle.setText(task.title);
        etAmount.setText(task.amount);
        etTime.setText(task.time);
        etTitle.setEnabled(false);
        etTime.setEnabled(false);
        spinnerMembers.setVisibility(View.GONE);

        btnSave.setText("Update");

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnSave.setOnClickListener(v -> {
            String newAmount = etAmount.getText().toString();
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

    // --- Calendar Adapter & Model ---

    private static class DateItem {
        String day;
        String date;
        boolean isSelected;
        Calendar calendar;

        DateItem(String day, String date, boolean isSelected, Calendar calendar) {
            this.day = day;
            this.date = date;
            this.isSelected = isSelected;
            this.calendar = (Calendar) calendar.clone();
        }
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DateViewHolder> {
        private List<DateItem> dates;

        CalendarAdapter(List<DateItem> dates) {
            this.dates = dates;
        }

        @NonNull
        @Override
        public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent,
                    false);
            return new DateViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
            DateItem item = dates.get(position);
            holder.dayText.setText(item.day);
            holder.dateText.setText(item.date);

            if (item.isSelected) {
                holder.container.setBackgroundResource(R.drawable.date_selector_bg);
                holder.container.setSelected(true);
                holder.dayText.setTextColor(Color.WHITE);
                holder.dateText.setTextColor(Color.WHITE);
            } else {
                holder.container.setBackgroundResource(R.drawable.date_selector_bg);
                holder.container.setSelected(false);
                holder.dayText.setTextColor(Color.parseColor("#666666")); // Gray
                holder.dateText.setTextColor(Color.parseColor("#1A1A1A")); // Black
            }

            holder.itemView.setOnClickListener(v -> {
                for (DateItem d : dates)
                    d.isSelected = false;
                item.isSelected = true;
                notifyDataSetChanged();

                // Update Month Header
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                monthText.setText(monthFormat.format(item.calendar.getTime()));

                // Update selected date and load tasks
                selectedDate = item.calendar;
                loadTasksForDate(selectedDate);
            });
        }

        @Override
        public int getItemCount() {
            return dates.size();
        }

        class DateViewHolder extends RecyclerView.ViewHolder {
            LinearLayout container;
            TextView dayText, dateText;

            DateViewHolder(View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.dateContent);
                dayText = itemView.findViewById(R.id.dayText);
                dateText = itemView.findViewById(R.id.dateText);
            }
        }
    }

    // --- Timeline Adapter & Model ---

    private static class TaskModel {
        String time;
        String title;
        String subtitle;
        String status;
        String progress;
        int color;
        int iconRes;
        Calendar dateAssigned;
        String amount;

        TaskModel(String time, String title, String subtitle, String status, String progress, int color, int iconRes,
                Calendar dateAssigned, String amount) {
            this.time = time;
            this.title = title;
            this.subtitle = subtitle;
            this.status = status;
            this.progress = progress;
            this.color = color;
            this.iconRes = iconRes;
            this.dateAssigned = dateAssigned != null ? (Calendar) dateAssigned.clone() : null;
            this.amount = amount;
        }
    }

    private class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TaskViewHolder> {
        private List<TaskModel> tasks;

        TimelineAdapter(List<TaskModel> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline_task, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            TaskModel task = tasks.get(position);

            holder.timeText.setText(task.time);
            holder.taskTitle.setText(task.title);
            holder.taskSubtitle.setText(task.subtitle);
            holder.statusText.setText(task.status);
            holder.progressText.setText(task.progress);

            holder.taskCard.setCardBackgroundColor(task.color);
            holder.taskIcon.setImageResource(task.iconRes);

            // Add click listener to more icon for edit/delete
            holder.itemView.setOnLongClickListener(v -> {
                showTaskOptionsDialog(task, position);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView timeText, taskTitle, taskSubtitle, statusText, progressText;
            CardView taskCard;
            ImageView taskIcon;

            TaskViewHolder(View itemView) {
                super(itemView);
                timeText = itemView.findViewById(R.id.timeText);
                taskTitle = itemView.findViewById(R.id.taskTitle);
                taskSubtitle = itemView.findViewById(R.id.taskSubtitle);
                statusText = itemView.findViewById(R.id.statusText);
                progressText = itemView.findViewById(R.id.progressText);
                taskCard = itemView.findViewById(R.id.taskCard);
                taskIcon = itemView.findViewById(R.id.taskIcon);
            }
        }
    }
}
