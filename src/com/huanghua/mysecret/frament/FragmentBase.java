package com.huanghua.mysecret.frament;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * Fragmenet 基类
 */
public abstract class FragmentBase extends Fragment {

    protected View contentView;

    public LayoutInflater mInflater;

    Toast mToast;

    private Handler handler = new Handler();

    public void runOnWorkThread(Runnable action) {
        new Thread(action).start();
    }

    public void runOnUiThread(Runnable action) {
        handler.post(action);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mApplication = CustomApplcation.getInstance();
        mInflater = LayoutInflater.from(getActivity());
    }

    public FragmentBase() {

    }

    public void ShowToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }

    public void ShowToast(int text) {
        if (mToast == null) {
            mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        }
        mToast.setText(text);
        mToast.show();
    }

    public View findViewById(int paramInt) {
        return getView().findViewById(paramInt);
    }

    public CustomApplcation mApplication;

    /**
     * 动画启动页面 startAnimActivity
     * 
     * @throws
     */
    public void startAnimActivity(Intent intent) {
        this.startActivity(intent);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }

    public void startAnimActivity(Class<?> cla) {
        getActivity().startActivity(new Intent(getActivity(), cla));
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }

    public void showLog(String msg) {
        if (CustomApplcation.DEBUG) {
            Log.i(CustomApplcation.TAG, msg);
        }
    }

}
