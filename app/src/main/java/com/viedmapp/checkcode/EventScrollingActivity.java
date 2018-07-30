package com.viedmapp.checkcode;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.viedmapp.checkcode.Recycler.EventRecyclerAdapter;

import java.util.ArrayList;
import java.util.Objects;


public class EventScrollingActivity extends AppCompatActivity {

    DatabaseReference myRef;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> mDataSet = new ArrayList<>();

    private String eventName;
    private String sheetID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        RecyclerView mRecyclerView = findViewById(R.id.event_recyclerView);

        //Linear Layout Manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new EventRecyclerAdapter(mDataSet,R.layout.text_row_item, this);

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String username = getIntent().getStringExtra("userID");
            myRef = database.getReference().child("Users").child(username).child("Events");

            Query myEventsDataQuery = myRef.orderByKey();

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
            e.printStackTrace();
        }


        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

    }

    public void setEventName(final String eventName) {
        this.eventName = eventName;

        Query myQuery = myRef.child(eventName).orderByKey();
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    if(Objects.equals(snapshot.getKey(), "sheetID")){
                        sheetID = Objects.requireNonNull(snapshot.getValue()).toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnResultToActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                returnResultToActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void returnResultToActivity(){
        Intent returnIntent = getIntent();
        returnIntent.putExtra("sheetID", sheetID);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
