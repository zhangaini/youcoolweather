package com.example.fafa.youcoolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhang on 2017/7/1.
 */

public class Forecast {
    public String date;
    @SerializedName("tmp")
    public Temperature temperature;

    public class Temperature {
        public String max;
        public String min;
    }
    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}
