package com.gagandeep.databasesync;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    public RecyclerAdapter(ArrayList<Diary> arrayList) {
        this.arrayList = arrayList;
    }

    private ArrayList<Diary> arrayList = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.titleTextView.setText(arrayList.get(i).getTitle());
        viewHolder.descriptionTextView.setText(arrayList.get(i).getDescription());
        viewHolder.updatedOnTextView.setText(arrayList.get(i).getUpdatedOn());
        int syncStatus = arrayList.get(i).getSyncstatus();
        if(syncStatus == DbContract.SYNC_STATUS_OK)
            viewHolder.syncStatus.setImageResource(R.drawable.ic_done);
        else
            viewHolder.syncStatus.setImageResource(R.drawable.ic_sync);



    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView syncStatus;
        TextView titleTextView, descriptionTextView, updatedOnTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            syncStatus = itemView.findViewById(R.id.imageViewStatus);
            titleTextView = itemView.findViewById(R.id.textViewName);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
            updatedOnTextView = itemView.findViewById(R.id.textViewUpdatedOn);
        }
    }
}
