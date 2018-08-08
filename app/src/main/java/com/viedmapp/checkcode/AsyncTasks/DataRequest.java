package com.viedmapp.checkcode.AsyncTasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.AsyncTask;

import com.viedmapp.checkcode.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


public class DataRequest extends AsyncTask<String,Integer,String> {
    public AsyncResponse delegate = null;

    private AlertDialog alertDialog;
    private WeakReference<Activity> activityWeakReference;

    public DataRequest(Activity activity) {
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
    protected String doInBackground(String... strings) {
        try{
            //Script URL
            String script = strings[0];
            URL url = new URL(script);

            //Init HTTPS Connection
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream)
            );

            String line = "";
            StringBuilder data = new StringBuilder();

            while (line != null) {
                line = bufferedReader.readLine();
                data.append(line).append("\n");
            }

            inputStream.close();

            return data.toString().substring(data.indexOf("["), data.lastIndexOf("]") + 1);


        }catch(Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        //Parsing
        try{
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);

            alertDialog.dismiss();
            delegate.processFinish(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}