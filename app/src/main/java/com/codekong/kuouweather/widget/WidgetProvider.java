package com.codekong.kuouweather.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codekong.kuouweather.service.TimerService;

/**
 * Created by 尚振鸿 on 2016-09-21.
 * 天气小组件处理类
 */
public class WidgetProvider extends AppWidgetProvider{

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //widget被从屏幕移除
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //最后一个widget被从屏幕移除,停止服务
        context.stopService(new Intent(context, TimerService.class));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        //第一个widget被添加到屏幕上,启动服务
        context.startService(new Intent(context, TimerService.class));
        Log.d("timers", "onEnabled: ");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //刷新widget
    }
}
