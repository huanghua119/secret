package com.huanghua.mysecret.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.load.DateLoad;
import com.huanghua.mysecret.load.DateLoad.OnDateLoadCompleteListener;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.ui.WriteCommentActivity;
import com.huanghua.mysecret.util.CommonUtils;

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

    private List<SecretSupport> mAllSecretSupport = null;
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
        mAllSecretSupport = new ArrayList<SecretSupport>();
    }

    public void startQuery() {
        BmobQuery<SecretSupport> ssQuery = new BmobQuery<SecretSupport>();
        ssQuery.addWhereEqualTo("secret", mCurrentSecret);
        ssQuery.setLimit(1000);
        ssQuery.findObjects(getContext(), new FindListener<SecretSupport>() {
            @Override
            public void onSuccess(List<SecretSupport> arg0) {
                CommonUtils.showLog("query SecretSupport success: " + arg0);
                setSecretSupportList(arg0);
                refresh();
                DateLoad.update(mCurrentSecret.getObjectId(), arg0);
            }

            @Override
            public void onError(int arg0, String arg1) {
                CommonUtils.showLog("query SecretSupport error: " + arg1);
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
                DateLoad.updateComment(mCurrentSecret.getObjectId(), arg0.size());
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
                        CommonUtils.showLog("save updateSupport success");
                        if (mIsComment) {
                            getContext().sendBroadcast(new Intent(DATE_COMMENT_CHANGER));
                        } else {
                            startQuery();
                        }
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        mClickEable = true;
                        CommonUtils.showLog("save updateSupport failure");
                    }
                });
            } else {
                SecretSupport ss = new SecretSupport();
                ss.setFromUser(UserManager.getInstance(getContext())
                        .getCurrentUser());
                ss.setSecret(mCurrentSecret);
                ss.setSupport(v == mHappy);
                ss.save(getContext(), new SaveListener() {
                    @Override
                    public void onSuccess() {
                        CommonUtils.showLog("save secretSupport success");
                        mCurrentSecret.increment("commentCount");
                        mCurrentSecret.update(getContext());
                        if (mIsComment) {
                            getContext().sendBroadcast(new Intent(DATE_COMMENT_CHANGER));
                        } else {
                            startQuery();
                        }
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        mClickEable = true;
                        CommonUtils.showLog("save secretSupport failure");
                    }
                });
            }
            mClickEable = false;
        }
    }

    public void setSecret(Secret secret,OnDateLoadCompleteListener listener, Handler handler, int position) {
        mCurrentSecret = secret;
        mIsCheck = false;
        mCuSupport = null;
        mHappy.setSelected(false);
        mCry.setSelected(false);
        mHappy.setText("0");
        mCry.setText("0");
        mClickEable = true;
        DateLoad.loadDate(getContext(), listener, handler, secret, position);
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

    @Override
    protected void onAttachedToWindow() {
        if (mDateChangerReciver == null) {
            mDateChangerReciver = new DateChangerReciver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(DATE_COMMENT_CHANGER);
            getContext().registerReceiver(mDateChangerReciver, filter);
        }
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mDateChangerReciver);
        super.onDetachedFromWindow();
    }

    public static final String DATE_COMMENT_CHANGER = "date_comment_changer";
    private DateChangerReciver mDateChangerReciver = null;

    private class DateChangerReciver extends BroadcastReceiver  {
        @Override
        public void onReceive(Context context, Intent intent) {
            startQuery();
        }
    }

    public void setSecretSupportList(List<SecretSupport> list) {
        mAllSecretSupport = list;
    }

    public void refresh() {
        mIsCheck = false;
        mCuSupport = null;
        int happy = 0;
        int cry = 0;
        mHappy.setSelected(false);
        mCry.setSelected(false);
        if (mAllSecretSupport != null && mAllSecretSupport.size() != 0) {
            for (SecretSupport ss : mAllSecretSupport) {
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
        }
        mHappy.setText(happy + "");
        mCry.setText(cry + "");
        mClickEable = true;
    }

    public void refreshInCache(Secret secret, List<SecretSupport> list) {
        mAllSecretSupport = list;
        mCurrentSecret = secret;
        refresh();
    }

    public void setCommentCount(int count) {
        if (mComment != null) {
            mComment.setText(count + "");
        }
    }
}
