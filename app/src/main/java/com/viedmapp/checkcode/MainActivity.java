package com.viedmapp.checkcode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        if(view.getId() == R.id.btn_view_events) {
            Intent miIntent = new Intent(MainActivity.this, ScanActivity.class);
            startActivity(miIntent);
            finish();
        }
    }

}
