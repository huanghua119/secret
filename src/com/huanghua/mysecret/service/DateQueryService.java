package com.huanghua.mysecret.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.bean.Secret;
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
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (CommonUtils.isNetworkAvailable(DateQueryService.this)) {
                    queryNewSecret();
                }
            }
        }, 0, QUERY_PERIOD_TIME);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        CommonUtils.showLog(TAG, "onDestroy");
        mTimer.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CommonUtils.showLog(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private Timer mTimer = null;
    private BmobQuery<Secret> mQuerySecret = null;
    public static String mLastSecretId = null;
    public static boolean sHasNewSecret = false;
    public static int sSecretCount = 0;
    public static final String QUERY_NEW_SECRTE_ACTION = "query_new_secret_action";
    private static final int QUERY_PERIOD_TIME = 10 * 1000 * 60;
    private FindListener<Secret> mQuerySecretListener = new FindListener<Secret>() {
        @Override
        public void onError(int arg0, String arg1) {
        }

        @Override
        public void onSuccess(List<Secret> arg0) {
            if (arg0.size() != 0) {
                Secret secret = arg0.get(0);
                if (sHasNewSecret) {
                    mLastSecretId = secret.getObjectId();
                    return;
                }
                sHasNewSecret = false;
                if (mLastSecretId != null) {
                    if (!secret.getObjectId().equals(mLastSecretId)) {
                        sHasNewSecret = true;
                    }
                }
                mLastSecretId = secret.getObjectId();
                if (sHasNewSecret) {
                    sendBroadcast(new Intent(QUERY_NEW_SECRTE_ACTION));
                }
            }
        }
    };
    private CountListener mQuerySecretCountListener = new CountListener() {

        @Override
        public void onSuccess(int count) {
            sSecretCount = count;
        }

        @Override
        public void onFailure(int arg0, String arg1) {

        }
    };

    private void queryNewSecret() {
        CommonUtils.showLog(TAG, "queryNewSecret");
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Secret>();
            mQuerySecret.order("-createdAt");
            mQuerySecret.include("user");
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
        }
        mQuerySecret.findObjects(this, mQuerySecretListener);
        mQuerySecret.count(this, Secret.class, mQuerySecretCountListener);
    }
}
