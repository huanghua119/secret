
package com.huanghua.mysecret.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;

public class BaseActivity extends FragmentActivity {

    CustomApplcation mApplication;

    protected int mScreenWidth;
    protected int mScreenHeight;
    public LayoutInflater mInFlater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = CustomApplcation.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;
        mInFlater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    Toast mToast;
    Toast mToast2;

    public void ShowToast(final String text) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mToast == null) {
                        mToast = new Toast(BaseActivity.this);
                        mToast.setDuration(Toast.LENGTH_SHORT);
                        mToast.setView(mInFlater.inflate(R.layout.toast_view, null));
                        mToast.setGravity(Gravity.CENTER, 0, 0);
                    }
                    View toast = mToast.getView();
                    TextView m = (TextView) toast.findViewById(R.id.toast_msg);
                    m.setText(text);
                    mToast.show();
                }
            });

        }
    }

    public void ShowToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mToast == null) {
                    mToast = new Toast(BaseActivity.this);
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(mInFlater.inflate(R.layout.toast_view, null));
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                }
                View toast = mToast.getView();
                TextView m = (TextView) toast.findViewById(R.id.toast_msg);
                m.setText(resId);
                mToast.show();
            }
        });
    }

    public void ShowToastOld(String text) {
        if (mToast2 == null) {
            mToast2 = Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT);
        }
        mToast2.setText(text);
        mToast2.show();
    }

    public void ShowToastOld(int text) {
        if (mToast2 == null) {
            mToast2 = Toast.makeText(BaseActivity.this, text, Toast.LENGTH_SHORT);
        }
        mToast2.setText(text);
        mToast2.show();
    }

    public void startAnimActivity(Class<?> cla) {
        this.startActivity(new Intent(this, cla));
    }

    public void startAnimActivity(Intent intent) {
        this.startActivity(intent);
    }

}
