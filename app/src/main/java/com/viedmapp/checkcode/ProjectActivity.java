package com.viedmapp.checkcode;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.viedmapp.checkcode.AsyncTasks.DataRequest;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ProjectActivity extends AppCompatActivity implements AsyncResponse{

    private DatabaseReference mDatabase;
    private FirebaseAuth fireBaseAuth;
    private static String username;
    private static String email;
    private String script = "https://script.google.com/macros/s/AKfycbxOGgyOnrUR8-GhUmWme21mFdfWyW1QKf070RQ0tmgWXyf2PlY-/exec?editorr=";
    private String sheetUrl;
    private String sheetID;
    private String sheetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mScanner=(Button) findViewById(R.id.btnScanner);

        mScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProjectActivity.this,ScanActivity.class);

                startActivity(intent);


            }
        });
        ImageButton mShowDialog = findViewById(R.id.btnShowDialog);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ProjectActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.pop_up, null);
                final EditText mName = (EditText) mView.findViewById(R.id.proyect_name);
                Button mLogin = (Button) mView.findViewById(R.id.acept_button);
                Button mCancel=(Button) mView.findViewById(R.id.cancel_button);



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

                mLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!mName.getText().toString().isEmpty()){
                            Toast.makeText(ProjectActivity.this,
                                    "Su nombre esta bien",
                                    Toast.LENGTH_SHORT).show();
                            username = getIntent().getStringExtra("userID");
                            email = getIntent().getStringExtra("email");

                            sendData(email,mName.getText().toString());
                            sheetName = mName.getText().toString();

                            dialog.dismiss();

                        }

                        else{
                            Toast.makeText(ProjectActivity.this,
                                    "No coloco n ingun nibre",
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
                Intent intent = new Intent(ProjectActivity.this, EventScrollingActivity.class);
                intent.putExtra("userID",getIntent().getStringExtra("userID"));
                startActivity(intent);
            }
        });
    }

    private void sendData(String email, String sheetName) {
        DataHandshake dataHandshake = new DataHandshake();
        dataHandshake.delegate = this;
        try {
            dataHandshake.execute(script + email + "&name=" + sheetName).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();

    }

    @Override
    public void processFinish(ArrayList<String> arrayList) {
        sheetUrl = arrayList.get(1);
        sheetID = arrayList.get(2);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(username).child("Events").child(sheetName).setValue(sheetID);
    }
}

