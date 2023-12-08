package com.suraj.todo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.suraj.todo.fingerprint;

import com.suraj.todo.R;
import com.suraj.todo.category_list_view;
import com.suraj.todo.objects.main_list;

import java.util.ArrayList;

public class main_adapter extends RecyclerView.Adapter<main_adapter.MyViewHolder> implements fingerprint.FingerPrintCallBack {
    Context context;
    static ArrayList<main_list> list;
    public main_adapter(Context context, ArrayList<main_list> list){
        this.context = context;
        main_adapter.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_list_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull main_adapter.MyViewHolder holder, int position) {
        main_list mainList = list.get(position);
        holder.title.setText(mainList.getCategory());
        switch (mainList.getCategory()){
            case "All":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.alltask));
                break;
            case "Private":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.personal));
                break;
            case "Work":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.work));
                break;
            case "Shopping":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.cart));
                break;
            case "Trip":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.plane));
                break;
            case "Study":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.book));
                break;
            case "Home":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.house));
                break;
            case "Music":
                holder.imageType.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.music));
                break;
        }
        holder.taskNumbers.setText(String.valueOf(getTaskCountForCategory(mainList.getCategory())));
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, category_list_view.class);
            if (mainList.getCategory().equals("Private")){
                fingerprintAuthentication(holder.getAdapterPosition());
            }
            else {
                intent.putExtra("category",mainList.getCategory());
                intent.putExtra("taskCount",String.valueOf(getTaskCountForCategory(mainList.getCategory())));
                context.startActivity(intent);
            }
        });
    }

    private void fingerprintAuthentication(int position) {
        fingerprint fingerprint = new fingerprint(context);
        fingerprint.fingerprint(context,"unlock",position);
        fingerprint.setfingerprintCallback(main_adapter.this);
    }

    private int getTaskCountForCategory(String category) {
        int count = 0;
        for (main_list task : list) {
            if (task.getCategory().equals(category)) {
                count = task.getTaskCount();
            }
        }
        return count;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onSuccess(String what, int position) {
        main_list mainList = list.get(position);
        if (what.equals("unlock")){
            Intent intent = new Intent(context, category_list_view.class);
            intent.putExtra("category",mainList.getCategory());
            intent.putExtra("taskCount",String.valueOf(getTaskCountForCategory(mainList.getCategory())));
            context.startActivity(intent);
        }
    }


    @Override
    public void onFail(String what,int position) {
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageType;
        TextView title,taskNumbers;
        CardView cardView;
        BiometricManager biometricManager;
        private  MyViewHolder(View itemview) {
            super(itemview);
            imageType = itemview.findViewById(R.id.taskTypeIcon);
            title = itemview.findViewById(R.id.taskTypeTitle);
            taskNumbers = itemview.findViewById(R.id.taskTypeCount);
            cardView = itemview.findViewById(R.id.cardViewMain);
            biometricManager = androidx.biometric.BiometricManager.from(itemView.getContext());
        }
    }
}
