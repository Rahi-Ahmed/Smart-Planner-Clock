package com.example.smartplannerclock.data;

import android.content.Context;

import com.example.smartplannerclock.AppDatabase;
import com.example.smartplannerclock.TaskDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executorService;

    public TaskRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        taskDao = db.taskDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Task task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    public void getAllTasks(Callback<List<Task>> callback) {
        executorService.execute(() -> callback.onResult(taskDao.getAllTasks()));
    }

    public void getTaskById(int id, Callback<Task> callback) {
        executorService.execute(() -> callback.onResult(taskDao.getTaskById(id)));
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}
