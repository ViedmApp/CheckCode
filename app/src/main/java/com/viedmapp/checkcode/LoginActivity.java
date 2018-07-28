package com.viedmapp.checkcode;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private FirebaseAuth fireBaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient= new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        SignInButton signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,777);
            }
        });

        fireBaseAuth=FirebaseAuth.getInstance();
        firebaseAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null){
                    goMainScreen();
                }
            }
        };


        }

    @Override
    protected void onStart() {
        super.onStart();
        fireBaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    /*private void saveUserInfo() {
        String nombre = "mathias";
        String adress = "moyarzun";
        UserInfo userInfo = new UserInfo(nombre, adress);

        FirebaseUser user = fireBaseAuth.getCurrentUser();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            databaseReference.child(user.getUid()).setValue(userInfo);
        }
    }
    */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777) {
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.e("GoogleSignInResult", result.toString());
            handleSigninResult(result);

        }
    }

    private void handleSigninResult(GoogleSignInResult result) {
        if(result.isSuccess()){
            fireBaseAuthWithGoogle(result.getSignInAccount());


        }

        else{
            Toast.makeText(this,"No se puede iniciar sesion",Toast.LENGTH_SHORT).show();
        }


    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount signInAccount) {
        AuthCredential credential= GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);
        fireBaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"No se pudo autentificar",Toast.LENGTH_SHORT).show();

                }
                else{
                    boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();

                    DatabaseReference mDatabase;
                    String user_id=fireBaseAuth.getCurrentUser().getUid();
                    String email=fireBaseAuth.getCurrentUser().getEmail();
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    if(isNew) {
                        Toast.makeText(LoginActivity.this,"falso",Toast.LENGTH_SHORT).show();

                        DatabaseReference current_user=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                        Map<String, String> newPost = new HashMap<>();
                        newPost.put("correo", email);
                        current_user.setValue(newPost);
                    }
                    else{
                        Toast.makeText(LoginActivity.this,"verdadero",Toast.LENGTH_SHORT).show();

                    }






                }
            }
        });

    }

    private void goMainScreen() {
    Intent intent=new Intent(this,ProjectActivity.class);
    String user_id=fireBaseAuth.getCurrentUser().getUid();

    intent.putExtra("userID", user_id);
    intent.putExtra("email", fireBaseAuth.getCurrentUser().getEmail());
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().signOut();
        if(firebaseAuthListener!=null){
            fireBaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
}


