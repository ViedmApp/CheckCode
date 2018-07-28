package com.viedmapp.checkcode.Recycler;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viedmapp.checkcode.R;

import java.util.ArrayList;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder>{
    private ArrayList<String> mDataset;
    private int resource;

    public EventRecyclerAdapter(ArrayList<String> mDataset, int resource) {
        this.mDataset = mDataset;
        this.resource = resource;
    }

    @Override
    public EventRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    //ViewHolderClass
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.event_textView);
        }
    }
}
