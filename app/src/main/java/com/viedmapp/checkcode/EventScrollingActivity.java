package com.viedmapp.checkcode;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.viedmapp.checkcode.Recycler.EventRecyclerAdapter;

import java.util.ArrayList;


public class EventScrollingActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> mDataSet = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        mRecyclerView = (RecyclerView)findViewById(R.id.event_recyclerView);

        //Linear Layout Manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new EventRecyclerAdapter(mDataSet,R.layout.text_row_item);
        mDataSet.add("testing");

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            String username = getIntent().getStringExtra("userID");

            Query myEventsDataQuery = myRef.child("Users").child(username).child("Events").orderByKey();

            myEventsDataQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(!mDataSet.contains(snapshot.getKey())){
                            mDataSet.add(snapshot.getKey());
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("loadPost:onCancelled", databaseError.toException().toString());
                }
            });

        }catch(Exception e){

        }
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);



    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
