package com.example.lbstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import static com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING;
import static com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL;

public class MainActivity extends AppCompatActivity {

    private TextView positionText;//经纬度等信息显示
    private boolean isVisibility_flag = false;//控制是否显示经纬度
    private LocationClient mLocationClient;//定位客户端
    private MapView mapView;//地图显示
    private BaiduMap baiduMap;//地图总控制器
    private boolean isFirstLocate = true;//控制第一次经纬度显示次数
    private FloatingActionButton  Map_but;
    private Toolbar toolbar;//标题栏
    private DrawerLayout drawerLayout;//抽屉式布局


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        //注册位置监听器 , 当获取到位置信息时候,会回调
        mLocationClient.registerLocationListener(new MyLocationListener());
        //初始化：一定要在setContentView之前用
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        positionText = findViewById(R.id.position_text_view);
        mapView = findViewById(R.id.bmapView);
        Map_but =findViewById(R.id.Map_but);//浮选按钮

        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//开启 我 在地图上显示的功能
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);//设置好自定义的标题栏

        //下面几列是布局
        ActionBar actionBar = getSupportActionBar();//标题栏
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);//让导航栏显示出来
            actionBar.setHomeAsUpIndicator(R.drawable.daohanglan);//设置导航栏按钮
        }
        navigationView.setCheckedItem(R.id.nav_daohang);//设置为默认选中
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {//处理逻辑
                drawerLayout.closeDrawers();//关闭抽屉
                return true;
            }
        });


        //由于android 6.0运行时权限，所以要加上动态申请权限的代码,其中有三个属于危险权限,我们需要一次性申请三个权限
        //这里我们用到一个做法，首先创建一个空的List集合，然后判断这3个权限是否被授权，如果没有被授权就添加到集合中，如果已经授权则不添加
        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //然后判断List里是否有对象，有则代表需要动态申请权限
        if (!permissionList.isEmpty()) {//集合不为空则代表有需要申请的危险权限

            //调用toArray（）方法将List转换成数组,然后传入到ActivityCompat.requestPermissions（）一次性全部去申请
            String[] perimissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,perimissions,1);

        }else requestLocation();
    }
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    /*位置管理选择*/
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();//定位客户端选项类
        option.setScanSpan(2000);//设置2秒钟更新一次数据
        //High_Accuracy ：表示高精度模式，在GPS信号正常的情况下优先使用GPS，在无法接收GPS的时候使用网络定位，一般手机默认就是这种模式
        //Battery_Saving ：表示节电模式，只会使用网络定位
        //Device_Sensors ：表示传感器模式，只会使用GPS定位
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);//详细的位置信息
        option.setCoorType("bd09ll");//设置的是百度的定位标准
        mLocationClient.setLocOption(option);
    }
    @Override
    //只有权限全部允许才能运行
    //第一是请求的id，第二个是请求具体的权限，第三个是请求的结果
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result: grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"权限未全部通过",Toast.LENGTH_SHORT);
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(MainActivity.this,"未发生未知错误",Toast.LENGTH_SHORT);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /*监听线程，获得当前的经纬度，并显示*/
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                        if (location.getLocType() == BDLocation.TypeGpsLocation) {
                        positionText.setText(" Location Type  :  GPS "+"\n Longitude  :  "+location.getLongitude()+"\n Latitude  :  "+location.getLatitude()+"\n Developer  :  LongKai_G");
                    }else positionText.setText(" Location Type  :  Network "+"\n Longitude  :  "+location.getLongitude()+"\n Latitude  :  "+location.getLatitude()+"\n Developer  :  LongKai_G");
                        navigateTo(location);
                    }
                    //按锁定自己位置按钮改变isFirslLocate可以再次回到地图
                    Map_but.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isFirstLocate = true;
                        }
                    });
                    //隐藏百度logo
                    View child = mapView.getChildAt(1);
                    if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
                        child.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }
    }
    private void navigateTo(BDLocation location){//封装地点信息和我的信息, 并把信息传到地图显示

        if(isFirstLocate){//防止多次调用，这个方法只需要调用一次 地图移动到我们当前位置
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());//传入纬度（第一个参数）和经度
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update =MapStatusUpdateFactory.zoomTo(17f);//缩放 3-19之间选择值越大越清晰
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData locData = new MyLocationData.Builder()
                //获取半径
                .accuracy(location.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.getDirection())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        baiduMap.setMyLocationData(locData);
        //定位模式、是否开启方向、设置自定义定位图标、精度圈填充颜色以及精度圈边框颜色5个属性。
        //地图SDK支持三种定位模式：NORMAL（普通态）, FOLLOWING（跟随态）, COMPASS（罗盘态）
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.location);
        MyLocationConfiguration myLocationConfiguration =new MyLocationConfiguration(NORMAL,true,
                mCurrentMarker,0x3290A4F7,0xAA212101);
        baiduMap.setMyLocationConfiguration(myLocationConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//加载toolbar这个菜单文件
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//menu上面的item按键具体实现

        switch (item.getItemId()){
            case R.id.backup:
                if (isVisibility_flag){
                    positionText.setVisibility(View.VISIBLE);//显示经纬度信息
                    Toast.makeText(this,"显示信息",Toast.LENGTH_SHORT).show();
                    isVisibility_flag = false;
                }else{
                    positionText.setVisibility(View.GONE);//隐藏经纬度信息
                    isVisibility_flag = true;
                }
                break;
            case R.id.delete:
                Toast.makeText(this,"清除",Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this,"you clicked settings",Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home://注意 前面加android 而且导航栏按钮 id 一直是android.R.id.home
                drawerLayout.openDrawer(GravityCompat.START);//打开向Start滑动的菜单
        }
        return true;
    }

    public void onConnectHotSpotMessage(String s, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);

    }
}