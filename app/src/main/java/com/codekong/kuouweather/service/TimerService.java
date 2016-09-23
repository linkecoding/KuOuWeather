package com.codekong.kuouweather.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.codekong.kuouweather.R;
import com.codekong.kuouweather.widget.WidgetProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 尚振鸿 on 2016-09-21.
 */
public class TimerService extends Service {
    private Timer timer;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM月dd日 HH:mm");
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("timers", "onCreate");
        timer = new Timer();
        //参数依次为要执行的任务、延时、更新时间(毫秒)
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateViews();
            }
        }, 0, 1000);
    }

    private void updateViews(){
        String dateTime = simpleDateFormat.format(new Date());
        String[] strs = dateTime.split(" ");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String cityName = preferences.getString("city_name", "");
        String weatherType = preferences.getString("type", "");

        //widget布局
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.weather_widget);
        //设置布局中的内容
        remoteViews.setTextViewText(R.id.show_time_tv, strs[1]);
        remoteViews.setTextViewText(R.id.show_date_tv, strs[0]);
        if (!TextUtils.isEmpty(cityName) && !TextUtils.isEmpty(weatherType)){
            remoteViews.setTextViewText(R.id.show_city_tv, cityName);
            remoteViews.setTextViewText(R.id.show_weather_tv, weatherType);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName componentName = new ComponentName(getApplicationContext(), WidgetProvider.class);
        //更新widget
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer = null;
    }
}
