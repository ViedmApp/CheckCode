package com.viedmapp.checkcode.AsyncTasks;

import android.os.AsyncTask;

import com.google.api.client.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class DataHandshake extends AsyncTask<String,Void,String> {
    public AsyncResponse delegate = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
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

            delegate.processFinish(jData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

            while(line!=null){
                line = bufferedReader.readLine();
                data.append(line).append("\n");
            }

            inputStream.close();

            return data.toString().substring(data.indexOf("["),data.lastIndexOf("]")+1);

        } catch (IOException e) {
            e.printStackTrace();
            return "Error";
        }

    }
}
