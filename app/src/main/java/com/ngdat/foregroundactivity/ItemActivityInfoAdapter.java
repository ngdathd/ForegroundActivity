package com.ngdat.foregroundactivity;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemActivityInfoAdapter extends RecyclerView.Adapter<ItemActivityInfoAdapter.ItemActivityInfoViewHolder> {

    private final List<ItemActivityInfo> activityList;

    public ItemActivityInfoAdapter(List<ItemActivityInfo> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ItemActivityInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_info, parent, false);
        return new ItemActivityInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemActivityInfoViewHolder holder, int position) {
        ItemActivityInfo item = activityList.get(position);

        holder.textViewAppName.setText(item.getAppName());
        holder.textViewActivityName.setText(item.getActivityName());
        holder.textViewStartTime.setText("Start: " + formatTimestamp(item.getStartTime()));
        holder.textViewEndTime.setText("End: " +
                (item.getEndTime() != -1 ? formatTimestamp(item.getEndTime()) : "Ongoing"));

        try {
            Drawable appIcon = holder.itemView.getContext()
                    .getPackageManager()
                    .getApplicationIcon(item.getPackageName());
            holder.imageViewIcon.setImageDrawable(appIcon);
        } catch (PackageManager.NameNotFoundException e) {
            holder.imageViewIcon.setImageResource(R.drawable.ic_launcher_foreground); // Default icon
        }
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ItemActivityInfoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewIcon;
        TextView textViewAppName, textViewActivityName, textViewStartTime, textViewEndTime;

        public ItemActivityInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewAppName = itemView.findViewById(R.id.textViewAppName);
            textViewActivityName = itemView.findViewById(R.id.textViewActivityName);
            textViewStartTime = itemView.findViewById(R.id.textViewStartTime);
            textViewEndTime = itemView.findViewById(R.id.textViewEndTime);
        }
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
