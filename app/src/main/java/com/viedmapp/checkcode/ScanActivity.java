package com.viedmapp.checkcode;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.Result;
import com.viedmapp.checkcode.AsyncTasks.AsyncResponse;
import com.viedmapp.checkcode.AsyncTasks.DataRequest;
import com.viedmapp.checkcode.AsyncTasks.SendData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements AsyncResponse, ZXingScannerView.ResultHandler, TextToSpeech.OnInitListener{
    private static final int MY_DATA_CHECK_CODE = 1;
    private TextToSpeech mTts;
    private ZXingScannerView escanerView;
    private  static  final int REQUEST_CODE=1;

    private boolean isFlash;
    private boolean isVoiceActive;
    private String scannedData;
    private boolean typeMode;
    Switch switchE;
    private SharedPreferences prefs;

    static private String name ="";
    static private int quantity;
    static private String ticketID;
    static private String sheetID;



    HashMap<String, String> params = new HashMap<>();


    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (Exception e)          { e.printStackTrace(); }

        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
            verifyPermissions();
            prefs=getSharedPreferences("Preferences", Context.MODE_PRIVATE);





        //switchE = findViewById(R.id.switch_light);
            super.onCreate(savedInstanceState);
            Fabric.with(this, new Crashlytics());
            if(isOnline()) {
                escanerView = new ZXingScannerView(this) {


                    protected IViewFinder createViewFinderView(Context abc) {
                        return new CameraPreview(abc);
                    }
                };
                escanerView.startCamera();
                setContentView(R.layout.activity_scan);
                showCameraLayout(R.id.camera_preview);
                Button mAcept = findViewById(R.id.btn_aceppt);
                mAcept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final EditText mCode = findViewById(R.id.code_name);
                        if (!mCode.getText().toString().isEmpty()) {
                            handleResultManual(mCode.getText().toString());
                        }


                        else{
                            Toast.makeText(ScanActivity.this,
                                    "No a ingresado nada",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                sheetID = getIntent().getStringExtra("sheetID");

                Intent checkIntent = new Intent();
                checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

            }
            else{
                buildDialog(ScanActivity.this).show();

            }
    }

    protected void onStart(){
        super.onStart();
        resetCamera();
    }

    protected void onResume(){
        super.onResume();
        resetCamera();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No hay conexión a internet");
        builder.setMessage("Necesecitas estar conectado a datos móviles o a una red WiFi. Presiona Ok para salir");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        return builder;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mode_test:
                if (item.isChecked()) {
                    item.setChecked(false);
                    typeMode = false;
                    return true;

                } else {
                    item.setChecked(true);
                    typeMode = true;
                    return true;

                }
            case R.id.logout:
                sendToLogin();
                return true;

            default:
                return super.onOptionsItemSelected(item);


        }
    }
    public void torch(View view){
        //Toggle Flashlight
        isFlash = !isFlash;
        escanerView.setFlash(isFlash);
        setButtonFilter(R.id.linterna, isFlash);
    }



    private void sendToLogin() {
        GoogleSignInClient mGoogleSignInClient ;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(ScanActivity.this,
                new OnCompleteListener<Void>() {  //
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ScanActivity.this, LoginActivity.class);
                        Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG).show();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
    }


    public void handleResult(Result result) {
        try {

            //Send data to GoogleSheet
            if (!typeMode) {




                scannedData = result.getText();

                DataRequest dataRequest = new DataRequest(this);
                dataRequest.delegate = this;
                String requestScript = "https://script.google.com/macros/s/AKfycbx9yWevNhKhStGaDDPA3VPmmaY5XkUnjh24Z-MlTMK5Pq4hBn4/exec?idSheet=";
                dataRequest.execute(requestScript + sheetID + "&sdata=" + scannedData);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        resetCamera();
        escanerView.setFlash(isFlash);
        setButtons();
    }

    public void handleResultManual(String result) {
        try {

            //Send data to GoogleSheet
            if (!typeMode) {

                scannedData = result;

                DataRequest dataRequest = new DataRequest(this);
                dataRequest.delegate = this;
                String requestScript = "https://script.google.com/macros/s/AKfycbx9yWevNhKhStGaDDPA3VPmmaY5XkUnjh24Z-MlTMK5Pq4hBn4/exec?idSheet=";
                dataRequest.execute(requestScript + sheetID + "&sdata=" + scannedData);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        resetCamera();
        escanerView.setFlash(isFlash);
        setButtons();
    }

    @Override
    public void processFinish(ArrayList<String> arrayList){
        ticketID = arrayList.get(0);
        name = arrayList.get(1);
        quantity = (arrayList.get(2).equalsIgnoreCase("#N/A"))?10:
                Integer.valueOf(arrayList.get(2));
        showReceivedData();
    }

    //Deprecated
    public void scannerQR(View view){
        //Scans code
        resetCamera();
        escanerView.setAutoFocus(true);
        escanerView.setResultHandler(this);
        //escanerView.setFlash(isFlash);
    }

    //switch de linterna


    protected void onPause(){
        super.onPause();
        if(escanerView!=null)escanerView.stopCamera();
    }

    protected void resetCamera(){
        //stops and starts camera
        escanerView.stopCamera();
        escanerView.startCamera();
        escanerView.setResultHandler(this);
    }

    public void showCameraLayout(int ID){
        //Shows escanerView camera on a frame layout
        FrameLayout preview = findViewById(ID);
        preview.addView(escanerView.getRootView());
        //escanerView.setFlash(isFlash);
    }

    public void toggleVoiceAlerts(View view){
        isVoiceActive=!isVoiceActive;
        ImageButton imageButton = findViewById(R.id.voice_alerts);
        imageButton.setImageResource(isVoiceActive?R.drawable.ic_sharp_volume_up_24px:
            R.drawable.ic_sharp_volume_off_24px);
        imageButton.setColorFilter(imageButton.getColorFilter());
        if(isVoiceActive) {
            params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0");
        }else {
            params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1");
        }

    }

    protected void setButtons(){
        //Sets Color filters for flashlight and voice alerts buttons

        setButtonFilter(R.id.voice_alerts, isVoiceActive);
    }

    protected void setButtonFilter(int ID, boolean isActive){
        //Changes icon color filter between black(inactive) and white
        ImageButton imageButton = findViewById(ID);
        imageButton.setColorFilter(getResources().getColor(isActive?R.color.button_on :R.color.button_off));
    }


    @Deprecated
    protected void toggleButton(int ID1, int ID2){
        //Toggles visibility of 2 buttons given their ID
        toggleButton(ID1);

        toggleButton(ID2);
    }

    protected void toggleButton(int ID){
        //Toggles visibility of a button
        int visible = findViewById(ID).getVisibility();
        findViewById(ID).setVisibility(visible==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
    }


    private void verifyPermissions(){
        Log.d("ScanActivity","VerifyPermissions: asking user for permissions");
        String[] permissions ={Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanActivity.this,permissions,REQUEST_CODE);
                }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        verifyPermissions();
    }
    public void onInit(int i) {
        mTts.setLanguage(new Locale(Locale.getDefault().getLanguage()));
    }


    private void showReceivedData() {

        //Layout Inflater
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_response, (LinearLayout)findViewById(R.id.dialogResponseLayout));

        final TextView ticketView = dialogLayout.findViewById(R.id.dialog_ticket_view);
        ticketView.append(ticketID);
        final TextView nameView = dialogLayout.findViewById(R.id.dialog_name_view);
        nameView.append(name);
        final TextView cantView = dialogLayout.findViewById(R.id.dialog_cantidad_view);
        cantView.append(String.valueOf(quantity));

        final TextView resultView = dialogLayout.findViewById(R.id.dialog_status_view);
        final TextView txtNE = (TextView) dialogLayout.findViewById(R.id.textNE);

        if (name != null && name.equalsIgnoreCase("#N/A")) {
            //DECIR INVALIDO

            //Speak result
            mTts.speak("Entrada inválida", TextToSpeech.QUEUE_FLUSH, params);

            //Update text in layout
            resultView.append("Entrada inválida");
            txtNE.append("La entrada no se encuentra registrada en base de datos. Intente nuevamente");
        } else if (quantity < 1) {
            //DECIR INCORRECTO POR CANTIDAD

            //Speak result
            mTts.speak("Error", TextToSpeech.QUEUE_FLUSH, params);

            //Update text in layout
            resultView.append("Error");
            txtNE.append("Se ha agotado el stock para este código");

        } else {
            //DECIR EL NOMBRE DE LA ENTRADA
            //Speak result
            mTts.speak(name, TextToSpeech.QUEUE_FLUSH, params);

            //Update text in layout
            resultView.append("Entrada Válida");
            txtNE.append("Bienvenido");

            ImageView imageView = dialogLayout.findViewById(R.id.imgNE);
            imageView.setImageResource(R.drawable.ic_check_circle_black_24dp);


            new SendData(scannedData).execute(sheetID);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        Button btnListo = dialogLayout.findViewById(R.id.button_regresar_scan);
        btnListo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.button_regresar_scan) {
                    alertDialog.dismiss();
                }
            }
        });

        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                mTts = new TextToSpeech(this, this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

}