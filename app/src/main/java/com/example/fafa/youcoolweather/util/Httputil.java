package com.example.fafa.youcoolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by zhang on 2017/6/30.
 */

public class Httputil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client= new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
        //这里只需要发送一条HTTP请求过来调用此方法，传入请求地址并且注册一个回调来处理服务器响应就可以了

    }

}
