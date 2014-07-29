package com.huanghua.mysecret.service;

import com.huanghua.mysecret.util.CommonUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DateQueryService extends Service {

    public static final String TAG = "query_service";

    @Override
    public IBinder onBind(Intent intent) {
        CommonUtils.showLog(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        CommonUtils.showLog(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        CommonUtils.showLog(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CommonUtils.showLog(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

}
