package com.huanghua.mysecret.load;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.CountListener;
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
    int mPriority = -1;

    public DateLoadTask(Context context, OnDateLoadCompleteListener listener,
            Handler handler, Secret secret, int priority) {
        this.mContext = context;
        this.mHanler = handler;
        this.mListener = listener;
        this.mSecret = secret;
        this.mPriority = priority;
    }

    @Override
    public void run() {
        DateLoadThreadManager.removeTask(mSecret.getObjectId());
        BmobQuery<SecretSupport> ssQuery = new BmobQuery<SecretSupport>();
        ssQuery.addWhereEqualTo("secret", mSecret);
        boolean hasNetWork = CommonUtils.isNetworkAvailable(mContext);
        if (hasNetWork) {
            ssQuery.setCachePolicy(CachePolicy.NETWORK_ONLY);
        } else {
            ssQuery.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
        }
        ssQuery.setLimit(1000);
        ssQuery.findObjects(mContext, new FindListener<SecretSupport>() {
            @Override
            public void onSuccess(final List<SecretSupport> arg0) {
                CommonUtils.showLog(TAG, "query SecretSupport success: " + arg0
                        + " mPriority:" + mPriority);
                DateLoad.put(mSecret.getObjectId(), arg0);
                mHanler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.OnLoadSecretSupportComplete(mPriority, arg0);
                    }
                });
            }

            @Override
            public void onError(int arg0, String arg1) {
                CommonUtils.showLog(TAG, "query SecretSupport error: " + arg1);
            }
        });

        BmobQuery<Comment> mQueryComent = new BmobQuery<Comment>();
        mQueryComent.addWhereEqualTo("secret", mSecret);
        if (hasNetWork) {
            mQueryComent.setCachePolicy(CachePolicy.NETWORK_ONLY);
        } else {
            mQueryComent.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
        }
        mQueryComent.count(mContext, Comment.class, new CountListener() {
            @Override
            public void onSuccess(final int arg0) {
                CommonUtils.showLog(TAG, "query Comment success: " + arg0
                        + " mPriority:" + mPriority);
                DateLoad.putComment(mSecret.getObjectId(), arg0);
                mHanler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.OnLoadSecretCommentComplete(mPriority, arg0);
                    }
                });
            }
            
            @Override
            public void onFailure(int arg0, String arg1) {
                
            }
        });
    }

}
