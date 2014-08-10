package com.huanghua.mysecret.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.baidu.location.LocationClient;
import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.ApkBean;
import com.huanghua.mysecret.frament.ChoicenessFragment;
import com.huanghua.mysecret.frament.MoreFragment;
import com.huanghua.mysecret.frament.NearSecretFragment;
import com.huanghua.mysecret.load.DateLoad;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.huanghua.mysecret.util.ThemeUtil;

public class MainActivity extends BaseActivity {

    private Button[] mTabs;
    private Fragment[] fragments;
    private int mIndex;
    private int mCurrentTabIndex;
    private ImageView mChoiceness_tips;
    private ImageView mNearby_tips;
    private ImageView mMore_tips;

    private ChoicenessFragment mChoicenessFrament;
    private NearSecretFragment mNearSecretFrament;
    private MoreFragment mMoreFragment;
    private SharePreferenceUtil mSp = null;
    private LocationClient mLocationClient;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null
                    && DateQueryService.QUERY_NEW_SECRTE_ACTION.equals(action)) {
                if (DateQueryService.sHasNewSecret) {
                    mChoiceness_tips.setVisibility(View.VISIBLE);
                } else {
                    mChoiceness_tips.setVisibility(View.GONE);
                }
            } else if (action != null
                    && DateQueryService.CHECK_NEW_VERSION_UPDATE.equals(action)) {
                ApkBean apkBean = (ApkBean) intent.getSerializableExtra("apk");
                if (apkBean != null) {
                    CommonUtils.createUpdateVersionDialog(context, apkBean)
                            .show();
                }
            } else if (action != null
                    && DateQueryService.PUSH_ACTION_NEW_MESSAGE
                            .equals(action)) {
                mMore_tips.setVisibility(View.VISIBLE);
                mMoreFragment.showNewTips(View.VISIBLE);
            } else if (action != null && "update_near_secret_location".equals(action)) {
                mNearSecretFrament.refreshDate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DateQueryService.QUERY_NEW_SECRTE_ACTION);
        intentFilter.addAction(DateQueryService.CHECK_NEW_VERSION_UPDATE);
        intentFilter.addAction(DateQueryService.PUSH_ACTION_NEW_MESSAGE);
        intentFilter.addAction("update_near_secret_location");
        registerReceiver(mBroadcastReceiver, intentFilter);
        initView();
        initTab();
        if (mSp == null) {
            mSp = CustomApplcation.getInstance().getSpUtil();
        }
        mLocationClient = ((CustomApplcation)getApplication()).mLocationClient;
    }

    private void initView() {
        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_choiceness);
        mTabs[1] = (Button) findViewById(R.id.btn_nearby);
        mTabs[2] = (Button) findViewById(R.id.btn_more);
        mChoiceness_tips = (ImageView) findViewById(R.id.iv_choiceness_tips);
        mNearby_tips = (ImageView) findViewById(R.id.iv_nearby_tips);
        mMore_tips = (ImageView) findViewById(R.id.iv_moreby_tips);
    }

    private void initTab() {
        mChoicenessFrament = new ChoicenessFragment();
        mNearSecretFrament = new NearSecretFragment();
        mMoreFragment = new MoreFragment();
        fragments = new Fragment[] { mChoicenessFrament, mNearSecretFrament,
                mMoreFragment };
        FragmentTransaction trx = getSupportFragmentManager()
                .beginTransaction();
        trx.add(R.id.fragment_container, mChoicenessFrament);
        trx.add(R.id.fragment_container, mNearSecretFrament);
        trx.add(R.id.fragment_container, mMoreFragment);
        trx.commit();
        // 添加显示第一个fragment
        if (ThemeUtil.isThemeFinish(this)) {
            getSupportFragmentManager().beginTransaction()
            .hide(mNearSecretFrament)
            .hide(mChoicenessFrament)
            .show(mMoreFragment).commit();
            mTabs[2].setSelected(true);
            mCurrentTabIndex = 2;
            ThemeUtil.setThemeFinish(this, false);
        } else {
            getSupportFragmentManager().beginTransaction()
            .hide(mNearSecretFrament)
            .hide(mMoreFragment)
            .show(mChoicenessFrament).commit();
            mTabs[0].setSelected(true);
            mCurrentTabIndex = 0;
        }
    }

    /**
     * button点击事件
     * 
     * @param view
     */
    public void onTabSelect(View view) {
        switch (view.getId()) {
        case R.id.btn_choiceness:
            mIndex = 0;
            if (mCurrentTabIndex == mIndex) {
                mChoicenessFrament.toTopSelect();
            }
            break;
        case R.id.btn_nearby:
            mIndex = 1;
            if (mCurrentTabIndex == mIndex) {
                mNearSecretFrament.toTopSelect();
            }
            break;
        case R.id.btn_more:
            mIndex = 2;
            break;
        }
        if (mCurrentTabIndex != mIndex) {
            FragmentTransaction trx = getSupportFragmentManager()
                    .beginTransaction();
            trx.hide(fragments[mCurrentTabIndex]);
            if (!fragments[mIndex].isAdded()) {
                trx.add(R.id.fragment_container, fragments[mIndex]);
            }
            trx.show(fragments[mIndex]).commit();
        }
        mTabs[mCurrentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        mTabs[mIndex].setSelected(true);
        mCurrentTabIndex = mIndex;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationClient.start();
        mNearby_tips.setVisibility(View.GONE);
        mChoiceness_tips.setVisibility(View.GONE);
        if (DateQueryService.sHasNewSecret) {
            mChoiceness_tips.setVisibility(View.VISIBLE);
        }
        if (mSp.hasNewMessage()) {
            mMore_tips.setVisibility(View.VISIBLE);
            mMoreFragment.showNewTips(View.VISIBLE);
        } else {
            mMore_tips.setVisibility(View.GONE);
            mMoreFragment.showNewTips(View.GONE);
        }
    }

    @Override
    public void finish() {
        mRunFinishAnim = false;
        super.finish();
    }

    private static long firstTime;

    /**
     * 连续按两次返回键就退出
     */
    @Override
    public void onBackPressed() {
        if (firstTime + 2000 > System.currentTimeMillis()) {
            if (mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
            super.onBackPressed();
        } else {
            ShowToastOld(R.string.pass_exit);
        }
        firstTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        if (!mSwitchTheme) {
            DateLoad.clearAll();
        }
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        super.onDestroy();
    }

}
