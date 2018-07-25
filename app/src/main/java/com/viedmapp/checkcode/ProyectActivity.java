package com.viedmapp.checkcode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProyectActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth fireBaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mScanner=(Button) findViewById(R.id.btnScanner);

        mScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProyectActivity.this,ScanActivity.class);

                startActivity(intent);


            }
        });
        ImageButton mShowDialog = findViewById(R.id.btnShowDialog);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ProyectActivity.this);
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
                            Toast.makeText(ProyectActivity.this,
                                    "Su nombre esta bien",
                                    Toast.LENGTH_SHORT).show();
                            String username = getIntent().getStringExtra("userID");
                            mDatabase = FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("Users").child(username).child(mName.getText().toString()).setValue("asjdas");

                            Toast.makeText(ProyectActivity.this,username,Toast.LENGTH_SHORT).show();


                            dialog.dismiss();

                        }

                        else{
                            Toast.makeText(ProyectActivity.this,
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
    }
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().signOut();

    }
}

