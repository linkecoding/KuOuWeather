package com.codekong.kuouweather.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class NetConnection {
    public NetConnection(final String url, final HttpMethod method, final HttpCallBackListener listener, final String[] header, final String... kvs) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection connection = null;
                StringBuffer paramsStr = new StringBuffer();
                int responseCode = 0;
                for (int i = 0; i < kvs.length; i += 2){
                    //将其拼接为键值对的形式
                    paramsStr.append(kvs[i]).append("=").append(kvs[i+1]).append("&");
                }
                switch (method){
                    case POST:
                        try {
                            connection = (HttpURLConnection) new URL(url).openConnection();
                            connection.setConnectTimeout(8000);
                            connection.setReadTimeout(8000);
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            if (header != null){
                                connection.setRequestProperty(header[0], header[1]);
                            }
                            connection.setRequestProperty("Accept-Charset", "UTF-8");
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                            outputStream.write(paramsStr.toString().getBytes());
                            outputStream.flush();
                            outputStream.close();
                            responseCode = connection.getResponseCode();
                        } catch (ProtocolException e1) {
                            if (listener != null){
                                listener.onError(e1);
                            }
                        } catch (IOException e1) {
                            if (listener != null){
                                listener.onError(e1);
                            }
                        }
                        break;
                    default:
                        try {
                            connection = (HttpURLConnection) new URL(url + "?" + paramsStr.toString()).openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(8000);
                            connection.setReadTimeout(8000);
                            connection.setDoInput(true);
                            connection.setUseCaches(false);
                            if (header != null){
                                connection.setRequestProperty(header[0], header[1]);
                            }
                            connection.setRequestProperty("Accept-Charset", "UTF-8");
                            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            responseCode = connection.getResponseCode();
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                if (responseCode == 200){
                    InputStream in = null;
                    try {
                        in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        in.close();
                        reader.close();
                        return response.toString();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            Log.d("xiaohong", "onPostExecute: " + result);
            if (result != null){
                if (listener != null){
                    listener.onFinish(result);
                }
            }else{
                if (listener != null){
                    listener.onError(null);
                }
            }
            super.onPostExecute(result);

        }
    }.execute();
}
}
