package com.suraj.todo.Adapters;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suraj.todo.R;
import com.suraj.todo.objects.item_list;

import java.util.ArrayList;
import java.util.Objects;

public class sub_list_adapter extends RecyclerView.Adapter<sub_list_adapter.viewHolder> {
    Context context;
    static ArrayList<item_list> list;
    ExtendedFloatingActionButton fab;
    int deleteTaskPosition = -1;
    Vibrator vibrator ;
    public sub_list_adapter(Context context,ArrayList<item_list> list,ExtendedFloatingActionButton fab){
        this.context = context;
        sub_list_adapter.list = list;
        this.fab = fab;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    @NonNull
    @Override
    public sub_list_adapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sub_list_layout,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull sub_list_adapter.viewHolder holder, @SuppressLint("RecyclerView") int position) {
        item_list itemList = list.get(position);
        holder.task.setText(String.valueOf(itemList.getTask()));
        String date = itemList.getDate()+" "+retMonth(itemList.getMonth())+", "+itemList.getYear();
        holder.date.setText(date);
        ColorStateList color = holder.cardView.getCardBackgroundColor();
        if (itemList.isCompleted()){
            setTickedStyle(holder,true);
            holder.checkBox.setChecked(true);
        }
        holder.checkBox.setOnClickListener(v -> {
            vibrator.vibrate(10);
            if (holder.checkBox.isChecked()){
                FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(itemList.getCateg()).document(itemList.getDocID()).update("completed",true);
                itemList.setCompleted(true);
                setTickedStyle(holder,true);
            }
            else {
                FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(itemList.getCateg()).document(itemList.getDocID()).update("completed",false);
                itemList.setCompleted(false);
                setTickedStyle(holder,false);
            }
        });
        holder.cardView.setOnLongClickListener(v -> {
            deleteTaskStyle(holder,color);
            deleteTaskPosition = position;
            return false;
        });
        fab.setOnClickListener(v -> deleteTask(deleteTaskPosition));

    }

    private String retMonth(int monthNum){
        String month = "Haha";
        switch (monthNum){
            case 0: month = "Jan";
            break;
            case 1: month = "Feb";
                break;
            case 2: month = "Mar";
                break;
            case 3: month = "April";
                break;
            case 4: month = "May";
                break;
            case 5: month = "June";
                break;
            case 6: month = "July";
                break;
            case 7: month = "Aug";
                break;
            case 8: month = "Sep";
                break;
            case 9: month = "Oct";
                break;
            case 10: month = "Nov";
                break;
            case 11: month = "Dec";
                break;
        }
        return month;
    }

    private void deleteTask(int position) {
        vibrator.vibrate(20);
        item_list itemList = list.get(position);
        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(itemList.getCateg()).document(itemList.getDocID())
                .delete();
        list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0,getItemCount());
        fab.setVisibility(View.GONE);
        deleteTaskPosition = -1;
    }

    private void deleteTaskStyle(viewHolder holder,ColorStateList color){
        vibrator.vibrate(20);
        if (holder.deletePressed){
            fab.setVisibility(View.VISIBLE);
            holder.cardView.setCardBackgroundColor(Color.parseColor("#ff0000"));
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.deletePressed = false;
        } else {
            fab.setVisibility(View.GONE);
            holder.cardView.setCardBackgroundColor(color);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.deletePressed = true;
        }
    }
    private void setTickedStyle(viewHolder holder, boolean checked){
            if (checked) {
                holder.cardView.setAlpha(0.7F);
                holder.task.setTextAppearance(R.style.CHECKBOX_TEXT);
                holder.date.setTextAppearance(R.style.COMPLETED_TASK);
                holder.status.setText(R.string.completed);
                holder.status.setTextAppearance(R.style.COMPLETED_TASK);
                holder.status.setVisibility(View.VISIBLE);
                holder.task.setPaintFlags(holder.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                return;
            }
            holder.cardView.setAlpha(1F);
            holder.task.setTextAppearance(R.style.TEXT_COLOR);
            holder.date.setTextAppearance(R.style.TEXT_COLOR);
            holder.status.setVisibility(View.GONE);
            holder.task.setPaintFlags(holder.task.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView task,date,status,seeNote;
        CardView cardView;
        CheckBox checkBox;
        boolean deletePressed = true;
        public viewHolder(View itemView){
            super(itemView);
            task = itemView.findViewById(R.id.taskTitle);
            date = itemView.findViewById(R.id.taskDate);
            status = itemView.findViewById(R.id.taskDoneOrDelayed);
            checkBox = itemView.findViewById(R.id.taskCompleteCheckBox);
            cardView = itemView.findViewById(R.id.cardViewList);
            seeNote = itemView.findViewById(R.id.taskNote);
        }
    }
}
