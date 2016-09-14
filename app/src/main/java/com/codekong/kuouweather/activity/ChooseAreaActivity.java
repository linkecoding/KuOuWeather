package com.codekong.kuouweather.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codekong.kuouweather.R;
import com.codekong.kuouweather.db.KuOuWeatherDB;
import com.codekong.kuouweather.model.City;
import com.codekong.kuouweather.model.County;
import com.codekong.kuouweather.model.Province;
import com.codekong.kuouweather.net.HttpCallBackListener;
import com.codekong.kuouweather.net.HttpMethod;
import com.codekong.kuouweather.net.NetConnection;
import com.codekong.kuouweather.util.HandleResponse;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;
    private ListView areaListView;
    //区域ListView适配器
    private ArrayAdapter<String> adapter;
    //ListView的数据
    private List<String> dataList = new ArrayList<>();
    //数据库操作实例
    private KuOuWeatherDB kuOuWeatherDB;

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;

    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area);
        initView();
        initEvent();
    }

    /**
     * 初始化布局控件
     */
    private void initView() {
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        titleText = (TextView) findViewById(R.id.title_text);
        areaListView = (ListView) findViewById(R.id.area_list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        areaListView.setAdapter(adapter);
        kuOuWeatherDB = KuOuWeatherDB.getInstance(this);
        areaListView.setOnItemClickListener(this);
        //加载省级数据
        queryProvinces();
    }

    /**
     * 查询全国所有的省,优先从数据库查询,若数据库没有则从服务器请求
     */
    private void queryProvinces(){
        provinceList = kuOuWeatherDB.loadProvinces();
        if (provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                //将省份名字加入List中
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            titleText.setText(R.string.str_china);
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询某省下面所有的市,优先从数据库查询,若数据库没有则从服务器请求
     */
    private void queryCities(){
        cityList = kuOuWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            //根据ProvinceCode去查找该省下面所有的市
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 查询某市下面所有的县,优先从数据库查询,若数据库没有则从服务器请求
     */
    private void queryCounties(){
        //根据城市id去数据库加载下面所有的县的数据
        countyList = kuOuWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }
    private void queryFromServer(String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            //根据code加载市或县的数据
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }else{
            //加载省的数据
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        //显示加载中对话框
        showProgressDialog();

        new NetConnection(address, HttpMethod.GET, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    //请求处理省份数据
                    result = HandleResponse.handleProvincesResponse(kuOuWeatherDB, response);
                }else if ("city".equals(type)){
                    //请求处理城市信息
                    result = HandleResponse.handleCitiesResponse(kuOuWeatherDB, response, selectedProvince.getId());
                }else if ("county".equals(type)){
                    //请求县的信息
                    result = HandleResponse.handleCountiesResponse(kuOuWeatherDB, response, selectedCity.getId());
                }

                if (result){
                    //通过runOnUiThread()回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭加载对话框
                            closeProgressDialog();
                            if ("province".equals(type)){
                                //由于前面存在网络请求,此时数据库中已经存在数据,可以直接从数据库中加载
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }

                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, new String[]{});
    }

    /**
     * 显示加载中对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载中……");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭加载中对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 处理Back键,判断是加载对应的数据还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            //县显示返回一级则显示城市
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }

    /**
     * ListView点击事件处理
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentLevel == LEVEL_PROVINCE){
            selectedProvince = provinceList.get(position);
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            selectedCity = cityList.get(position);
            queryCounties();
        }
    }
}
