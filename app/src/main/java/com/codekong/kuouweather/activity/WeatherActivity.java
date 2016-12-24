package com.codekong.kuouweather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.codekong.kuouweather.R;
import com.codekong.kuouweather.config.AppConfig;
import com.codekong.kuouweather.net.HttpCallBackListener;
import com.codekong.kuouweather.net.HttpMethod;
import com.codekong.kuouweather.net.NetConnection;
import com.codekong.kuouweather.service.AutoUpdateWeatherService;
import com.codekong.kuouweather.util.ClassUtil;
import com.codekong.kuouweather.util.HandleResponse;
import com.codekong.kuouweather.util.ShareUtil;
import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.mistatistic.sdk.MiStatInterface;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.ShareSDK;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener, BaseRefreshListener {
    private PullToRefreshLayout pullToRefreshLayout;

    //向下弹出菜单
    private View popupMenuView;
    private PopupWindow popupMenu;
    //右上角菜单控件
    private ImageView menuImageView;
    //popupMenu菜单item
    private TextView changeCityTv, updateWeather, shareWeatherTv, settingTv;

    private Context context;
    //同步中文字显示
    private TextView syncTextTv;
    //当前温度
    private TextView curTempTv;
    //城市名称
    private TextView cityNameTv;
    //天气类型
    private TextView weatherTypeTv;
    //最低温度
    private TextView lowTempTv;
    //最高温度
    private TextView highTempTv;
    //风力大小
    private TextView fengliTv;

    //存储天气图片控件实例
    private ImageView[] futureWeatherTypeImgs;
    //存储这4类TextView(天气日期(星期)、天气类型、天气风力、最低温最高温)
    private TextView[] futureWeeks, futureWeatherTypes, futureFengLis, futureLowHighTemps;
    //6类生活显示
    private TextView gmIndex, fsIndex, ctIndex, ydIndex, xcIndex, lsIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        //小米更新,这种情况下, 若本地版本是debug版本则使用沙盒环境，否则使用线上环境
        XiaomiUpdateAgent.update(this);
        //社会化分享
        ShareSDK.initSDK(this, AppConfig.SHARE_APP_KEY);
        context = this;
        initView();
        initEvent();
    }

    /**
     * 初始化获取布局控件
     */
    private void initView() {
        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.weather_layout);
        pullToRefreshLayout.setRefreshListener(this);

        //弹出菜单View
        popupMenuView = getLayoutInflater().inflate(R.layout.popup_menu_layout, null);

        menuImageView = (ImageView) findViewById(R.id.menu_image);
        menuImageView.setOnClickListener(this);

        syncTextTv = (TextView) findViewById(R.id.sync_text);
        curTempTv = (TextView) findViewById(R.id.cur_temp);
        cityNameTv = (TextView) findViewById(R.id.city_name);
        weatherTypeTv = (TextView) findViewById(R.id.weather_type);
        lowTempTv = (TextView) findViewById(R.id.low_temp);
        highTempTv = (TextView) findViewById(R.id.height_temp);
        fengliTv = (TextView) findViewById(R.id.fenli);

        futureWeatherTypeImgs = new ImageView[4];
        futureWeatherTypeImgs[0] = (ImageView) findViewById(R.id.future_weather_type_img1);
        futureWeatherTypeImgs[1] = (ImageView) findViewById(R.id.future_weather_type_img2);
        futureWeatherTypeImgs[2] = (ImageView) findViewById(R.id.future_weather_type_img3);
        futureWeatherTypeImgs[3] = (ImageView) findViewById(R.id.future_weather_type_img4);

        futureWeeks = new TextView[4];
        futureWeeks[0] = (TextView) findViewById(R.id.future_week1);
        futureWeeks[1] = (TextView) findViewById(R.id.future_week2);
        futureWeeks[2] = (TextView) findViewById(R.id.future_week3);
        futureWeeks[3] = (TextView) findViewById(R.id.future_week4);

        futureWeatherTypes = new TextView[4];
        futureWeatherTypes[0] = (TextView) findViewById(R.id.future_weather_type1);
        futureWeatherTypes[1] = (TextView) findViewById(R.id.future_weather_type2);
        futureWeatherTypes[2] = (TextView) findViewById(R.id.future_weather_type3);
        futureWeatherTypes[3] = (TextView) findViewById(R.id.future_weather_type4);

        futureFengLis = new TextView[4];
        futureFengLis[0] = (TextView) findViewById(R.id.future_fengli1);
        futureFengLis[1] = (TextView) findViewById(R.id.future_fengli2);
        futureFengLis[2] = (TextView) findViewById(R.id.future_fengli3);
        futureFengLis[3] = (TextView) findViewById(R.id.future_fengli4);

        futureLowHighTemps = new TextView[4];
        futureLowHighTemps[0] = (TextView) findViewById(R.id.future_low_high_temp1);
        futureLowHighTemps[1] = (TextView) findViewById(R.id.future_low_high_temp2);
        futureLowHighTemps[2] = (TextView) findViewById(R.id.future_low_high_temp3);
        futureLowHighTemps[3] = (TextView) findViewById(R.id.future_low_high_temp4);

        gmIndex = (TextView) findViewById(R.id.gm_index);
        fsIndex = (TextView) findViewById(R.id.fs_index);
        ctIndex = (TextView) findViewById(R.id.ct_index);
        ydIndex = (TextView) findViewById(R.id.yd_index);
        xcIndex = (TextView) findViewById(R.id.xc_index);
        lsIndex = (TextView) findViewById(R.id.ls_index);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        String countyCode = getIntent().getStringExtra("county_code");
        String countyName = getIntent().getStringExtra("county_name");

        if (!TextUtils.isEmpty(countyCode) && !TextUtils.isEmpty(countyName)){
            //如果有县级代号就去查询天气代号进一步查询天气信息
            syncTextTv.setVisibility(View.VISIBLE);
            syncTextTv.setText(R.string.syncing);
            queryWeatherCode(countyCode, countyName);
        }else{
            //没有县级代号时就直接显示本地天气
            showWeather();
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode, String countyName){
        String address = AppConfig.WEATHER_CODE_OF_COUNTY_CODE_URL + countyCode + ".xml";
        queryFromServer(address, "countyCode", countyCode, countyName);
    }


    /**
     * 查询天气代号所对应的天气信息
     * @param weatherCode
     * @param countyName
     */
    private void queryWeatherInfo(String weatherCode, String countyName){
        String address = AppConfig.WEATHER_INFO_URL;
        queryFromServer(address, "weatherCode", weatherCode, countyName);
    }

    private void queryFromServer(String address, String type, final String countyCode, final String countyName){
        if ("countyCode".equals(type)){
            new NetConnection(address, HttpMethod.GET, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    if (!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode, countyName);
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            syncTextTv.setText(R.string.sync_failed);
                        }
                    });
                }
            }, null, new String[]{});
        }else  if ("weatherCode".equals(type)){
            String url = AppConfig.WEATHER_INFO_URL;
            String[] header = new String[]{"apikey", AppConfig.WEATHER_API_KEY};
            String[] params = new String[]{"cityid", countyCode, "cityname", countyName};
            new NetConnection(url, HttpMethod.GET, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    HandleResponse.handleWeatherResponse(context, countyCode, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                            syncTextTv.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            syncTextTv.setText(R.string.sync_failed);
                        }
                    });
                }
            }, header, params);
        }

    }

    /**
     * 从SharePreferences文件中读取存储的天气信息并设置到界面上
     */
    private void showWeather() {
        Map<String, String> weatherImg = new HashMap<>();
        weatherImg.put("晴", "01");
        weatherImg.put("多云", "02");
        weatherImg.put("阴", "03");
        weatherImg.put("雾", "04");
        weatherImg.put("大风", "05");
        weatherImg.put("雷", "06");
        weatherImg.put("风暴", "07");
        weatherImg.put("沙尘暴", "08");
        weatherImg.put("小雨", "09");
        weatherImg.put("中雨", "10");
        weatherImg.put("大雨", "11");
        weatherImg.put("雷阵雨", "12");
        weatherImg.put("阵雨", "13");
        weatherImg.put("小雪", "14");
        weatherImg.put("中雪", "15");
        weatherImg.put("雨夹雪", "16");
        weatherImg.put("阵雪", "17");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        curTempTv.setText(prefs.getString("cur_temp", ""));
        cityNameTv.setText(prefs.getString("city_name", ""));
        weatherTypeTv.setText(prefs.getString("type", ""));
        lowTempTv.setText(prefs.getString("low_temp", ""));
        highTempTv.setText(prefs.getString("high_temp", ""));
        fengliTv.setText(prefs.getString("fengli", "无"));

        for (int i = 0; i < futureWeeks.length; i++) {
            String weatherType = prefs.getString("forecast_type" + (i+1), null);
            futureWeeks[i].setText(prefs.getString("forecast_week" + (i+1), null));
            futureWeatherTypes[i].setText(prefs.getString("forecast_type" + (i+1), null));
            futureFengLis[i].setText(weatherType);
            futureLowHighTemps[i].setText(prefs.getString("forecast_lowtemp" + (i+1), null) + " / " + prefs.getString("forecast_hightemp" + (i+1), null));
            if (weatherImg.containsKey(weatherType)){
                futureWeatherTypeImgs[i].setImageResource(ClassUtil.getResId("ic_weather_" + weatherImg.get(weatherType), R.drawable.class));
            }else{
                futureWeatherTypeImgs[i].setImageResource(R.drawable.ic_weather_01);
            }
        }

        gmIndex.setText(prefs.getString("gm_index", null));
        fsIndex.setText(prefs.getString("fs_index", null));
        ctIndex.setText(prefs.getString("ct_index", null));
        ydIndex.setText(prefs.getString("yd_index", null));
        xcIndex.setText(prefs.getString("xc_index", null));
        lsIndex.setText(prefs.getString("ls_index", null ));

        Intent intent = new Intent(this, AutoUpdateWeatherService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menu_image:
                showMenu(v);
                break;
            case R.id.close_menu:
                if (popupMenu!= null && popupMenu.isShowing()){
                    popupMenu.dismiss();
                }
                break;
            case R.id.change_city:
                changeCity();
                break;
            case R.id.share_weather:
                if (popupMenu != null){
                    popupMenu.dismiss();
                }
                ShareUtil.showShare(this);
                break;
            default:
                break;
        }
    }

    /**
     * 更新天气
     */
    private void updateWeather() {
        syncTextTv.setText(R.string.syncing);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("weather_code", "");
        String cityName = prefs.getString("city_name", "");
        if (!TextUtils.isEmpty(weatherCode)){
            queryWeatherInfo(weatherCode, cityName);
        }
        if (popupMenu != null){
            popupMenu.dismiss();
        }
    }

    /**
     * 切换城市
     */
    private void changeCity() {
        popupMenu.dismiss();
        Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
        intent.putExtra("from_weather_activity", true);
        startActivity(intent);
        finish();
    }

    /**
     * 以下拉方式弹出菜单
     */
    private void showMenu(View v) {
        popupMenu = new PopupWindow(popupMenuView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupMenu.setAnimationStyle(R.style.popupInOutAnimation);
        popupMenu.setFocusable(true);
        popupMenu.setOutsideTouchable(true);
        ColorDrawable dw = new ColorDrawable(000000);
        popupMenu.setBackgroundDrawable(dw);
        popupMenu.showAtLocation(v, Gravity.TOP, 0, 60);

        setPopupMenuClick();
    }

    /**
     * 为弹出菜单设置item点击事件
     */
    private void setPopupMenuClick(){
        ImageView closeMenu = (ImageView) popupMenuView.findViewById(R.id.close_menu);
        closeMenu.setOnClickListener(this);
        changeCityTv = (TextView) popupMenuView.findViewById(R.id.change_city);
        changeCityTv.setOnClickListener(this);
        shareWeatherTv = (TextView) popupMenuView.findViewById(R.id.share_weather);
        shareWeatherTv.setOnClickListener(this);
        settingTv = (TextView) popupMenuView.findViewById(R.id.setting);
        settingTv.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MiStatInterface.recordPageStart(this, "主界面");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MiStatInterface.recordPageEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateWeather();
                pullToRefreshLayout.finishRefresh();
            }
        }, 1500);
    }

    @Override
    public void loadMore() {
    }
}
