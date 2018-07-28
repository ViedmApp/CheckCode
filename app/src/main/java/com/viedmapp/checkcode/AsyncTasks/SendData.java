package com.viedmapp.checkcode.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

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

public class SendData extends AsyncTask<String,Void,String> {
    private String scannedData;


    public SendData(String scannedData) {
        this.scannedData = scannedData;
    }

    @Override
    protected String doInBackground(String... voids) {
        try {

            //Enter script URL Here
            String scriptURL = "https://script.google.com/macros/s/AKfycbydx3sGJ3-xXKzq6clducWjxZkFvDpjxQSiAIiggIHvzxVU6rQZ/exec";
            URL url = new URL(scriptURL);

            JSONObject postDataParams = new JSONObject();

            //Passing scanned code as parameter
            postDataParams.put("sdata", scannedData);


            Log.e("params", postDataParams.toString());

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

            int responseCode = conn.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line= in.readLine())!=null){
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            }
            else {
                return "false: " + responseCode;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }

    }

    private String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
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
