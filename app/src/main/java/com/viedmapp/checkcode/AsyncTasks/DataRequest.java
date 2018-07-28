package com.viedmapp.checkcode.AsyncTasks;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncTask;
import android.util.Log;

import com.viedmapp.checkcode.R;
import com.viedmapp.checkcode.ScanActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class DataRequest extends AsyncTask<String,Integer,ArrayList<String>> {
    public AsyncResponse delegate = null;
    private String scannedData;
    private String data;
    //private String scriptURL2 = "https://script.google.com/macros/s/AKfycbzr0GzvSxAyd5XSGCTaqdzyhowMPaMoeGQ9TCj6oUhhAXY12M8/exec";
    //rivate String scriptURL = "https://script.google.com/macros/s/AKfycbzyQ3FbAXZPbyz5gRNh1nbsdn73kjeVIiAxgzmrmYnXTd0djfK4/exec";
    private String scriptURL3 = "https://script.google.com/macros/s/AKfycbx9yWevNhKhStGaDDPA3VPmmaY5XkUnjh24Z-MlTMK5Pq4hBn4/exec";
    private AlertDialog alertDialog;
    private WeakReference<ScanActivity> activityWeakReference;
    private ArrayList<String> jData;

    public DataRequest(String scannedData, ScanActivity activity) {
        this.scannedData = scannedData;
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Builder builder=new Builder(activityWeakReference.get());
        builder.setTitle(R.string.dialog_wait);
        builder.setView(R.layout.dialog_wait);
        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        try{
            //Script URL
            URL url = new URL(scriptURL3);

            //Scanned Data as JSONObject
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("sdata",scannedData);
            Log.e("params",postDataParams.toString());
            publishProgress(10);
            //Init HTTPS Connection
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setReadTimeout(15000);
            httpsConn.setConnectTimeout(15000);
            httpsConn.setRequestMethod("GET");
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(true);

            //Send Data
            OutputStream outputStream = httpsConn.getOutputStream();

            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(getPostDataString(postDataParams));

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            if(httpsConn.getResponseCode()==HttpsURLConnection.HTTP_OK) Log.d("HttpConn = ","OK");

            publishProgress(50);

            //Get Data
            InputStream inputStream = httpsConn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while(line!=null){
                line = bufferedReader.readLine();
                stringBuilder.append(line);
            }

            data = stringBuilder.toString().substring(data.indexOf("["),data.lastIndexOf("]")+1);

            //Parse Data
            jData = new ArrayList<>();
            JSONArray jArray = new JSONArray(data);
            JSONObject jObject = (JSONObject) jArray.get(0);
            jData.add(jObject.getString("Codigo_de_barra"));
            jData.add(jObject.get("Nombre").toString());
            jData.add(jObject.getString("Cantidad").equals("#N/A")?"10":jObject.getString("Cantidad"));

            publishProgress(100);

        }catch(Exception e){
            e.printStackTrace();
        }

        return jData;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        super.onPostExecute(result);
        alertDialog.dismiss();
        delegate.processFinish(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    private String getPostDataString(JSONObject params) throws Exception {

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
