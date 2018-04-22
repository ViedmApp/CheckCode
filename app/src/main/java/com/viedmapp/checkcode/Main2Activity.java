package com.viedmapp.checkcode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;

public class Main2Activity extends AppCompatActivity {

    private  static  final int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        verifyPermissions();

    }



    private void verifyPermissions(){
        Log.d("MainActivity","VerifyPermissions: asking user for permissions");
        String[] permissions ={Manifest.permission.CAMERA};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0])== PackageManager.PERMISSION_GRANTED){

        }
        else{
            ActivityCompat.requestPermissions(Main2Activity.this,permissions,REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        verifyPermissions();
    }


    public void realMode(View view){
        Intent intent = new Intent(Main2Activity.this, MainActivity.class);
        intent.putExtra("tof",true);
        startActivity(intent);
    }

    public void testMode(View view){
        Intent intent = new Intent(Main2Activity.this, MainActivity.class);
        intent.putExtra("tof",false);
        startActivity(intent);
    }
}