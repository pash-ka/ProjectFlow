package com.hfad.projectflow;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hfad.projectflow.database.Drawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> {

    public interface OnClickListener {
        void onItemClick(int position);
    }
    private List<Drawing> thumbnails;
    private OnClickListener listener;

    public NavDrawerAdapter(List<Drawing> thumbnails, OnClickListener listener) {
        this.thumbnails = thumbnails != null ? thumbnails : new ArrayList<>();
        System.out.println(thumbnails.size());
        this.listener = listener;
    }


    @NonNull
    @Override
    public NavDrawerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NavDrawerAdapter.ViewHolder holder, int position) {
        byte[] thumbnail = thumbnails.get(position).getDrawingData();
        System.out.println(Arrays.toString(thumbnail));
        if (thumbnail != null) {
            holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length));
        }
        holder.imageView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return thumbnails != null ? thumbnails.size() : 0;
    }



    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Drawing> newItems) {
        System.out.println("old: " + thumbnails);
        System.out.println("new: " + newItems);
        this.thumbnails = newItems;
        notifyDataSetChanged(); // Notify RecyclerView of data change
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_thumbnail);
        }
    }

    public List<Drawing> getThumbnails() {
        return thumbnails;
    }
}
