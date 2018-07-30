package com.viedmapp.checkcode.Recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.viedmapp.checkcode.EventScrollingActivity;
import com.viedmapp.checkcode.R;

import java.util.ArrayList;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder>{

    private ArrayList<String> mDataSet;
    private int resource;
    private int lastSelected = -1;

    private EventScrollingActivity activity;

    public EventRecyclerAdapter(ArrayList<String> mDataSet, int resource, EventScrollingActivity activity) {
        this.mDataSet = mDataSet;
        this.resource = resource;
        this.activity = activity;
    }

    @Override
    public EventRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mRadioButton.setText(mDataSet.get(position));
        holder.mRadioButton.setChecked(lastSelected == position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    //ViewHolderClass
    class ViewHolder extends RecyclerView.ViewHolder {

        RadioButton mRadioButton;

        ViewHolder(final View itemView) {
            super(itemView);
            mRadioButton = itemView.findViewById(R.id.radioButton);

            mRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastSelected = getAdapterPosition();
                    activity.setEventName(mRadioButton.getText().toString());
                    notifyDataSetChanged();
                }
            });
        }
    }
}
