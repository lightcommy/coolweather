package com.commy.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.commy.db.CoolWeatherOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Commy on 2016/4/20.
 * 把常用的数据库操作封装起来。
 */
public class CoolWeatherDB {
//    数据库名：
    public static final String DB_NAME="cool_weather";
//    数据库版本：
    public static final int VERSION=16;
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;
    /*
        将构造方法私有化：
        提供了一个getInstance()方法来获取实例，这样就可以保证全局范围内只会有一个CoolWeatherDB的实例。
     */
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
//        通过SQLiteOpenHelper的实例getWritableDatabase()返回一个可以对数据库进行读写操作的对象。
        db = dbHelper.getWritableDatabase();
    }
    /*
        获取CoolWeatherDB的实例：
     */
    public synchronized static CoolWeatherDB getInstance(Context context){
        if(coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /*
        将province实例存储到数据库：
     */
    public void saveProvince(Province province){
        if(province != null){
            db.execSQL("insert into Province(province_name,province_code)values(?,?)",
                    new String[]{province.getProvinceName(), province.getProvinceCode()});
        }
    }
    /*
        从数据库中读取全国所有的省份信息：
     */
    public List<Province> loadProvinces(){
        List<Province> list = new ArrayList<Province>();
        Cursor cursor = db.rawQuery("select * from Province", null);
        if(cursor.moveToFirst()){
            do{
                Province province  = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while(cursor.moveToNext());
        }
        if(cursor!=null){
             cursor.close();
        }
        return list;
    }
    /*
        将city实例存储到数据库
     */
//    todo 3.6
    public void saveCity(City city){
        if(city!=null){
            db.execSQL("insert into City(city_name,city_code,province_id)" +
                    "values(?,?,?)", new String[]{
                    city.getCityName(), city.getCityCode(), String.valueOf(city.getProvinceId())
            });
        }
    }
    /*
        从数据库读取某省下所有的城市信息：
     */
    public List<City> loadCities(int provinceId){
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.rawQuery("select * from City where province_id ="+provinceId,null);
        if(cursor.moveToFirst()){
            do{
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            }while(cursor.moveToNext());
        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }
    /*
        将County实例存储到数据库：
     */
    public void saveCounty(County county){
        if(county!=null){
            db.execSQL("insert into County(county_name,county_code,city_id)values(?,?,?)",
                    new String[]{county.getCountyName(), county.getCountyCode(), String.valueOf(county.getCityId())});
        }
    }
    /*
        从数据库中读取某城市下所有县信息：
     */
    public List<County> loadCounties(int cityId){
        List<County> list = new ArrayList<County>();
        Cursor cursor = db.rawQuery("select * from County where city_id ="+cityId,null);
        if(cursor.moveToFirst()){
            do{
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            }while(cursor.moveToNext());
        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }






}
