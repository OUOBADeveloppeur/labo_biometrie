package com.hf.tr760.widget;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.tr760.R;

public class LogViewHolder extends RecyclerView.ViewHolder {
    public TextView info;
    public LogViewHolder(@NonNull View itemView) {
        super(itemView);
        info = itemView.findViewById(R.id.info);
    }
}
