package com.example.smartplannerclock;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartplannerclock.data.Task;
import com.example.smartplannerclock.data.TaskRepository;

import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDate, etTime;
    private CheckBox cbAlarm;
    private TaskRepository repository;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        cbAlarm = findViewById(R.id.cbAlarm);
        Button btnSave = findViewById(R.id.btnSaveTask);

        repository = new TaskRepository(this);
        NotificationHelper.createNotificationChannel(this);

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("task_id")) {
            taskId = intent.getIntExtra("task_id", -1);
            etTitle.setText(intent.getStringExtra("task_title"));
            etDescription.setText(intent.getStringExtra("task_description"));
            etDate.setText(intent.getStringExtra("task_date"));
            etTime.setText(intent.getStringExtra("task_time"));
            cbAlarm.setChecked(intent.getBooleanExtra("task_alarm", false));
        }

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            etTime.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        boolean alarmEnabled = cbAlarm.isChecked();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task(title, description, date, time, false, alarmEnabled);

        if (taskId == -1) {
            repository.insert(task);
            Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
        } else {
            task.setId(taskId);
            repository.update(task);
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        }

        if (alarmEnabled) {
            NotificationHelper.scheduleAlarm(this, taskId == -1 ? (int) System.currentTimeMillis() : taskId, title, date, time);
        }

        finish();
    }
}
