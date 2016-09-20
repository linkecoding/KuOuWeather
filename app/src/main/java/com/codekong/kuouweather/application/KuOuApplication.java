package com.codekong.kuouweather.application;

import android.app.Application;
import android.util.Log;

import com.xiaomi.mistatistic.sdk.MiStatInterface;
import com.xiaomi.mistatistic.sdk.URLStatsRecorder;
import com.xiaomi.mistatistic.sdk.controller.HttpEventFilter;
import com.xiaomi.mistatistic.sdk.data.HttpEvent;

import org.json.JSONException;

/**
 * Created by 尚振鸿 on 2016-09-13.
 * 自定义Application类
 */
public class KuOuApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        //小米统计配置
        final String APP_ID = "2882303761517511218";
        final String APP_KEY = "5141751151218";
        // regular stats.
        MiStatInterface.initialize(this.getApplicationContext(), APP_ID, APP_KEY, "default channel");

        MiStatInterface.setUploadPolicy(MiStatInterface.UPLOAD_POLICY_WHILE_INITIALIZE, 0);
        MiStatInterface.enableLog();

        // enable exception catcher.
        MiStatInterface.enableExceptionCatcher(true);

        // enable network monitor
        URLStatsRecorder.enableAutoRecord();
        URLStatsRecorder.setEventFilter(new HttpEventFilter() {

            @Override
            public HttpEvent onEvent(HttpEvent event) {
                try {
                    Log.d("MI_STAT", event.getUrl() + " result =" + event.toJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // returns null if you want to drop this event.
                // you can modify it here too.
                return event;
            }
        });

        Log.d("MI_STAT", MiStatInterface.getDeviceID(this) + " is the device.");
    }
}
