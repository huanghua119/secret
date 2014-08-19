package com.huanghua.mysecret.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.CommonUtils;

/**
 * UpdateUserInfo
 * 
 * @author huanghua
 * 
 */
public class UpdateUserInfoActivity extends BaseActivity implements
        OnClickListener {

    public static final int UPDATE_TYPE_USER_EMAIL = 1;
    private int mUpdateType = 0;

    private TextView mSave = null;
    private TextView mTitle = null;
    private View mUpdateEmail = null;
    private TextView mEmailAlert = null;
    private EditText mEamil = null;
    private User mCurrentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user_info_view);
        mUpdateType = getIntent().getIntExtra("update_type",
                UPDATE_TYPE_USER_EMAIL);
        init();
    }

    private void init() {
        mTitle = (TextView) findViewById(R.id.title);
        mSave = (TextView) findViewById(R.id.btn_ok);
        mSave.setOnClickListener(this);
        mUpdateEmail = findViewById(R.id.update_email);
        mEmailAlert = (TextView) findViewById(R.id.emial_alert);
        mEamil = (EditText) findViewById(R.id.email);
        mCurrentUser = userManager.getCurrentUser();
        if (mCurrentUser == null || mCurrentUser.getObjectId() == null) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUpdateType == UPDATE_TYPE_USER_EMAIL) {
            mUpdateEmail.setVisibility(View.VISIBLE);
            mTitle.setText(R.string.update_email2);
            mEamil.requestFocus();
            Boolean isVerified = mCurrentUser.getEmailVerified();
            if (isVerified == null) {
                mEmailAlert.setText(R.string.alert_email2);
            } else if (isVerified) {
                mEmailAlert.setText(R.string.alert_email);
            } else {
                mEmailAlert.setText(R.string.alert_email3);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSave) {
            String email = mEamil.getText().toString();
            String oldEmail = mCurrentUser.getEmail();
            if (TextUtils.isEmpty(email)) {
                return;
            }
            if (email.equals(oldEmail)) {
                ShowToast(R.string.tease_me);
                return;
            }
            mCurrentUser.setEmail(email);
            mCurrentUser.update(this, new UpdateListener() {
                @Override
                public void onSuccess() {
                    ShowToast(R.string.send_verified_email_success,
                            R.drawable.tenpay_toast_logo_success);
                    finish();
                }

                @Override
                public void onFailure(int arg0, String arg1) {
                    CommonUtils.showLog("update_user", "arg0:" + arg0 + " arg1"
                            + arg0);
                    String src = getString(R.string.send_verified_email_fail);
                    if (arg0 == 301) {
                        src = getString(R.string.input_valid_email);
                    } else if (arg0 == 203) {
                        src = getString(R.string.email_exist);
                    }
                    ShowToast(src);
                }
            });
        }
    }

}
