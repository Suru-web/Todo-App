package com.suraj.todo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraj.todo.databinding.ActivityCreateTasksBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class create_tasks extends AppCompatActivity implements View.OnClickListener {
    private ActivityCreateTasksBinding binding;
    int date, month, yearDB;
    String task, note, category;
    FirebaseFirestore fb = FirebaseFirestore.getInstance();
    String authID = FirebaseAuth.getInstance().getUid();
    String receivedCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkUiMode();
        setDateAndTime();
        checkTextChange();

        Intent intent = getIntent();
        receivedCategory = intent.getStringExtra("category");
        if (receivedCategory!=null){
            category = receivedCategory;
            binding.category.setText(category);
        }

        binding.enterTask.requestFocus();
        binding.cancelButton.setOnClickListener(v -> {
            startActivity(new Intent(create_tasks.this, MainActivity.class));
            finish();
        });
        binding.datePickerTV.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int currentYear = c.get(Calendar.YEAR);
            int currentMonth = c.get(Calendar.MONTH);
            int currentDay = c.get(Calendar.DAY_OF_MONTH);
            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(
                    create_tasks.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        binding.datePickerTV.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        date = dayOfMonth;
                        month = monthOfYear;
                        yearDB = year;
                    },
                    currentYear, currentMonth, currentDay);
            datePickerDialog.show();
        });
        binding.addNote.setOnClickListener(v -> {
            binding.addNoteCardView.setVisibility(View.VISIBLE);
            binding.extraNote.requestFocus();
        });
        binding.category.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) create_tasks.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            binding.textInputLayoutEnterTask.clearFocus();
            binding.addCategoryCardView.setVisibility(View.VISIBLE);
        });
        binding.createButton.setOnClickListener(v -> {
            task = Objects.requireNonNull(binding.textInputLayoutEnterTask.getEditText()).getText().toString();
            note = binding.extraNote.getText().toString();
            if (date == 0 || month == 0 || yearDB == 0){
                binding.datePickerTVError.setVisibility(View.VISIBLE);
                return;
            }
            if (category == null) {
                category = "All";
            }
            if (task.isEmpty()) {
                binding.textInputLayoutEnterTask.setHelperText(getString(R.string.task_cannot_be_empty));
                return;
            }
            String id = fb.collection("users").document(authID).collection(category).document().getId();
            System.out.println("Printing the id :"+id);
            Map<String, Object> data = new HashMap<>();
            data.put("task", task);
            data.put("date", date);
            data.put("month", month);
            data.put("year", yearDB);
            data.put("notes", note);
            data.put("category", category);
            data.put("completed",false);
            fb.collection("users").document(authID).collection(category).add(data)
                    .addOnSuccessListener(documentReference -> {
                        Intent intent1 = new Intent(create_tasks.this, MainActivity.class);
                        startActivity(intent1);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(create_tasks.this, "Task cannot be saved", Toast.LENGTH_LONG).show());
        });
        binding.allTask.setOnClickListener(this);
        binding.workTask.setOnClickListener(this);
        binding.shoppingTask.setOnClickListener(this);
        binding.tripTask.setOnClickListener(this);
        binding.studyTask.setOnClickListener(this);
        binding.homeTask.setOnClickListener(this);
        binding.musicTask.setOnClickListener(this);
        binding.privateTask.setOnClickListener(this);

    }

    private void setDateAndTime() {
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String date = df.format(Calendar.getInstance().getTime());
        binding.datePickerTV.setText(date);
    }

    private void checkTextChange() {
        binding.enterTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 25) {
                    binding.textInputLayoutEnterTask.setHelperText("Its better to keep it short and sweet..");
                } else if (s.length() < 25) {
                    binding.textInputLayoutEnterTask.setHelperTextEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    protected void checkUiMode() {
        int nightModeFlags =
                binding.getRoot().getContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                setFlagColors(R.color.dark, true);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                setFlagColors(R.color.white, false);
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                setFlagColors(R.color.dark, true);
                break;
        }
    }

    protected void setFlagColors(int color, boolean blackMode) {
        //This code sets the status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(color));
        window.setNavigationBarColor(this.getResources().getColor(R.color.dark_sky_blue));

        //This part of the code displays the statusbar icon color to black
        if (!blackMode) {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.allTask) {
            category = "All";
        } else if (id == R.id.privateTask) {
            category = "Private";
        } else if (id == R.id.workTask) {
            category = "Work";
        } else if (id == R.id.shoppingTask) {
            category = "Shopping";
        } else if (id == R.id.tripTask) {
            category = "Trip";
        } else if (id == R.id.studyTask) {
            category = "Study";
        } else if (id == R.id.homeTask) {
            category = "Home";
        } else if (id == R.id.musicTask) {
            category = "Music";
        }
        binding.category.setText(category);
    }
}