package com.example.fafa.youcoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhang on 2017/7/1.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
