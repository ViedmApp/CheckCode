package com.viedmapp.checkcode.AsyncTasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;

import com.viedmapp.checkcode.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class DataHandshake extends AsyncTask<String,Void,String> {
    public AsyncResponse delegate = null;
    private WeakReference<Activity> activityWeakReference;
    private AlertDialog alertDialog;

    public DataHandshake(Activity activityWeakReference) {
        this.activityWeakReference = new WeakReference<>(activityWeakReference);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AlertDialog.Builder builder=new AlertDialog.Builder(activityWeakReference.get());
        builder.setTitle(R.string.dialog_creating_datasheet);
        builder.setView(R.layout.dialog_wait);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected String doInBackground(String... strings) {
        String script = strings[0];

        try {
            URL url = new URL(script);

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

        } catch (IOException e) {
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

            ArrayList<String> jData = new ArrayList<>();
            jData.add(jsonObject.getString("Email"));
            jData.add(jsonObject.getString("Url"));
            jData.add(jsonObject.getString("id"));

            alertDialog.dismiss();
            delegate.processFinish(jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
