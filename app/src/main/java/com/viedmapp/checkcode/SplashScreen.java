package com.viedmapp.checkcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new android.os.Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent miIntent = new Intent(SplashScreen.this , LoginActivity.class);
                startActivity(miIntent);
                finish();
            }
        },3000);
    }
}
