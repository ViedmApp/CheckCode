package com.viedmapp.checkcode;

import android.os.AsyncTask;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.zxing.Result;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView escanerView;
    private boolean isFlash;
    private boolean isVoiceActive;
    String scannedData;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        escanerView=new ZXingScannerView(this);
        escanerView.startCamera();
        escanerView.setAutoFocus(false);
        setContentView(R.layout.activity_main);
        showCameraLayout(R.id.camera_preview);
    }

    @Override
    public void handleResult(Result result) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Resultados...");
        builder.setMessage(result.getText());
        AlertDialog alertDialog=builder.create();
        alertDialog.show();

        //Send data to GoogleSheet
        scannedData = result.getText();
        new SendRequest().execute();

        //Toggle Handler OFF
        isFlash=false;
        escanerView.setFlash(false);
        escanerView.setResultHandler(null);
        resetCamera();
        escanerView.setAutoFocus(false);
        toggleButton(R.id.scan_button, R.id.goBack_button);
        setButtons();
    }

    public void ScannerQR(View view){
        resetCamera();
        escanerView.setResultHandler(this);
        toggleButton(R.id.scan_button,R.id.goBack_button);
        escanerView.setFlash(isFlash);
    }

    public void ToggleFlash(View view){
        isFlash = !isFlash;
        escanerView.toggleFlash();
        setButtonFilter(R.id.flashlight_button, escanerView.getFlash());
    }

    protected void onPause(){
        super.onPause();
        if(escanerView!=null)escanerView.stopCamera();
    }

    protected void resetCamera(){
        escanerView.stopCamera();
        escanerView.startCamera();
    }

    public void showCameraLayout(int ID){
        FrameLayout preview = findViewById(ID);
        preview.addView(escanerView.getRootView());
    }

    public void toggleVoiceAlerts(View view){
        isVoiceActive=!isVoiceActive;
        setButtonFilter(R.id.voice_alerts, isVoiceActive);
    }

    protected void setButtons(){
        setButtonFilter(R.id.flashlight_button, escanerView.getFlash());
        setButtonFilter(R.id.voice_alerts, isVoiceActive);
    }

    protected void setButtonFilter(int ID, boolean isActive){
        ImageButton imageButton = findViewById(ID);
        imageButton.setColorFilter(getResources().getColor(isActive?R.color.button_on :R.color.button_off));
    }

    protected void toggleButton(int ID1, int ID2){
        toggleButton(ID1);
        toggleButton(ID2);
    }

    protected void toggleButton(int ID){
        int visible = findViewById(ID).getVisibility();
        findViewById(ID).setVisibility(visible==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
    }

    public void goBack(View view){
        escanerView.setResultHandler(null);
        toggleButton(R.id.scan_button, R.id.goBack_button);
    }

    //InnerClass
    private class SendRequest extends AsyncTask<String, Void, String> {


        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                //Enter script URL Here
                URL url = new URL("https://script.google.com/macros/s/AKfycbydx3sGJ3-xXKzq6clducWjxZkFvDpjxQSiAIiggIHvzxVU6rQZ/exec");

                JSONObject postDataParams = new JSONObject();

                //String usn = Integer.toString(i);

                //Passing scanned code as parameter

                postDataParams.put("sdata",scannedData);


                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return "false : " + responseCode;
                }
            }
            catch(Exception e){
                return "Exception: " + e.getMessage();
            }
        }

    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
