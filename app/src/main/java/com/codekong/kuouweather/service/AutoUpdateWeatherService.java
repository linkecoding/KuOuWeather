package com.codekong.kuouweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.codekong.kuouweather.net.HttpCallBackListener;
import com.codekong.kuouweather.net.HttpMethod;
import com.codekong.kuouweather.net.NetConnection;
import com.codekong.kuouweather.receiver.AutoUpdateWeatherReceiver;
import com.codekong.kuouweather.util.HandleResponse;

/**
 * Created by 尚振鸿 on 2016-09-18.
 * 自动更新天气类
 */
public class AutoUpdateWeatherService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //8小时的毫秒数
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateWeatherReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherCode = prefs.getString("weather_code", "");
        String countyName = prefs.getString("city_name", "");

        String url = "http://apis.baidu.com/apistore/weatherservice/recentweathers";
        String[] header = new String[]{"apikey", "f04a61e1bf06e8761a06b5e31c64c16a"};
        String[] params = new String[]{"cityid", weatherCode, "cityname", countyName};
        new NetConnection(url, HttpMethod.GET, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                HandleResponse.handleWeatherResponse(AutoUpdateWeatherService.this, weatherCode, response);
            }

            @Override
            public void onError(Exception e) {

            }
        }, header, params);
    }
}
