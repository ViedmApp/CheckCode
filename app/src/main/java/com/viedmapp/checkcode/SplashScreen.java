package com.viedmapp.checkcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new android.os.Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent miIntent = new Intent(SplashScreen.this , MainActivity.class);
                startActivity(miIntent);
                finish();
            }
        },3000);
    }
}
