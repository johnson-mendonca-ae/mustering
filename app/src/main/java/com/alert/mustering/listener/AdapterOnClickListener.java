package com.alert.mustering.listener;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface AdapterOnClickListener<T extends RecyclerView.ViewHolder> {
    void onClick(T viewHolder, View view);
}
