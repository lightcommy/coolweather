package com.commy.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commy.model.City;
import com.commy.model.CoolWeatherDB;
import com.commy.model.County;
import com.commy.model.Province;
import com.commy.util.HttpCallbackListener;
import com.commy.util.HttpUtil;
import com.commy.util.Utility;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity {
    public static final String tag="RIVEN";
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();
//todo selectedCity ID 一直是0
    /*
        省列表
     */
    private List<Province> provinceList;
    /*
        市列表
     */
    private List<City> cityList;
    /*
        县列表
     */
    private List<County> countyList;

    /*
        选中的省份
     */
    private Province selectedProvince;
    /*
        选中的城市
     */
    private City selectedCity;
    /*
        当前选中的级别
     */
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)&&!getIntent().getBooleanExtra("isFromWeatherAc",false)){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.choose_area);
        listView=(ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
//                todo 3.0
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(index);
                queryCities();
                }else if(currentLevel ==LEVEL_CITY){
                    selectedCity = cityList.get(index);
                    Log.d(tag,selectedCity.getId()+"is selectedCityID<<<<"+"index is+"+index+"<<<" +
                            cityList.size());
                  queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String countyCode = countyList.get(index).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
                if(selectedCity!=null&&selectedProvince!=null){

                    Log.d(tag,"selectedCityID is"+selectedCity.getId()+"");
                    Log.d(tag,"selectedProvinceID is"+ selectedProvince.getId());
                }

            }
        });
//        todo 1.0
            queryProvinces();
    }

    /*
        查询全国所有的省，优先从数据库查询，如果没有再去服务器上查询
     */
    private void queryProvinces(){
//        todo 1.1
//        todo 2.1
        provinceList = coolWeatherDB.loadProvinces();
//
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
//            通知显示项目已经更改，view应该自动更改自己的显示项一致。
            adapter.notifyDataSetChanged();
//            默认选中位置：
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
//            todo 1.2
            queryFromServer(null,"province");
        }
    }

    /*
        查询选中省内所有的市，优先从数据库查询，没有再去服务器查。
     */
//    todo 3.1
    private void queryCities(){
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }else{
//            todo 3.2
          queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    /*
        查询选中市内所有县，优先从数据库查询，若没有再去服务器。
     */
    private void queryCounties(){
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
         queryFromServer(selectedCity.getCityCode(),"county");
            Log.d(tag,selectedCity.getCityCode());
        }
    }
    /*
        根据传入的代号和类型从服务器查询省市县数据：
     */
    private void queryFromServer(final String code,final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
//            todo 3.3
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
//            todo 1.3
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
         showProgressDialog();
//        todo 1.4
//        todo 3.4
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)){
//                    todo 1.5
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if("city".equals(type)){
//                    todo 3.5
                    result = Utility.handleCitiesResponse(
                            coolWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(
                            coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
//                    通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                     closeProgressDialog();
                            if("province".equals(type)){
//                                todo 1.7
                                queryProvinces();
                            }else if("city".equals(type)){
//                                todo 3.7
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
//                      回到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                     closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /*
        显示进度对话框：
     */
    private void showProgressDialog(){
        if(progressDialog ==null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /*
        关闭对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
    /*
        捕获Back键，根据当前的级别判断此时应该返回市列表、省列表或者直接退出。
     */
    @Override
    public void onBackPressed(){
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            if(getIntent().getBooleanExtra("isFromWeatherAc",false)){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
        }
    }

}
