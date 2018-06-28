package com.viedmapp.checkcode;


import android.content.DialogInterface;
import android.content.Intent;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements AsyncResponse, ZXingScannerView.ResultHandler, TextToSpeech.OnInitListener{
    private static final int MY_DATA_CHECK_CODE = 1;
    private TextToSpeech mTts;
    private ZXingScannerView escanerView;
    private boolean isFlash;
    private boolean isVoiceActive;
    private String scannedData;

    static private String name ="";
    static private int quantity;
    static private String ticketID;

    HashMap<String, String> params = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        escanerView=new ZXingScannerView(this);
        escanerView.startCamera();
        setContentView(R.layout.activity_scan);
        showCameraLayout(R.id.camera_preview);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    public boolean receivedBoolean() {
        Bundle values = getIntent().getExtras();
        return (values != null) && values.getBoolean("tof");

    }


    @Override
    public void handleResult(Result result) {
        try {

            //Send data to GoogleSheet
            if (receivedBoolean()) {
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
        escanerView.setResultHandler(this);
        toggleButton(R.id.scan_button,R.id.goBack_button);
        escanerView.setFlash(isFlash);
    }

    public void toggleFlash(View view){
        //Toggle Flashlight
        isFlash = !isFlash;
        escanerView.setFlash(isFlash);
        setButtonFilter(R.id.flashlight_button, isFlash);
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
        setButtonFilter(R.id.flashlight_button, escanerView.getFlash());
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

    public void onInit(int i) {
        mTts.setLanguage(new Locale(Locale.getDefault().getLanguage()));
    }

    private void showReceivedData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);

        //Layout Inflater
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_response, (LinearLayout)findViewById(R.id.dialogResponseLayout));

        //Update text in layout
        final TextView ticketView = dialogLayout.findViewById(R.id.dialog_ticket_view);
        ticketView.append(ticketID);
        final TextView nameView = dialogLayout.findViewById(R.id.dialog_name_view);
        nameView.append(name);
        final TextView cantView = dialogLayout.findViewById(R.id.dialog_cantidad_view);
        cantView.append(String.valueOf(quantity));

        //Text_To_Speech Results
        final TextView resultView = dialogLayout.findViewById(R.id.dialog_status_view);
        if (name!= null && name.equalsIgnoreCase("#N/A")){
            //DECIR INVALIDO
            resultView.setText(getString(R.string.ticket_invalid).toUpperCase());
            //Speak result
            mTts.speak("Entrada inválida", TextToSpeech.QUEUE_FLUSH, params);
        }else if (quantity >=1){
            //DECIR INCORRECTO POR CANTIDAD
            resultView.setText(getString(R.string.ticket_scanned).toUpperCase());
            //Speak result
            mTts.speak("Error", TextToSpeech.QUEUE_FLUSH, params);
        }else{
            resultView.setText(getString(R.string.ticket_valid).toUpperCase());
            //DECIR EL NOMBRE DE LA ENTRADA
            //Speak result
            mTts.speak(name, TextToSpeech.QUEUE_FLUSH, params);
            new SendData(scannedData).execute();
        }

        builder.setView(dialogLayout);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
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