package com.viedmapp.checkcode;
import android.Manifest;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;


import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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

    static private String name ="";
    static private int quantity;
    static private String ticketID;


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
            switchE = findViewById(R.id.switch_light);
            super.onCreate(savedInstanceState);
            Fabric.with(this, new Crashlytics());
            if(isOnline()) {
                escanerView = new ZXingScannerView(this);
                escanerView.startCamera();
                setContentView(R.layout.activity_scan);
                showCameraLayout(R.id.camera_preview);

                Intent checkIntent = new Intent();
                checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

            }
            else{
                buildDialog(ScanActivity.this).show();

            }
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
        if(item.isChecked()){
            item.setChecked(false);
            typeMode=false;

        }
        else{
            item.setChecked(true);
            typeMode=true;

        }
        return super.onOptionsItemSelected(item);

    }
    @Override
    public void handleResult(Result result) {
        try {

            //Send data to GoogleSheet
            if (!typeMode) {
                scannedData = result.getText();

                DataRequest dataRequest = new DataRequest(scannedData,this);
                dataRequest.delegate = this;
                dataRequest.execute();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        escanerView.setResultHandler(null);
        resetCamera();
        escanerView.setFlash(isFlash);
        toggleButton(R.id.scan_button, R.id.goBack_button);
        setButtons();
    }

    @Override
    public void processFinish(ArrayList<String> arrayList){
        ticketID = arrayList.get(0);
        name = arrayList.get(1);
        quantity = Integer.valueOf(arrayList.get(2));
        showReceivedData();
    }

    public void scannerQR(View view){
        //Scans code
        resetCamera();
        escanerView.setAutoFocus(true);
        escanerView.setResultHandler(this);
        toggleButton(R.id.scan_button,R.id.goBack_button);
        escanerView.setFlash(isFlash);
    }

    //switch de linterna
    public void onclick(View view){
        if(view.getId() == R.id.switch_light){
            isFlash = !isFlash;
            escanerView.toggleFlash();

        }
    }


    protected void onPause(){
        super.onPause();
        if(escanerView!=null)escanerView.stopCamera();
    }

    protected void resetCamera(){
        //stops and starts camera
        escanerView.stopCamera();
        escanerView.startCamera();
    }

    public void showCameraLayout(int ID){
        //Shows escanerView camera on a frame layout
        FrameLayout preview = findViewById(ID);
        preview.addView(escanerView.getRootView());
        escanerView.setFlash(isFlash);
    }

    public void toggleVoiceAlerts(View view){
        isVoiceActive=!isVoiceActive;
        setButtonFilter(R.id.voice_alerts, isVoiceActive);
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

    public void goBack(View view){
        //Stops scan
        escanerView.setResultHandler(null);
        toggleButton(R.id.scan_button, R.id.goBack_button);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        //Layout Inflater
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_response, (RelativeLayout) findViewById(R.id.dialogResponseLayout));

        final TextView ticketView = dialogLayout.findViewById(R.id.dialog_ticket_view);
        ticketView.append(ticketID);
        final TextView nameView = dialogLayout.findViewById(R.id.dialog_name_view);
        nameView.append(name);
        final TextView cantView = dialogLayout.findViewById(R.id.dialog_cantidad_view);
        cantView.append(String.valueOf(quantity));

        Button btnListo = null;
        if (name != null && name.equalsIgnoreCase("#N/A")) {
            //DECIR INVALIDO

            //Speak result
            mTts.speak("Entrada inválida", TextToSpeech.QUEUE_FLUSH, params);
            //Update text in layout
            final TextView resultView = dialogLayout.findViewById(R.id.dialog_status_view);
            resultView.append("Entrada inválida");
            //final ImageView imgNE = (ImageView) dialogLayout.findViewById(R.id.imgNE);
            final TextView txtNE = (TextView) dialogLayout.findViewById(R.id.textNE);
            txtNE.append("La entrada no se encuentra registrada en base de datos. Intente nuevamente");
            btnListo = dialogLayout.findViewById(R.id.button_regresar_scan);
            builder.setView(dialogLayout);
        } else if (quantity >= 1) {
            //DECIR INCORRECTO POR CANTIDAD

            //Speak result
            mTts.speak("Error", TextToSpeech.QUEUE_FLUSH, params);

            final TextView resultView = (TextView) dialogLayout.findViewById(R.id.dialog_status_view);
            resultView.append("Error");
            final TextView txtDuplicada = (TextView) dialogLayout.findViewById(R.id.textNE);
            txtDuplicada.append("La entrada ya ha sido procesada");
            btnListo = dialogLayout.findViewById(R.id.button_regresar_scan);
            builder.setView(dialogLayout);

        } else {
            //DECIR EL NOMBRE DE LA ENTRADA
            //Speak result
            mTts.speak(name, TextToSpeech.QUEUE_FLUSH, params);
            new SendData(scannedData).execute();
            btnListo = dialogLayout.findViewById(R.id.button_regresar_scan);
            builder.setView(dialogLayout);

        }

        final AlertDialog alertDialog = builder.create();

        btnListo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.button_regresar_scan) {
                    alertDialog.dismiss();
                }
            }
        });



        builder.setCancelable(false);
        //AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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