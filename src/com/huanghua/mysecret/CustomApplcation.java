package com.huanghua.mysecret;

import java.io.File;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import cn.bmob.v3.datatype.BmobGeoPoint;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.util.CacheUtils;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * 自定义全局Applcation类
 */
public class CustomApplcation extends Application {

    public static final String TAG = "secret_hh";
    public static final boolean DEBUG = true;
    public static CustomApplcation mInstance;

    public static BmobGeoPoint lastPoint = null;// 上一次定位到的经纬度
    
    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;
    private static BmobGeoPoint mBp = null;
    private static String mAddress = null;
    private final int UP_TIME = 60 * 1000 * 10;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        init();
        InitLocation();
    }

    private void init() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.notify);
        mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        initImageLoader(getApplicationContext());
        mBp = new BmobGeoPoint();
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        mAddress = getString(R.string.unknown_address);
    }

    /** 初始化ImageLoader */
    public static void initImageLoader(Context context) {
        File cacheDir = CacheUtils.getCacheDirectory(context, true, "pic");// 获取到缓存的目录地址
        // 创建配置ImageLoader(所有的选项都是可选的,只使用那些你真的想定制)，这个可以设定在APPLACATION里面，设置为全局的配置参数
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                // 线程池内加载的数量
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new WeakMemoryCache())
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                // 将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .discCache(new UnlimitedDiscCache(cacheDir))// 自定义缓存路径
                // .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);// 全局初始化此配置
    }

    public static CustomApplcation getInstance() {
        return mInstance;
    }

    // 单例模式，才能及时返回数据
    SharePreferenceUtil mSpUtil;
    public static final String PREFERENCE_NAME = "mysecret_sharedinfo";

    public synchronized SharePreferenceUtil getSpUtil() {
        if (mSpUtil == null) {
            mSpUtil = new SharePreferenceUtil(this, PREFERENCE_NAME);
        }
        return mSpUtil;
    }

    NotificationManager mNotificationManager;

    public NotificationManager getNotificationManager() {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        return mNotificationManager;
    }

    MediaPlayer mMediaPlayer;

    public synchronized MediaPlayer getMediaPlayer() {
        if (mMediaPlayer == null)
            mMediaPlayer = MediaPlayer.create(this, R.raw.notify);
        return mMediaPlayer;
    }

    public final String PREF_LONGTITUDE = "longtitude";// 经度
    private String longtitude = "";

    /**
     * 获取经度
     * 
     * @return
     */
    public String getLongtitude() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        longtitude = preferences.getString(PREF_LONGTITUDE, "");
        return longtitude;
    }

    /**
     * 设置经度
     * 
     * @param pwd
     */
    public void setLongtitude(String lon) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if (editor.putString(PREF_LONGTITUDE, lon).commit()) {
            longtitude = lon;
        }
    }

    public final String PREF_LATITUDE = "latitude";// 经度
    private String latitude = "";

    /**
     * 获取纬度
     * 
     * @return
     */
    public String getLatitude() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        latitude = preferences.getString(PREF_LATITUDE, "");
        return latitude;
    }

    /**
     * 设置维度
     * 
     * @param pwd
     */
    public void setLatitude(String lat) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        if (editor.putString(PREF_LATITUDE, lat).commit()) {
            latitude = lat;
        }
    }

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Battery_Saving);
        option.setCoorType("gcj02");
        option.setIsNeedAddress(true);
        option.setScanSpan(UP_TIME);
        mLocationClient.setLocOption(option);
    }
    
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null) {
                int error = location.getLocType();
                CommonUtils.showLog(DateQueryService.TAG, "onReceiveLocation error: "+ error);
                mBp.setLatitude(location.getLatitude());
                mBp.setLongitude(location.getLongitude());
                if (location.getCity() != null && location.getDistrict() != null) {
                    mAddress = location.getCity() + " " +location.getDistrict();
                } else if (location.getCity() != null && location.getDistrict() == null) {
                    mAddress = location.getCity();
                } else {
                    mLocationClient.requestLocation();
                    mAddress = getString(R.string.unknown_address);
                }
                if (error == 161) {
                    sendBroadcast(new Intent("update_near_secret_location"));
                }
            } else {
                mLocationClient.requestLocation();
            }
        }
    }

    public static BmobGeoPoint getLocation() {
        return mBp;
    }

    public static String getAddress() {
        return mAddress;
    }
}
