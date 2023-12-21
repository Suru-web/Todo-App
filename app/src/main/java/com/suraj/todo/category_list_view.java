package com.suraj.todo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.suraj.todo.Adapters.sub_list_adapter;
import com.suraj.todo.objects.item_list;

import com.suraj.todo.databinding.ActivityCategoryListViewBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class category_list_view extends AppCompatActivity {
    ActivityCategoryListViewBinding binding;
    ArrayList<item_list> list = new ArrayList<>();
    String authID;
    FirebaseAuth auth;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    TextView sortDueDate, sortCreateTime;

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryListViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        sub_list_adapter adapter = new sub_list_adapter(binding.getRoot().getContext(),list,binding.deleteTask);
        binding.listViewRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.listViewRecycler.setAdapter(adapter);
        Intent intent = getIntent();

        String category = intent.getStringExtra("category");
        String taskCount = intent.getStringExtra("taskCount");
        binding.categoryShow.setText(category);
        binding.taskCountShow.setText(taskCount+" Tasks");

        auth = FirebaseAuth.getInstance();
        authID = auth.getUid();
        binding.backButton.setOnClickListener(v -> finish());
        binding.addTaskListViewActivity.setOnClickListener(v -> {
            Intent intent1 = new Intent(category_list_view.this,create_tasks.class);
            intent1.putExtra("category",category);
            adapter.notifyDataSetChanged();
            startActivity(intent1);
        });

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.menu_popup_window, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        sortDueDate = popupView.findViewById(R.id.dueDateSort);
        sortCreateTime = popupView.findViewById(R.id.createTimeSort);
        binding.menuButton.setOnClickListener(v -> {
            popupWindow.showAsDropDown(v,50,25);
        });
        sortDueDate.setOnClickListener(v -> {
            popupWindow.dismiss();
            sortListDueDate(list,adapter);
        });
        sortCreateTime.setOnClickListener(v -> {
            popupWindow.dismiss();
            sortListByCreationTime(list,adapter);
        });


        assert category != null;
        firestore.collection("users").document(authID).collection(category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        list.clear();
                        for (QueryDocumentSnapshot document : task.getResult()){
                            item_list itemList = document.toObject(item_list.class);
                            itemList.setDocID(document.getId());
                            itemList.setCateg(category);

                            Timestamp timestamp = (Timestamp) document.get("timeStamp");

                            assert timestamp != null;
                            Date date = timestamp.toDate();
                            itemList.setCreationTimestamp(date);
                            list.add(itemList);
                        }
                        sortListByCreationTime(list,adapter);
                    }
                    else {
                        Log.d(TAG,"Task error ",task.getException());
                    }
                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortListDueDate(ArrayList<item_list> list,sub_list_adapter adapter) {
        list.sort(new Comparator<item_list>() {
            @Override
            public int compare(item_list item1, item_list item2) {
                int yearComparison = Integer.compare(item1.getYear(), item2.getYear());
                if (yearComparison != 0) {
                    return yearComparison;
                }
                int monthComparison = Integer.compare(item1.getMonth(), item2.getMonth());
                if (monthComparison != 0) {
                    return monthComparison;
                }
                return Integer.compare(item1.getDate(), item2.getDate());
            }
        });
        adapter.notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void sortListByCreationTime(ArrayList<item_list> list, sub_list_adapter adapter) {
        list.sort(new Comparator<item_list>() {
            @Override
            public int compare(item_list item1, item_list item2) {
                return item1.getCreationTimestamp().compareTo(item2.getCreationTimestamp());
            }
        });
        adapter.notifyDataSetChanged();
    }

}