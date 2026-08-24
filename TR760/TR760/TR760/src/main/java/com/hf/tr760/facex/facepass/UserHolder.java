package com.hf.tr760.facex.facepass;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.tr760.R;

public class UserHolder extends RecyclerView.ViewHolder {
    public ImageView head;
    public TextView name;
    public TextView id;

    public UserHolder(@NonNull View itemView) {
        super(itemView);

        head = itemView.findViewById(R.id.head);
        name = itemView.findViewById(R.id.name);
        id = itemView.findViewById(R.id.id);
    }
}
