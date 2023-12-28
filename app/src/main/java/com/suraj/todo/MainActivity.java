package com.suraj.todo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.suraj.todo.Adapters.ImageAdapter;
import com.suraj.todo.Adapters.main_adapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.suraj.todo.objects.main_list;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.suraj.todo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    final ArrayList<main_list> combinedList = new ArrayList<>();
    ArrayList<main_list> allList = new ArrayList<>();
    ArrayList<main_list> privateList = new ArrayList<>();
    ArrayList<main_list> workList = new ArrayList<>();
    ArrayList<main_list> shoppingList = new ArrayList<>();
    ArrayList<main_list> tripList = new ArrayList<>();
    ArrayList<main_list> studyList = new ArrayList<>();
    ArrayList<main_list> homeList = new ArrayList<>();
    ArrayList<main_list> musicList = new ArrayList<>();
    String authID;
    Bitmap bitmap;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setSystemBarColors();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        authID = auth.getUid();
        firestore = FirebaseFirestore.getInstance();
        setNameUser();
        setFabColor();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(DueDateWorker.class, 24, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(periodicWorkRequest);

        ViewPager viewPager = findViewById(R.id.viewPager);
        ImageAdapter adapterImage = new ImageAdapter(this);
        viewPager.setAdapter(adapterImage);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                saveCurrentImagePosition(position);
                vibrator.vibrate(5);
                setFabColor();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        int savedPosition = getSavedImagePosition();
        viewPager.setCurrentItem(savedPosition);

        main_adapter adapter = new main_adapter(binding.getRoot().getContext(), combinedList);
        binding.notesListRecycler.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(binding.getRoot().getContext(), 2);
        binding.notesListRecycler.setLayoutManager(gridLayoutManager);
        binding.addNewTask.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, create_tasks.class));
            finish();
        });
        getValuePutToAdapter(adapter);
    }
    private void saveCurrentImagePosition(int position) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("imagePosition", position);
        editor.apply();
    }
    private int getSavedImagePosition() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return preferences.getInt("imagePosition", 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getValuePutToAdapter(main_adapter adapter) {
        combinedList.clear();
        firestore.collection("users").document(authID).collection("All")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (allList.isEmpty()) {
                                allList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : allList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    allList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(allList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        firestore.collection("users").document(authID).collection("Private")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        privateList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (privateList.isEmpty()) {
                                privateList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : privateList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    privateList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(privateList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        firestore.collection("users").document(authID).collection("Work")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        workList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (workList.isEmpty()) {
                                workList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : workList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    workList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(workList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        firestore.collection("users").document(authID).collection("Trip")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tripList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (tripList.isEmpty()) {
                                tripList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : tripList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    tripList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(tripList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        firestore.collection("users").document(authID).collection("Study")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studyList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (studyList.isEmpty()) {
                                studyList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : studyList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    studyList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(studyList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        firestore.collection("users").document(authID).collection("Home")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        homeList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (homeList.isEmpty()) {
                                homeList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : homeList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    homeList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(homeList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        firestore.collection("users").document(authID).collection("Music")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        musicList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (musicList.isEmpty()) {
                                musicList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : musicList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    musicList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(musicList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        firestore.collection("users").document(authID).collection("Shopping")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        shoppingList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            main_list mainList = document.toObject(main_list.class);
                            mainList.setTaskCount(mainList.getTaskCount() + 1);
                            if (shoppingList.isEmpty()) {
                                shoppingList.add(mainList);
                            } else {
                                boolean categoryExists = false;
                                for (main_list listItem : shoppingList) {
                                    if (listItem.getCategory().equals(mainList.getCategory())) {
                                        listItem.setTaskCount(listItem.getTaskCount() + 1);
                                        categoryExists = true;
                                        break;
                                    }
                                }
                                if (!categoryExists) {
                                    shoppingList.add(mainList);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        // Instead of directly adding items to combinedList, use synchronized block
                        synchronized (combinedList) {
                            combinedList.addAll(shoppingList);
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void setFabColor() {
        @SuppressLint("UseCompatLoadingForDrawables")
        int position = getSavedImagePosition();
        if (position==0){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.male1)).getBitmap();
        } else if (position==1){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.female1)).getBitmap();
        } else if (position==7){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.female4)).getBitmap();
        } else if (position==2){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.male2)).getBitmap();
        } else if (position==3){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.female2)).getBitmap();
        } else if (position==4){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.male3)).getBitmap();
        } else if (position==5){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.female3)).getBitmap();
        } else if (position==6){
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.male4)).getBitmap();
        }

        Palette.from(bitmap).generate(palette -> {
            // Get the dominant color
            assert palette != null;
            int dominantColor = palette.getDominantColor(getResources().getColor(R.color.dark_sky_blue));
            binding.addNewTask.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dominantColor));
            boolean isLight = ColorUtils.calculateLuminance(dominantColor) > 0.3;

            // Set the text color based on the background color
            int textColor = isLight ? getResources().getColor(R.color.dark) : getResources().getColor(R.color.white);
            binding.addNewTask.setTextColor(textColor);
            binding.addNewTask.setIconTint(android.content.res.ColorStateList.valueOf(textColor));
        });
    }

    @SuppressLint("SetTextI18n")
    protected void setNameUser() {
        String name = Objects.requireNonNull(user).getDisplayName();
        if (name != null && name.isEmpty()) {
            name = "User";
        }
        String trimmedName = "";
        if (name != null) {
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) != ' ') {
                    trimmedName = trimmedName.concat(String.valueOf(name.charAt(i)));
                } else {
                    break;
                }
            }
        }
        binding.welcomeName.setText("Hello there,\n" + trimmedName);
    }

    private void setSystemBarColors() {
        int nightModeFlags = binding.getRoot().getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            Window window = this.getWindow();
            int flags = window.getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }
}