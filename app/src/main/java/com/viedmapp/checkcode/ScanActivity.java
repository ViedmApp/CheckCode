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

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.zxing.Result;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView escanerView;
    private boolean isFlash;
    private boolean isVoiceActive;
    String scannedData;
    private String name;
    private int cantidad;
    private volatile boolean dataReady;
    Button button;
    boolean value;

    private String invalid = "Entrada Invalida";
    private String checked = "Entrada ya ha sido ingresada";
    private String valid = "Entrada valida";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        escanerView=new ZXingScannerView(this);
        escanerView.startCamera();
        setContentView(R.layout.activity_scan);
        showCameraLayout(R.id.camera_preview);
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
                new SendRequest().execute();
                final Void aVoid = new GetData().execute().get();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //AlertBuilder
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Resultados...");

        String resultados;
        resultados = name +" - "+ cantidad + "\n";
        if (name!= null && name.equalsIgnoreCase("#N/A")){
            resultados += invalid;
        }else if (cantidad >1){
            resultados += checked;
        }else{
            resultados += valid;
        }

        builder.setMessage(resultados);
        AlertDialog alertDialog=builder.create();
        alertDialog.show();


        //Toggle Handler OFF
        isFlash=false;
        escanerView.setFlash(false);
        escanerView.setResultHandler(null);
        resetCamera();
        toggleButton(R.id.scan_button, R.id.goBack_button);
        setButtons();
    }

    public void ScannerQR(View view){
        //Scans code
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

    public class GetData extends AsyncTask<Void, Void, Void> {
        String data;
        String sheetID = "1ncQfu_NMce05zyoGqzQG46lxNS5SctMnSvV-ie56GDw";
        String scriptURL = "https://script.googleusercontent.com/macros/echo?user_content_key=M47DYqJC1KxkfiFOhg1aRBCymEwJfBQGN-pW6VXJnt92FwLBLkjDgQf4Z0r1RXuAjn_RZtE3PcSY4MO6m4m9cx3hmvpoXqQqm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnGxAGPW033IQl2oxH0rlLXVFCdD0IUeY3c0bmQEaX1CKBGYpCQYOwNXoy5tc54YxgK7YkTdRRYv4&lib=M_wXbUSJmUL9F1vIvB8aYhNLn92VrIBpM";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(scriptURL);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

                InputStream inputStream = httpsURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";

                while (line!=null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }

                data = data.substring(data.indexOf("["),data.lastIndexOf("}")+1);
                Log.e("JOBJECT DATA: ", data);


                JSONArray jArray = new JSONArray(data);
                JSONObject jObject = (JSONObject) jArray.get(0);
                name = jObject.get("Nombre").toString();
                cantidad = jObject.getInt("Cantidad");
                Log.e("DATA", name + " - " + cantidad);
            } catch(Exception ex){
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            dataReady = true;
        }
    }
}
