package com.huanghua.mysecret.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.config.Config;
import com.huanghua.mysecret.service.DateQueryService;

/**
 * 引导页
 */
public class SplashActivity extends BaseActivity {

    private static final int GO_HOME = 100;
    private static final int GO_LOGIN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // BmobIM SDK初始化--只需要这一段代码即可完成初始化
        Bmob.initialize(this, Config.applicationId);
        //BmobInstallation.getCurrentInstallation(this).save();
        userManager.initCurrentUser();
        Installation in = new Installation(this);
        in.setUser(userManager.getCurrentUser());
        in.save(this);
        BmobPush .startWork(this, Config.applicationId);
        startService(new Intent(this, DateQueryService.class));

        mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
        mRunFinishAnim = false;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GO_HOME:
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
                break;
            case GO_LOGIN:
                //startAnimActivity(LoginActivity.class);
                finish();
                break;
            }
        }
    };

}
