package com.codekong.kuouweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.codekong.kuouweather.db.KuOuWeatherDB;
import com.codekong.kuouweather.model.City;
import com.codekong.kuouweather.model.County;
import com.codekong.kuouweather.model.ForecasteWeatherInfo;
import com.codekong.kuouweather.model.Province;
import com.codekong.kuouweather.model.TodayWeatherInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 尚振鸿 on 2016-09-14.
 * 处理网络请求回来的数据
 */
public class HandleResponse {

    /**
     * 解析和处理服务器返回的省级数据
     * @param kuOuWeatherDB  数据库操作类实例
     * @param response       服务器返回的数据
     * @return
     */
    public synchronized static boolean handleProvincesResponse(KuOuWeatherDB kuOuWeatherDB, String response){
        if (!TextUtils.isEmpty(response)){
            //第一次分割字符串
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0){
                for (String p : allProvinces) {
                    //第二次分割字符串
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析的数据存储到Province表
                    kuOuWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理服务器返回的某一个省下面的市级的数据
     * @param kuOuWeatherDB   数据库操作类实例
     * @param response        服务器返回的数据
     * @param provinceId      省的id
     * @return
     */
    public static boolean handleCitiesResponse(KuOuWeatherDB kuOuWeatherDB, String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0){
                for (String c : allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出的数据存储到City表
                    kuOuWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理某一个市下面的县级数据
     * @param kuOuWeatherDB   数据库操作类实例
     * @param response        服务器返回的数据
     * @param cityId          城市Id
     * @return
     */
    public static boolean handleCountiesResponse(KuOuWeatherDB kuOuWeatherDB, String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0){
                for (String c : allCounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的数据存储到County类
                    kuOuWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param context
     * @param weatherCode
     * @param response
     */
    public static void handleWeatherResponse(Context context, String weatherCode, String response){
        if (response != null){
            try {
                JSONObject jsonObject1 = new JSONObject(response);
                //获取数据成功
                if (jsonObject1.getInt("errNum") == 0){
                    JSONObject retData = jsonObject1.getJSONObject("retData");
                    String cityName = retData.getString("city");
                    JSONObject today = retData.getJSONObject("today");
                    String curTemp = today.getString("curTemp");
                    String fengli = today.getString("fengli");
                    String hightemp = today.getString("hightemp");
                    String lowtemp = today.getString("lowtemp");
                    String type = today.getString("type");

                    JSONArray index = today.getJSONArray("index");
                    String[] indexs = new String[6];
                    for (int i = 0; i < index.length(); i++) {
                        indexs[i] = index.getJSONObject(i).getString("details");
                    }

                    TodayWeatherInfo todayWeatherInfo = new TodayWeatherInfo(cityName, weatherCode, curTemp, fengli, type, hightemp, lowtemp, indexs);

                    JSONArray forecast = retData.getJSONArray("forecast");
                    List<ForecasteWeatherInfo> forecasteWeatherInfoList = new ArrayList<>();
                    for (int i = 0; i < forecast.length(); i++) {
                        JSONObject jsonObject = forecast.getJSONObject(i);
                        ForecasteWeatherInfo forecastWeatherInfo = new ForecasteWeatherInfo(
                                jsonObject.getString("week"),
                                jsonObject.getString("fengli"),
                                jsonObject.getString("hightemp"),
                                jsonObject.getString("lowtemp"),
                                jsonObject.getString("type"));
                        forecasteWeatherInfoList.add(forecastWeatherInfo);
                    }

                    //存储获取回来的当天的和以后四天的数据
                    saveWeatherInfo(context, todayWeatherInfo, forecasteWeatherInfoList);
                }else{
                    Toast.makeText(context, "数据加载失败", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "数据加载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 将所有天气信息存储到sharePreferences中
     * @param context
     * @param todayWeatherInfo    当天的天气信息
     * @param forecasteWeatherInfoList  未来的天气信息
     */
    public static void saveWeatherInfo(Context context, TodayWeatherInfo todayWeatherInfo, List<ForecasteWeatherInfo> forecasteWeatherInfoList){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", todayWeatherInfo.getCityName());
        editor.putString("weather_code", todayWeatherInfo.getWeatherCode());
        editor.putString("cur_temp", todayWeatherInfo.getCurTemp());
        editor.putString("fengli", todayWeatherInfo.getFengli());
        editor.putString("type", todayWeatherInfo.getType());
        editor.putString("high_temp", todayWeatherInfo.getHightemp());
        editor.putString("low_temp", todayWeatherInfo.getLowtemp());
        editor.putString("gm_index", todayWeatherInfo.getGmIndex());
        editor.putString("fs_index", todayWeatherInfo.getFsIndex());
        editor.putString("ct_index", todayWeatherInfo.getCtIndex());
        editor.putString("yd_index", todayWeatherInfo.getYdIndex());
        editor.putString("xc_index", todayWeatherInfo.getXcIndex());
        editor.putString("ls_index", todayWeatherInfo.getLsIndex());
        //存储未来4天的天气信息
        for (int i = 0; i < forecasteWeatherInfoList.size(); i++) {
            ForecasteWeatherInfo forecastWeatherInfo = forecasteWeatherInfoList.get(i);
            editor.putString("forecast_week" + (i+1), forecastWeatherInfo.getWeek());
            editor.putString("forecast_fengli" + (i+1), forecastWeatherInfo.getFengli());
            editor.putString("forecast_hightemp" + (i+1), forecastWeatherInfo.getHightemp());
            editor.putString("forecast_lowtemp" + (i+1), forecastWeatherInfo.getLowtemp());
            editor.putString("forecast_type" + (i+1), forecastWeatherInfo.getType());
        }
        editor.commit();
    }
}
