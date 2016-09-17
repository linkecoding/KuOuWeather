package com.codekong.kuouweather.model;

/**
 * Created by 尚振鸿 on 2016-09-17.
 * 未来天气基本信息的Model
 */
public class ForecasteWeatherInfo {
    //日期(星期)
    private String week;
    //风力
    private String fengli;
    //最高温度
    private String hightemp;
    //最低温度
    private String lowtemp;
    //天气类型
    private String type;

    public ForecasteWeatherInfo(String week, String fengli, String hightemp, String lowtemp, String type) {
        this.week = week;
        this.fengli = fengli;
        this.hightemp = hightemp;
        this.lowtemp = lowtemp;
        this.type = type;
    }

    public String getWeek() {
        return week;
    }

    public String getFengli() {
        return fengli;
    }

    public String getHightemp() {
        return hightemp;
    }

    public String getLowtemp() {
        return lowtemp;
    }

    public String getType() {
        return type;
    }
}
