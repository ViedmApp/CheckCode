package com.viedmapp.checkcode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.viedmapp.checkcode.AsyncTasks.DataRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ProjectActivity extends AppCompatActivity implements AsyncResponse{


    static final int PICK_EVENT_REQUEST = 1;

    private String sheetName;
    private String sheetID;
    private SharedPreferences prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs=getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final String userID=prefs.getString("user_id",null);
        final String email=prefs.getString("email",null);


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


               mBuilder.setView(mView);
               final AlertDialog dialog = mBuilder.create();
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        DataRequest dataRequest = new DataRequest(this);
        dataRequest.delegate = this;
        String script = "https://script.google.com/macros/s/AKfycby1AyUstBCYK83-l8PSg0-DelVVGjhqhkH7DmzpefrVHNdS360/exec?editorr=";
        dataRequest.execute(script + email + "&name=" + sheetName);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    protected void onDestroy() {
        super.onDestroy();

        FirebaseAuth.getInstance().signOut();
    }


    @Override
    public void processFinish(JSONObject jsonObject) {

        String sheetUrl = "";
        String sheetID = "";
        try {
            sheetUrl = jsonObject.getString("Url");
            sheetID = jsonObject.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String userID = prefs.getString("user_id",null);

        assert userID != null;
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

