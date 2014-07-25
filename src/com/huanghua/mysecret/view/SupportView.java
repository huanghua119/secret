package com.huanghua.mysecret.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.ui.WriteCommentActivity;

/***
 * 笑脸哭脸点击View
 * 
 * @author huanghua
 * 
 */
public class SupportView extends LinearLayout implements View.OnClickListener {

    private Secret mCurrentSecret = null;
    private SecretSupport mCuSupport = null;
    private User mCurrentUser;
    private boolean mIsCheck = false;
    private boolean mClickEable = true;
    private boolean mIsComment = false;

    private TextView mHappy = null;
    private TextView mCry = null;
    private TextView mComment = null;

    public SupportView(Context context) {
        this(context, null);
    }

    public SupportView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHappy = (TextView) findViewById(R.id.item_support_happy);
        mCry = (TextView) findViewById(R.id.item_support_cry);
        mComment = (TextView) findViewById(R.id.item_commit);
        mCry.setOnClickListener(this);
        mHappy.setOnClickListener(this);
        mComment.setOnClickListener(this);
    }

    public void startQuery() {
        mIsCheck = false;
        mCuSupport = null;
        mHappy.setSelected(false);
        mCry.setSelected(false);
        BmobQuery<SecretSupport> ssQuery = new BmobQuery<SecretSupport>();
        ssQuery.addWhereEqualTo("secret", mCurrentSecret);
        ssQuery.findObjects(getContext(), new FindListener<SecretSupport>() {
            @Override
            public void onSuccess(List<SecretSupport> arg0) {
                showLog("query SecretSupport success: " + arg0);
                int happy = 0;
                int cry = 0;
                for (SecretSupport ss : arg0) {
                    if (getCurrentUser() == null) {
                        mHappy.setSelected(false);
                        mCry.setSelected(false);
                    } else {
                        checkClick(ss);
                    }
                    if (ss.isSupport()) {
                        happy++;
                    } else {
                        cry++;
                    }
                }
                mHappy.setText(happy + "");
                mCry.setText(cry + "");
                mClickEable = true;
            }

            @Override
            public void onError(int arg0, String arg1) {
                showLog("query SecretSupport error: " + arg1);
                mClickEable = true;
            }
        });

        BmobQuery<Comment> mQueryComent = new BmobQuery<Comment>();
        mQueryComent.addWhereEqualTo("secret", mCurrentSecret);
        mQueryComent.findObjects(getContext(), new FindListener<Comment>() {
            @Override
            public void onError(int arg0, String arg1) {
            }

            @Override
            public void onSuccess(List<Comment> arg0) {
                mComment.setText(arg0.size() + "");
            }
        });
    }

    private void checkClick(SecretSupport ss) {
        if (!mIsCheck && ss.getFromUser() != null
                && ss.getFromUser().equals(getCurrentUser())) {
            mCuSupport = ss;
            mIsCheck = true;
            if (ss.isSupport()) {
                mHappy.setSelected(true);
                mCry.setSelected(false);
            } else {
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
        if (((BaseActivity) getContext()).checkUserLogin()) {
            return;
        }
        if (v == mComment) {
            if (!mIsComment) {
                Intent intent = new Intent();
                intent.setClass(getContext(), WriteCommentActivity.class);
                intent.putExtra("secret", mCurrentSecret);
                getContext().startActivity(intent);
                ((Activity) getContext()).overridePendingTransition(
                        R.anim.right_in, R.anim.right_out);
            }
        } else {
            if (!mClickEable) {
                return;
            }
            if (mCuSupport != null
                    && mCuSupport.getFromUser().equals(getCurrentUser())) {
                if ((mCuSupport.isSupport() && v == mHappy)
                        || (!mCuSupport.isSupport() && v == mCry)) {
                    return;
                }
            }
            if (mCuSupport != null) {
                mCuSupport.setSupport(v == mHappy);
                mCuSupport.update(getContext(), new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save updateSupport success");
                        startQuery();
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        mClickEable = true;
                        showLog("save updateSupport failure");
                    }
                });
            } else {
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
                        startQuery();
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        mClickEable = true;
                        showLog("save secretSupport failure");
                    }
                });
            }
            mClickEable = false;
        }
    }

    public void setSecret(Secret secret) {
        mCurrentSecret = secret;
        startQuery();
    }

    public void setInComment(boolean in) {
        mIsComment = in;
    }

    private User getCurrentUser() {
        mCurrentUser = UserManager.getInstance(getContext()).getCurrentUser();
        return mCurrentUser;
    }
}
