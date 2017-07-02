package com.example.fafa.youcoolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.fafa.youcoolweather.gson.Forecast;
import com.example.fafa.youcoolweather.gson.Weather;
import com.example.fafa.youcoolweather.util.Httputil;
import com.example.fafa.youcoolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ImageView backImage;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private String TAG="TAG";
    public SwipeRefreshLayout reFresh;
    public String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button changeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        backImage=(ImageView)findViewById(R.id.back_image) ;
        reFresh = (SwipeRefreshLayout) findViewById(R.id.my_swipe_refresh);
        reFresh.setColorSchemeColors(Color.GRAY);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        changeButton = (Button) findViewById(R.id.change_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String backImageSrc=prefs.getString("back_image",null);
        if (backImageSrc!=null){
            Glide.with(this).load(backImageSrc).into(backImage);
        }
        else{
            Log.e(TAG, "onCreate: "+"正在加载图片" );
            loadBackImage();
        }
        String weatherString =prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存的时候直接解析天气的数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId; //这个id用来刷新数据的时候使用
            showWeatherInfo(weather);
        }
        else{
            //无缓存的时候去服务器读取数据
            mWeatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        reFresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

    }

    private void loadBackImage() {
        String requestImage ="http://guolin.tech/api/bing_pic";
        Httputil.sendOkHttpRequest(requestImage, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "加载图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pic=response.body().string();
                Log.e(TAG, "图片路径: "+pic );
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("back_image",pic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(pic).into(backImage);
                    }
                });

            }
        });
    }

    //根据天气的id请求城市天气信息
    public void requestWeather(String weatherId) {
        String weatherUrl ="http://guolin.tech/api/weather?cityid=" + weatherId +"&key=2d695b14e8634a178c8650ce56197e7d";
        Log.e(TAG, "requestWeather:URL "+weatherUrl );
        Httputil.sendOkHttpRequest(weatherUrl, new Callback() {
         @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                Log.e(TAG, "o返回数据"+responseText );
                final Weather weather =Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else{
                            Toast.makeText(WeatherActivity.this, "获取返回天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                        reFresh.setRefreshing(false);
                    }
                });

            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败了", Toast.LENGTH_SHORT).show();
                        reFresh.setRefreshing(false);//表示刷新事件的结束
                    }
                });

            }


        });
        loadBackImage();
    }

    //处理并且展示天气的信息
    private void showWeatherInfo(Weather weather) {

        String cityName =weather.basic.cityName;
        Log.e(TAG, "showWeatherInfo: "+weather.aqi );
        String updateTime=weather.basic.update.updateTime.split(" ")[1];//通过空格分开 取第二个元素
        String degree=weather.now.temperature+"℃";
        String weatherInfo =weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView) view.findViewById(R.id.data_info);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        Log.e(TAG, "showWeatherInfo: "+weather.aqi );
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度： "+weather.suggestion.comfort.info;
        String carWash="洗车指数: "+weather.suggestion.carWash.info;
        String sport="运动建议: "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }
}
