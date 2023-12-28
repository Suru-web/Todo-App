package com.suraj.todo;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.suraj.todo.objects.item_list;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class DueDateWorker extends Worker {
    FirebaseFirestore firestore;
    ArrayList<item_list> list = new ArrayList<>();

    public DueDateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Implement the logic to check due dates and show notifications
        checkDueDatesAndShowNotifications();
        return Result.success();
    }

    private void checkDueDatesAndShowNotifications() {
        Timestamp timestamp = Timestamp.now();
        ArrayList<item_list> list = new ArrayList<>();

        checkCollectionForDueDates("All", timestamp,list);
        checkCollectionForDueDates("Private", timestamp,list);
        checkCollectionForDueDates("Work", timestamp,list);
        checkCollectionForDueDates("Trip", timestamp,list);
        checkCollectionForDueDates("Study", timestamp,list);
        checkCollectionForDueDates("Home", timestamp,list);
        checkCollectionForDueDates("Music", timestamp,list);
        checkCollectionForDueDates("Shopping", timestamp,list);
    }

    private void checkCollectionForDueDates(String collectionName, Timestamp timestamp,ArrayList<item_list> list) {
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection(collectionName)
                .whereEqualTo("timeStamp", timestamp)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.d("Error", error.toString());
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            list.add(documentSnapshot.toObject(item_list.class));
                            System.out.println(list.size());
                        }
                    }

                    if (list.size() > 0) {
                        showNotification("You have some Due Todo to complete", "Due date is today!");
                    }
                });
    }

    private void showNotification(String title, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel1", "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel1")
                .setSmallIcon(R.drawable.todo)
                .setContentTitle(title)
                .setContentText(content);

        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
        notificationManager.notify(1, builder.build());
    }
}
