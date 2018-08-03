package com.viedmapp.checkcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class SplashScreen extends Activity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        prefs=getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final String id=prefs.getString("user_id",null);


        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (id != null) {
                    Intent intent = new Intent(SplashScreen.this, ProjectActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },3000);
    }
}
