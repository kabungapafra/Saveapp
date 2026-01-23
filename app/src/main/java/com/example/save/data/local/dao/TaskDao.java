package com.example.save.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.save.data.local.entities.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("SELECT * FROM tasks ORDER BY dateAssigned DESC")
    LiveData<List<TaskEntity>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE dateAssigned BETWEEN :start AND :end ORDER BY time ASC")
    List<TaskEntity> getTasksForDateSync(long start, long end);

    @Query("SELECT * FROM tasks WHERE dateAssigned BETWEEN :start AND :end ORDER BY time ASC")
    LiveData<List<TaskEntity>> getTasksForDate(long start, long end);
}
