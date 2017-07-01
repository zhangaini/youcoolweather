package com.example.fafa.youcoolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.fafa.youcoolweather.db.City;
import com.example.fafa.youcoolweather.db.County;
import com.example.fafa.youcoolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhang on 2017/6/30.
 */

public class Utility {
    //解析 和处理服务器返回的省的数据
    public static boolean handlerProvincesResponse(String response){

        if(!TextUtils.isEmpty(response)){// 当服务器响应的东西不为空的时候开始解析
            try {
                JSONArray allProvinces =new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject =allProvinces.getJSONObject(i);//逐步解析
                    Province province= new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析 和处理服务器返回的市级数据
    public static boolean handlerCitiesResponse(String response,int ProvinceId){
        if(!TextUtils.isEmpty(response)){// 当服务器响应的东西不为空的时候开始解析
            try {
                JSONArray allCitys =new JSONArray(response);
                for(int i=0;i<allCitys.length();i++){
                    JSONObject CityObject =allCitys.getJSONObject(i);//逐步解析
                    City city= new City();
                    city.setCityName(CityObject.getString("name"));
                    city.setCityCode(CityObject.getInt("id"));
                    city.setProvinceId(ProvinceId);
                    city.save();
                    Log.e("TAG", "handlerCitiesResponse: "+city.getCityName() );
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //服务器返回的县级数据
    public static boolean handlerCountiesResponse(String response,int Cityid){
        if(!TextUtils.isEmpty(response)){// 当服务器响应的东西不为空的时候开始解析
            try {

                JSONArray allCounty =new JSONArray(response);
                for(int i=0;i<allCounty.length();i++){
                    JSONObject CityObject =allCounty.getJSONObject(i);//逐步解析
                    County county= new County();
                    county.setCountyName(CityObject.getString("name"));
                    county.setCityId(Cityid);
                    county.setWeatherId(CityObject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
