package com.example.smartplannerclock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartplannerclock.adapter.TaskAdapter;
import com.example.smartplannerclock.data.Task;
import com.example.smartplannerclock.data.TaskRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TaskFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTasks);
        FloatingActionButton fabAddTask = view.findViewById(R.id.fabAddTask);

        repository = new TaskRepository(requireContext());
        adapter = new TaskAdapter(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(requireContext(), AddEditTaskActivity.class);
                intent.putExtra("task_id", task.getId());
                intent.putExtra("task_title", task.getTitle());
                intent.putExtra("task_description", task.getDescription());
                intent.putExtra("task_date", task.getDate());
                intent.putExtra("task_time", task.getTime());
                intent.putExtra("task_alarm", task.isAlarmEnabled());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Task task) {
                repository.delete(task);
                NotificationHelper.cancelAlarm(requireContext(), task.getId());
                loadTasks();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        repository.getAllTasks(new TaskRepository.Callback<List<Task>>() {
            @Override
            public void onResult(List<Task> result) {
                new Handler(Looper.getMainLooper()).post(() -> adapter.setTasks(result));
            }
        });
    }
}
