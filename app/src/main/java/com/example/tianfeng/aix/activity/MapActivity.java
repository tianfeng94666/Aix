package com.example.tianfeng.aix.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import com.example.tianfeng.aix.R;
import com.example.tianfeng.aix.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by admin on 2016-04-26.
 */
public class MapActivity extends Activity implements LocationSource,
        AMapLocationListener, RadioGroup.OnCheckedChangeListener,AMap.OnMarkerClickListener,AMap.InfoWindowAdapter{
    private MapView mapView = null;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    //离我最近
    private TextView nearestshop;
    //所有店面
    private TextView actionbarshops;
    //我的位置
    private TextView actionlocate;

    // 兴趣点查询先关
    private PoiSearch search;
    //会返回查询数据的类
    private PoiSearch.Query query;
    // poi返回的结果
    private PoiResult poiResult;
    //poitems集合
    ArrayList<PoiItem> poiItems;
    //用户的纬度
    private double myaltitude;
    //用户的经度
    private double myLongitude;
    //用户所在省份
    private String myProvince;
    private int i= 0;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapactivity);
        initlayout(savedInstanceState);
    }

    private void initlayout(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);


        actionbarshops = (TextView) findViewById(R.id.actionbatshop);
        actionbarshops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent();
//                intent.setClass(MapActivity.this,XListViewActivity.class);
                intent.putParcelableArrayListExtra("poiItems",poiItems);
                startActivity(intent);
            }
        });
        actionlocate = (TextView) findViewById(R.id.actionbatlocate);
        actionlocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = false;
            }
        });

        nearestshop = (TextView) findViewById(R.id.actionbatTitle);
        nearestshop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PoiItem> list = new ArrayList<PoiItem>();
                list.add(findnearestshop(myaltitude,myLongitude,poiItems));

                aMap.clear();// 清理之前的图标
                PoiOverlay poiOverlay = new PoiOverlay(aMap, list);
                poiOverlay.removeFromMap();
                poiOverlay.addToMap();
                poiOverlay.zoomToSpan();
                aMap.moveCamera(CameraUpdateFactory.zoomTo(10));
            }
        });

        //地图初始化
        init();

    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(MapActivity.this, infomation);

    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式，参见类AMap。
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件


//        //自定义用户自己图标
//        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.amap_car));// 设置小蓝点的图标
//        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
//        myLocationStyle.strokeWidth(0f);// 设置圆形的边框粗细
//        aMap.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        System.out.println("-----"+i++);
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                if(flag == false){
                    myaltitude = aMapLocation.getAltitude();
                    myLongitude = aMapLocation.getLongitude();
                    myProvince = aMapLocation.getProvince();
                    //初始化数据
                    initdata();
                    flag = true;
                }
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);

            }
        }



    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

/**
 * 找出最近的咖啡店
 */

    public PoiItem findnearestshop(double altitude,double Longitude,List<PoiItem> poiItems){
        PoiItem nearestPoiItem = null ;
        double shortdistance = 1000000000;
        for(int i = 0; i<poiItems.size();i++){
            PoiItem poiItem = poiItems.get(i);
            LatLonPoint currentpoint = poiItem.getLatLonPoint();
            double currentaltitude = currentpoint.getLatitude();
            double currentLongitude=currentpoint.getLongitude();
            double a = Math.abs(altitude-currentaltitude);
            double b = Math.abs(Longitude - currentLongitude);
            double distance = Math.sqrt(a*a+b*b);
            if(distance < shortdistance){
                shortdistance = distance;
                nearestPoiItem = poiItem;
            }
        }
        return nearestPoiItem;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        // 调起高德地图app
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
            }
        });
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


    /**
     * 调起高德地图导航功能，如果没安装高德地图，会进入异常，可以在异常中处理，调起高德地图app的下载页面
     */
    public void startAMapNavi(Marker marker) {
        // 构造导航参数
        NaviPara naviPara = new NaviPara();
        // 设置终点位置
        naviPara.setTargetPoint(marker.getPosition());
        // 设置导航策略，这里是避免拥堵
        naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);

        // 调起高德地图导航
        try {
            AMapUtils.openAMapNavi(naviPara, getApplicationContext());
        } catch (AMapException e) {

            // 如果没安装会进入异常，调起下载页面
            AMapUtils.getLatestAMapApp(getApplicationContext());

        }

    }

    /**
     *查询数据，使得poiItems实例化
     */
    private void initdata(){

        // 初始化查询条件
        query = new PoiSearch.Query("埃克斯咖啡", null, myProvince);
        query.setPageSize(10);
        query.setPageNum(1);

        // 查询兴趣点
        search = new PoiSearch(MapActivity.this, query);
        // 异步搜索
        search.searchPOIAsyn();
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult result, int rCode) {
                if (rCode == 1000) {
                    if (result != null && result.getQuery() != null) {// 搜索poi的结果
                        if (result.getQuery().equals(query)) {// 是否是同一条
                            poiResult = result;
                            // 取得搜索到的poiitems有多少页
                            poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                            Log.v("tag", poiItems.toString());
                            List<SuggestionCity> suggestionCities = poiResult
                                    .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                            if (poiItems != null && poiItems.size() > 0) {
                                aMap.clear();// 清理之前的图标
                                PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                                poiOverlay.removeFromMap();
                                poiOverlay.addToMap();
                                poiOverlay.zoomToSpan();
                            } else if (suggestionCities != null
                                    && suggestionCities.size() > 0) {
                                showSuggestCity(suggestionCities);
                            } else {
                                ToastUtil.show(MapActivity.this,
                                        R.string.no_result);
                            }
                        }
                    } else {
                        ToastUtil.show(MapActivity.this,
                                R.string.no_result);
                    }
                } else {
                    ToastUtil.showerror(MapActivity.this, rCode);
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });

    }

}
