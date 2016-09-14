package com.codekong.kuouweather.util;

import android.text.TextUtils;

import com.codekong.kuouweather.db.KuOuWeatherDB;
import com.codekong.kuouweather.model.City;
import com.codekong.kuouweather.model.County;
import com.codekong.kuouweather.model.Province;

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
}
