package com.suraj.todo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.suraj.todo.Adapters.sub_list_adapter;
import com.suraj.todo.objects.item_list;

import com.suraj.todo.databinding.ActivityCategoryListViewBinding;

import java.util.ArrayList;

public class category_list_view extends AppCompatActivity {
    ActivityCategoryListViewBinding binding;
    ArrayList<item_list> list = new ArrayList<>();
    String authID;
    FirebaseAuth auth;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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


        assert category != null;
        firestore.collection("users").document(authID).collection(category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        list.clear();
                        for (QueryDocumentSnapshot document : task.getResult()){
                            item_list itemList = document.toObject(item_list.class);
                            System.out.println(itemList.isCompleted());
                            itemList.setDocID(document.getId());
                            itemList.setCateg(category);
                            list.add(itemList);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    else {
                        Log.d(TAG,"Task error ",task.getException());
                    }
                });

    }
}