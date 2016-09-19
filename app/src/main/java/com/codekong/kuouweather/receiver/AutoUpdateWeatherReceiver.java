package com.codekong.kuouweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.codekong.kuouweather.service.AutoUpdateWeatherService;

/**
 * Created by 尚振鸿 on 2016-09-19.
 */
public class AutoUpdateWeatherReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, AutoUpdateWeatherService.class);
        context.startService(intent1);
    }
}
