package com.commy.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commy.receiver.AutoUpdateReceiver;
import com.commy.util.HttpCallbackListener;
import com.commy.util.HttpUtil;
import com.commy.util.Utility;

import org.w3c.dom.Text;

public class WeatherActivity extends AppCompatActivity {
    public static final String tag="Riven1";
    private LinearLayout weatherInfoLayout;
    /*
        显示城市名：
     */
    private TextView cityNameText;
    /*
        显示发布时间：
     */
    private TextView publishText;
    /*
        显示天气描述信息：
     */
    private TextView weatherDespText;
    /*
        显示气温1：
     */
    private TextView temp1Text;
    /*
        显示气温2：
     */
    private TextView temp2Text;
    /*
        显示当前日期：
     */
    private TextView currentDateText;
    private Button button_home;
    private Button button_fresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText = (TextView)findViewById(R.id.publish_text);
        weatherDespText = (TextView)findViewById(R.id.weather_desp);
        temp1Text = (TextView)findViewById(R.id.temp1);
        temp2Text = (TextView)findViewById(R.id.temp2);
        currentDateText = (TextView)findViewById(R.id.current_date);
        button_home=(Button)findViewById(R.id.button_home);
        button_fresh=(Button)findViewById(R.id.refresh);

        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                intent.putExtra("isFromWeatherAc", true);
                startActivity(intent);
            }
        });

        button_fresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countyCode = getIntent().getStringExtra("county_code");
                Log.d(tag, "click countyCode is >><><><><><><><><><><><>" + countyCode);
                if (!TextUtils.isEmpty(countyCode)) {
//            有县级代号时就查询天气
                    publishText.setText("同步中...");
                    weatherInfoLayout.setVisibility(View.INVISIBLE);
                    cityNameText.setVisibility(View.INVISIBLE);
                    queryWeatherCode(countyCode);
                } else {
                    showWeather();
                }
            }
        });
        String countyCode = getIntent().getStringExtra("county_code");
        Log.d(tag,"countyCode is >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+countyCode);
        if(!TextUtils.isEmpty(countyCode)){
//            有县级代号时就查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else {
            showWeather();
        }
    }

    /*
        查询县级代号所对应的天气代号：
     */
    private void queryWeatherCode(String countyCode){
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address,"countyCode");
    }
    /*
        查询天气代号所对应的天气：
     */
    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        queryFromServer(address,"weatherCode");
    }

    /*
        根据传入的地址和类型向服务器查询天气代号或者天气信息：
     */
    private void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
//                        从服务器返回的数据中解析出天气代号：
                        String[] array = response.split("\\|");
                        if(array!=null&&array.length==2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
//                    处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });

            }
        });
    }

    /*
        从SharedPreferences文件中读取存储的天气信息，并显示到界面：
     */

    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateReceiver.class);
        startService(intent);
    }



}
