package com.huanghua.mysecret.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.manager.UserManager;

public class SupportView extends LinearLayout implements View.OnClickListener {

    private Secret mCurrentSecret = null;
    private SecretSupport mCuSupport = null;
    private User mCurrentUser;
    private boolean mIsCheck = false;
    private boolean mClickEable = true;

    private TextView mHappy = null;
    private TextView mCry = null;
    private TextView mCommit = null;

    public SupportView(Context context) {
        this(context, null);
    }

    public SupportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCurrentUser = UserManager.getInstance(getContext()).getCurrentUser();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHappy = (TextView) findViewById(R.id.item_support_happy);
        mCry = (TextView) findViewById(R.id.item_support_cry);
        mCommit = (TextView) findViewById(R.id.item_commit);
        mCry.setOnClickListener(this);
        mHappy.setOnClickListener(this);
        mCommit.setOnClickListener(this);

    }

    public void startQuery() {
        mIsCheck = false;
        mCuSupport = null;
        BmobQuery<SecretSupport> ssQuery = new BmobQuery<SecretSupport>();
        ssQuery.addWhereEqualTo("secret", mCurrentSecret);
        ssQuery.findObjects(getContext(), new FindListener<SecretSupport>() {
            @Override
            public void onSuccess(List<SecretSupport> arg0) {
                showLog("query SecretSupport success: " + arg0);
                int happy = 0;
                int cry = 0;
                for (SecretSupport ss : arg0) {
                    checkClick(ss);
                    if (ss.isSupport()) {
                        happy++;
                    } else {
                        cry++;
                    }
                }
                mHappy.setText(happy + "");
                mCry.setText(cry + "");
            }

            @Override
            public void onError(int arg0, String arg1) {
                showLog("query SecretSupport error: " + arg1);
            }
        });
    }

    private void checkClick(SecretSupport ss) {
        if (!mIsCheck && ss.getFromUser().equals(mCurrentUser)) {
            mCuSupport = ss;
            mIsCheck = true;
            if (ss.isSupport()) {
                mCry.setClickable(true);
                mHappy.setClickable(false);
                mHappy.setSelected(true);
                mCry.setSelected(false);
            } else {
                mHappy.setClickable(true);
                mCry.setClickable(false);
                mHappy.setSelected(false);
                mCry.setSelected(true);
            }
        }
    }

    public void showLog(String msg) {
        if (CustomApplcation.DEBUG) {
            Log.i(CustomApplcation.TAG, msg);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCommit) {

        } else {
            if (mCuSupport != null) {
                mCuSupport.delete(getContext());
            }
            if (!mClickEable) {
                return;
            }
            mClickEable = false;
            SecretSupport ss = new SecretSupport();
            ss.setFromUser(UserManager.getInstance(getContext())
                    .getCurrentUser());
            ss.setToUser(mCurrentSecret.getUser());
            ss.setSecret(mCurrentSecret);
            ss.setSupport(v == mHappy);
            ss.save(getContext(), new SaveListener() {
                @Override
                public void onSuccess() {
                    showLog("save secretSupport success");
                    mClickEable = true;
                    startQuery();
                }

                @Override
                public void onFailure(int arg0, String arg1) {
                    mClickEable = true;
                    showLog("save secretSupport failure");
                }
            });
            if (v == mHappy) {
                mHappy.setSelected(true);
                mCry.setSelected(false);
            } else {
                mCry.setSelected(true);
                mHappy.setSelected(false);
            }
            mHappy.setClickable(false);
            mCry.setClickable(false);
        }
    }

    public void setSecret(Secret secret) {
        mCurrentSecret = secret;
        startQuery();
    }
}
