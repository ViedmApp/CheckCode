package com.viedmapp.checkcode;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

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
    private boolean isFlash = false;
    String scannedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        escanerView=new ZXingScannerView(this);
        escanerView.startCamera();
        setContentView(R.layout.activity_main);
        showCameraLayout();
    }

    @Override
    public void handleResult(Result result) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Resultados del Scanner");
        builder.setMessage(result.getText());
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
        scannedData = result.getText();
        new SendRequest().execute();
        //escanerView.resumeCameraPreview(this);
        escanerView.stopCamera();
        escanerView.startCamera();
        setContentView(R.layout.activity_main);
        showCameraLayout();
        //escanerView = null;
    }

    public void EscanerQR(View view){
        escanerView.stopCamera();
        escanerView=new ZXingScannerView(this);

        setContentView(escanerView);
        escanerView.setResultHandler(this);
        escanerView.startCamera();
        escanerView.setFlash(isFlash);
    }
    public void ToggleFlash(View view){
        isFlash = !isFlash;
        escanerView.toggleFlash();
    }

    protected void onPause(){
        super.onPause();
        if(escanerView!=null)escanerView.stopCamera();
    }
    public void showCameraLayout(){
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(escanerView.getRootView());
    }
    public class SendRequest extends AsyncTask<String, Void, String> {


        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                //Enter script URL Here
                URL url = new URL("https://script.google.com/macros/s/AKfycbydx3sGJ3-xXKzq6clducWjxZkFvDpjxQSiAIiggIHvzxVU6rQZ/exec");

                JSONObject postDataParams = new JSONObject();

                //int i;
                //for(i=1;i<=70;i++)


                //    String usn = Integer.toString(i);

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
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();

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
        result.toString();
    }
}



