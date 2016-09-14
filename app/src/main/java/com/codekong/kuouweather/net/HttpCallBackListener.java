package com.codekong.kuouweather.net;

/**
 * Created by 53117 on 2016-04-01.
 */
public interface HttpCallBackListener {
    void onFinish(String response);
    void onError(Exception e);
}
