package com.hf.tr760.fingerprint;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.tr760.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncListAdapter extends RecyclerView.Adapter<SyncListAdapter.ViewHolder> {

    private List<PendingEnrollment> list;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    public SyncListAdapter(List<PendingEnrollment> list) {
        this.list = list;
    }

    public void updateData(List<PendingEnrollment> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sync_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingEnrollment item = list.get(position);
        holder.tvName.setText((item.getNom() != null ? item.getNom() : "") + " " + (item.getPrenom() != null ? item.getPrenom() : ""));
        holder.tvDate.setText("Date : " + sdf.format(new Date(item.getTimestamp())));
        
        if (item.isSynchronized()) {
            holder.tvStatus.setText("Synchronisé");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvStatus.setText("Non synchronisé");
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
