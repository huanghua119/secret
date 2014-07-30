package com.huanghua.mysecret.ui;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.frament.ChoicenessFragment;
import com.huanghua.mysecret.frament.MoreFragment;
import com.huanghua.mysecret.load.DateLoad;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends BaseActivity {

    private Button[] mTabs;
    private Fragment[] fragments;
    private int mIndex;
    private int mCurrentTabIndex;
    private ImageView mChoiceness_tips;
    private ImageView mNearby_tips;

    private ChoicenessFragment mChoicenessFrament;
    private ChoicenessFragment mChoicenessFrament2;
    private MoreFragment mMoreFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initTab();
    }

    private void initView() {
        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_choiceness);
        mTabs[1] = (Button) findViewById(R.id.btn_nearby);
        mTabs[2] = (Button) findViewById(R.id.btn_more);
        mChoiceness_tips = (ImageView) findViewById(R.id.iv_choiceness_tips);
        mNearby_tips = (ImageView) findViewById(R.id.iv_nearby_tips);
        // 把第一个tab设为选中状态
        mTabs[0].setSelected(true);
    }

    private void initTab() {
        mChoicenessFrament = new ChoicenessFragment();
        mChoicenessFrament2 = new ChoicenessFragment();
        mMoreFragment = new MoreFragment();
        fragments = new Fragment[] { mChoicenessFrament, mChoicenessFrament2,
                mMoreFragment };
        // 添加显示第一个fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mChoicenessFrament)
                .show(mChoicenessFrament).commit();
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
        mChoiceness_tips.setVisibility(View.GONE);
        mNearby_tips.setVisibility(View.GONE);
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
            if (CustomApplcation.mLocationClient.isStarted()) {
                CustomApplcation.mLocationClient.stop();
            }
            super.onBackPressed();
        } else {
            ShowToastOld(R.string.pass_exit);
        }
        firstTime = System.currentTimeMillis();
    }


    @Override
    protected void onDestroy() {
        DateLoad.clearAll();
        super.onDestroy();
    }
}
