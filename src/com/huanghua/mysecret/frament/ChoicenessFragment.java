package com.huanghua.mysecret.frament;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.ui.WriteSecretActivity;

/***
 * 精选秘密
 * 
 * @author huanghua
 * 
 */
public class ChoicenessFragment extends FragmentBase implements
        View.OnClickListener {

    private ImageButton mWriteSecret = null;
    private View mTopView = null;
    private RadioGroup mTitleRadioGroup = null;
    private ViewPager mPager;
    private VeryTopFragment mTopFragment = null;
    private VeryNewFragment mNewFragment = null;
    private static final int TAB_VERY_TOP = 0;
    private static final int TAB_VERY_NEW = 1;
    private static final int TAB_COUNT = 2;

    private RadioGroup.OnCheckedChangeListener mRadioListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
            case R.id.radio_hot:
                mPager.setCurrentItem(TAB_VERY_TOP);
                break;
            case R.id.radio_new:
                mPager.setCurrentItem(TAB_VERY_NEW);
                break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_choiceness, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init() {
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new ViewPagerAdapter(getActivity()
                .getFragmentManager()));
        mPager.setOnPageChangeListener(new PageChangeListener());

        mWriteSecret = (ImageButton) findViewById(R.id.write_secret);
        mWriteSecret.setOnClickListener(this);

        mTopView = findViewById(R.id.top_view);
        mTopView.setOnClickListener(this);
        mTitleRadioGroup = (RadioGroup) findViewById(R.id.choicess_radio);
        mTitleRadioGroup.setVisibility(View.VISIBLE);
        mTitleRadioGroup.setOnCheckedChangeListener(mRadioListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        int checkId = mTitleRadioGroup.getCheckedRadioButtonId();
        switch (checkId) {
        case R.id.radio_hot:
            mPager.setCurrentItem(TAB_VERY_TOP, false);
            break;
        case R.id.radio_new:
        default:
            mPager.setCurrentItem(TAB_VERY_NEW, false);
            break;
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mTopView) {
            int checkId = mTitleRadioGroup.getCheckedRadioButtonId();
            switch (checkId) {
            case R.id.radio_hot:
                mTopFragment.toListViewTop();
                break;
            case R.id.radio_new:
                mNewFragment.toListViewTop();
                break;
            }
        } else if (((BaseActivity) getActivity()).checkUserLogin()) {
            return;
        }
        if (v == mWriteSecret) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WriteSecretActivity.class);
            startAnimActivity(intent);
        }
    }

    public void toTopSelect() {
        int checkId = mTitleRadioGroup.getCheckedRadioButtonId();
        switch (checkId) {
        case R.id.radio_hot:
            mTopFragment.toTopSelect();
            break;
        case R.id.radio_new:
            mNewFragment.toTopSelect();
            break;
        }
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            switch (arg0) {
            case TAB_VERY_TOP:
                if (mTopFragment == null) {
                    mTopFragment = new VeryTopFragment();
                }
                return mTopFragment;
            case TAB_VERY_NEW:
                if (mNewFragment == null) {
                    mNewFragment = new VeryNewFragment();
                }
                return mNewFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }
    }

    private class PageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            checkRadio(arg0);
        }

    }

    private void checkRadio(int itemId) {
        switch (itemId) {
        case TAB_VERY_TOP:
            mTitleRadioGroup.check(R.id.radio_hot);
            break;
        case TAB_VERY_NEW:
            mTitleRadioGroup.check(R.id.radio_new);
            break;
        }
    }

    public void onAttachFragment() {
        mTopFragment = new VeryTopFragment();
        mNewFragment = new VeryNewFragment();
    }
}
