package com.codekong.kuouweather.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.codekong.kuouweather.R;
import com.codekong.kuouweather.net.HttpCallBackListener;
import com.codekong.kuouweather.net.HttpMethod;
import com.codekong.kuouweather.net.NetConnection;
import com.codekong.kuouweather.util.HandleResponse;

public class WeatherActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        context = this;
        initView();
        initEvent();
    }

    /**
     * 初始化获取布局控件
     */
    private void initView() {
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
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode", countyCode, countyName);
    }


    /**
     * 查询天气代号所对应的天气信息
     * @param weatherCode
     * @param countyName
     */
    private void queryWeatherInfo(String weatherCode, String countyName){
        String address = "http://apis.baidu.com/apistore/weatherservice/recentweathers";
        queryFromServer(address, "weatherCode", weatherCode, countyName);
    }

    private void queryFromServer(String address, String type, String countyCode, final String countyName){
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
            String url = "http://apis.baidu.com/apistore/weatherservice/recentweathers";
            String[] header = new String[]{"apikey", "f04a61e1bf06e8761a06b5e31c64c16a"};
            String[] params = new String[]{"cityid", countyCode, "cityname", countyName};
            new NetConnection(url, HttpMethod.GET, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    HandleResponse.handleWeatherResponse(context, response);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        curTempTv.setText(prefs.getString("cur_temp", ""));
        cityNameTv.setText(prefs.getString("city_name", ""));
        weatherTypeTv.setText(prefs.getString("type", ""));
        lowTempTv.setText(prefs.getString("low_temp", ""));
        highTempTv.setText(prefs.getString("high_temp", ""));
        fengliTv.setText(prefs.getString("fengli", "无"));

        for (int i = 0; i < futureWeeks.length; i++) {
            futureWeeks[i].setText(prefs.getString("forecast_week" + (i+1), null));
            futureWeatherTypes[i].setText(prefs.getString("forecast_type" + (i+1), null));
            futureFengLis[i].setText(prefs.getString("forecast_fengli" + (i+1), null));
            futureLowHighTemps[i].setText(prefs.getString("forecast_lowtemp" + (i+1), null) + " / " + prefs.getString("forecast_hightemp" + (i+1), null));
        }
    }
}
