package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.TaskModel;
import com.example.save.databinding.ItemTimelineTaskBinding;

import java.util.List;

/**
 * Adapter for the task timeline in DailyTasksActivity
 */
public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TaskViewHolder> {
    private List<TaskModel> tasks;
    private OnTaskLongClickListener longClickListener;

    public interface OnTaskLongClickListener {
        void onTaskLongClick(TaskModel task, int position);
    }

    public TimelineAdapter(List<TaskModel> tasks, OnTaskLongClickListener longClickListener) {
        this.tasks = tasks;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimelineTaskBinding itemBinding = ItemTimelineTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(itemBinding);
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

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTaskLongClick(task, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, taskTitle, taskSubtitle, statusText, progressText;
        CardView taskCard;
        ImageView taskIcon;

        TaskViewHolder(ItemTimelineTaskBinding itemBinding) {
            super(itemBinding.getRoot());
            timeText = itemBinding.timeText;
            taskTitle = itemBinding.taskTitle;
            taskSubtitle = itemBinding.taskSubtitle;
            statusText = itemBinding.statusText;
            progressText = itemBinding.progressText;
            taskCard = itemBinding.taskCard;
            taskIcon = itemBinding.taskIcon;
        }
    }
}
