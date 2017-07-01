package com.example.fafa.youcoolweather.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fafa.youcoolweather.R;
import com.example.fafa.youcoolweather.db.City;
import com.example.fafa.youcoolweather.db.County;
import com.example.fafa.youcoolweather.db.Province;
import com.example.fafa.youcoolweather.util.Httputil;
import com.example.fafa.youcoolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zhang on 2017/6/30.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEE_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList= new ArrayList<>();
    //省列表
    private List<Province> provinceList=new ArrayList<>();
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province provinceSelected;
    //选中的市
    private City citySelected;
    //当前选中的级别
    private  int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.e("TAG", "onActivityCreated: " );
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    //得到点击位置的坐标
                    provinceSelected=provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel==LEVEL_CITY){
                    citySelected=cityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//点击返回按钮只是更新了不同的数据并没有切换页面
                if (currentLevel==LEVEE_COUNTY)
                    queryCities();
                if (currentLevel==LEVEL_CITY)
                    queryProvinces();
            }
        });
        queryProvinces();
    }

    //查询中国的所有省以及直辖市
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        Log.e("TAG", "queryProvinces: " );
        if(DataSupport.findAll(Province.class)!=null)
        {
            provinceList= DataSupport.findAll(Province.class);
        }

        if (provinceList.size()>0){
            dataList.clear();
            for (Province province :
                    provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//设置默认选中位置 等下取消这个看看效果
            currentLevel=LEVEL_PROVINCE;

        }else{

            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }



    //查询选中的市内的所有城市
    private void queryCities() {
        titleText.setText(provinceSelected.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid = ?",String.valueOf(provinceSelected.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city :
                    cityList) {
                dataList.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            listView.setAdapter(adapter);
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        else{
            int provinceCode=provinceSelected.getProvinceCode();
            String address="http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }

    }
    private void queryCounties() {
        titleText.setText(citySelected.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("city = ?",String.valueOf(citySelected.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county:countyList
                 ) {
                dataList.add(county.getCountyName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEE_COUNTY;

        }else{
            int provinceCode=provinceSelected.getProvinceCode();
            int cityCode=citySelected.getCityCode();
            String address="http://guolin.tech/api/china"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        Httputil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result= false;
                if ("province".equals(type)){
                    Log.e("TAG", "onResponse: "+responseText );
                    result= Utility.handlerProvincesResponse(responseText);
                }else if("city".equals(type)){
                    Log.e("TAG", "onResponse:CITY "+responseText );
                    result=Utility.handlerCitiesResponse(responseText,provinceSelected.getId());
                }
                else if("county".equals(type)){
                    result=Utility.handlerCountiesResponse(responseText,citySelected.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }
                            else  if("city".equals(type)){
                                queryCities();
                            }
                            else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog!=null)
            progressDialog.dismiss();
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载......");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }


}

















