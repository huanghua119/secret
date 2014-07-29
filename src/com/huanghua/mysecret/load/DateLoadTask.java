package com.huanghua.mysecret.load;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.load.DateLoad.OnDateLoadCompleteListener;
import com.huanghua.mysecret.util.CommonUtils;

import android.content.Context;
import android.os.Handler;

public class DateLoadTask implements Runnable {
    private static final String TAG = "date_load";
    Context mContext = null;
    Handler mHanler = null;
    Secret mSecret = null;
    OnDateLoadCompleteListener mListener = null;
    long mPriority = -1;

    private DateLoadTask() {
    }

    public DateLoadTask(Context context, OnDateLoadCompleteListener listener,
            Handler handler, Secret secret, long priority) {
        this.mContext = context;
        this.mHanler = handler;
        this.mListener = listener;
        this.mSecret = secret;
        this.mPriority = priority;
    }

    @Override
    public void run() {
        BmobQuery<SecretSupport> ssQuery = new BmobQuery<SecretSupport>();
        ssQuery.addWhereEqualTo("secret", mSecret);
        ssQuery.findObjects(mContext, new FindListener<SecretSupport>() {
            @Override
            public void onSuccess(List<SecretSupport> arg0) {
                CommonUtils.showLog(TAG, "query SecretSupport success: " + arg0
                        + " mPriority:" + mPriority);
                int happy = 0;
                int cry = 0;
                for (SecretSupport ss : arg0) {
                    if (ss.isSupport()) {
                        happy++;
                    } else {
                        cry++;
                    }
                }
            }

            @Override
            public void onError(int arg0, String arg1) {
                CommonUtils.showLog(TAG, "query SecretSupport error: " + arg1);
            }
        });

        BmobQuery<Comment> mQueryComent = new BmobQuery<Comment>();
        mQueryComent.addWhereEqualTo("secret", mSecret);
        mQueryComent.findObjects(mContext, new FindListener<Comment>() {
            @Override
            public void onError(int arg0, String arg1) {
            }

            @Override
            public void onSuccess(List<Comment> arg0) {
            }
        });
    }

}
