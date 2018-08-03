package com.viedmapp.checkcode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.viedmapp.checkcode.AsyncTasks.AsyncResponse;
import com.viedmapp.checkcode.AsyncTasks.DataHandshake;

import java.util.ArrayList;

public class ProjectActivity extends AppCompatActivity implements AsyncResponse{

    static final String S_USER = "S_USER";
    static final String S_EMAIL = "S_EMAIL";
    static final String S_SHEET_ID = "saved_sheetID";
    static final int PICK_EVENT_REQUEST = 1;

    private static String userID;
    private static String email;
    private String sheetName;
    private String sheetID;
    private SharedPreferences prefs;


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(S_SHEET_ID,sheetID);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs=getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final String userID=prefs.getString("user_id",null);
        final String email=prefs.getString("email",null);

        /*
        if(savedInstanceState!=null){
            userID = savedInstanceState.getString(S_USER);
            email = savedInstanceState.getString(S_EMAIL);
        }else{
            userID = userID!=null?userID:getIntent().getStringExtra("userID");
            email = email!=null?email:getIntent().getStringExtra("email");
        }
        */




        Button mScanner = findViewById(R.id.btnScanner);
        mScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(sheetID !=null) {
                    Intent intent = new Intent(ProjectActivity.this, ScanActivity.class);
                    intent.putExtra("sheetID", sheetID);
                    startActivity(intent);
                }else{
                    Toast.makeText(ProjectActivity.this,
                            "Evento no seleccionado",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        ImageButton mShowDialog = findViewById(R.id.btnShowDialog);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ProjectActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.pop_up, null);
                final EditText mName = mView.findViewById(R.id.proyect_name);
                Button mAccept = mView.findViewById(R.id.acept_button);
                Button mCancel= mView.findViewById(R.id.cancel_button);



            /* mBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                mBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
               });
               */
               mBuilder.setView(mView);
               final AlertDialog dialog = mBuilder.create();
               dialog.show();

               mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!mName.getText().toString().isEmpty()){


                            sheetName = mName.getText().toString();

                            if(email!=null){
                                sendData(email,sheetName);
                            }else{
                                Toast.makeText(ProjectActivity.this,
                                        "Email no válido",
                                        Toast.LENGTH_LONG).show();
                            }



                            dialog.dismiss();

                        }

                        else{
                            Toast.makeText(ProjectActivity.this,
                                    "No coloco ningun nombre",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

//                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (!mEmail.getText().toString().isEmpty() && !mPassword.getText().toString().isEmpty()) {
//                            Toast.makeText(MainActivity.this,
//                                    R.string.success_login_msg,
//                                    Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(MainActivity.this, Main2Activity.class));
//                            dialog.dismiss();
//                        } else {
//                            Toast.makeText(MainActivity.this,
//                                    R.string.error_login_msg,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
        });

        Button mEventButton = findViewById(R.id.btn_view_events);
        mEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userID !=null) {

                    Intent intent = new Intent(view.getContext(), EventScrollingActivity.class);
                    intent.putExtra("userID", userID);

                    startActivityForResult(intent, PICK_EVENT_REQUEST);

                }else{
                    Toast.makeText(ProjectActivity.this,
                            "Usuario invalido",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendData(String email, String sheetName) {
        DataHandshake dataHandshake = new DataHandshake(this);
        dataHandshake.delegate = this;
        String script = "https://script.google.com/macros/s/AKfycbxOGgyOnrUR8-GhUmWme21mFdfWyW1QKf070RQ0tmgWXyf2PlY-/exec?editorr=";
        dataHandshake.execute(script + email + "&name=" + sheetName);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();

    }
    /*
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        userID = savedInstanceState.getString(S_USER);
        email = savedInstanceState.getString(S_EMAIL);
    }
    */

    @Override
    public void processFinish(ArrayList<String> arrayList) {
        String sheetUrl = arrayList.get(1);
        String sheetID = arrayList.get(2);
        final String userID=prefs.getString("user_id",null);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("Events");
        mDatabase.child(sheetName).child("sheetID").setValue(sheetID);
        mDatabase.child(sheetName).child("Url").setValue(sheetUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if(requestCode == PICK_EVENT_REQUEST){
            if(resultCode == RESULT_OK){
                sheetID = data.getStringExtra("sheetID");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

