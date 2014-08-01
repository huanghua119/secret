package com.huanghua.mysecret.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.manager.UserManager.UserManagerListener;

public class UserLoginActivity extends BaseActivity implements OnClickListener {

    private EditText mUserName = null;
    private EditText mUserPass = null;
    private Button mLogin = null;
    private Button mLogout = null;
    private View mLoginView = null;
    private View mUserDetail = null;

    private User mCurrentUser = null;

    public static final int DIALOG_NEW_REGISTER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.user_login_view);
        setContentView(R.layout.user_login_view_purple);
        init();
    }

    private void init() {
        mUserName = (EditText) findViewById(R.id.user_name);
        mUserPass = (EditText) findViewById(R.id.user_pass);
        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(this);
        mLogout = (Button) findViewById(R.id.user_logout);
        mLogout.setOnClickListener(this);
        mLoginView = findViewById(R.id.login_view);
        mUserDetail = findViewById(R.id.user_detail_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUser = userManager.getCurrentUser();
        if (mCurrentUser != null) {
            mLoginView.setVisibility(View.GONE);
            mUserDetail.setVisibility(View.VISIBLE);
        } else {
            mLoginView.setVisibility(View.VISIBLE);
            mUserDetail.setVisibility(View.GONE);
            mUserName.setText(getUserName());
        }
    }

    public void onRegister(View view) {
        Intent intent = new Intent();
        intent.setClass(this, UserRegisterActivity.class);
        startAnimActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v == mLogin) {
            login();
        } else if (v == mLogout) {
            userManager.logout();
            onBackPressed();
        }
    }

    @SuppressWarnings("deprecation")
    private void login() {
        String name = mUserName.getText().toString();
        String password = mUserPass.getText().toString();

        if (TextUtils.isEmpty(name)) {
            ShowToast(R.string.notid);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            ShowToast(R.string.notpass);
            return;
        }
        setUserName(name);
        userManager.login(name, password, new UserManagerListener() {
            @Override
            public void onSuccess(User u) {
                removeDialog(DIALOG_NEW_REGISTER);
                finish();
            }

            @Override
            public void onError(int arg0, String arg1) {
                showLog("user login failure:" + arg0);
                String str = getString(R.string.no_conn_network);
                if (arg0 == 101) {
                    str = getString(R.string.login_fail);
                }
                removeDialog(DIALOG_NEW_REGISTER);
                ShowToast(str);
            }
        });

        showDialog(DIALOG_NEW_REGISTER);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DIALOG_NEW_REGISTER:
            dialog = CommonUtils.createLoadingDialog(this,
                    getString(R.string.login_now));
            break;
        }
        return dialog;
    }

    private String getUserName() {
        SharedPreferences mSharedPreferences = getSharedPreferences("mysecret",
                Context.MODE_PRIVATE);
        String name = mSharedPreferences.getString("user_name", "");
        return name;
    }

    private void setUserName(String username) {
        SharedPreferences mSharedPreferences = getSharedPreferences("mysecret",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("user_name", username);
        editor.commit();
    }
}
