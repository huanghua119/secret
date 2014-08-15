package com.huanghua.mysecret.ui;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.ImageView;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.manager.UserManager.UserManagerListener;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.huanghua.mysecret.util.ThemeUtil;
import com.huanghua.mysecret.weibologin.AccessTokenKeeper;
import com.huanghua.mysecret.weibologin.WeiboConstants;
import com.huanghua.mysecret.weibologin.WeiboLoginButton;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

public class UserLoginActivity extends BaseActivity implements OnClickListener {

    private EditText mUserName = null;
    private EditText mUserPass = null;
    private Button mLogin = null;
    private Button mLogout = null;
    private View mLoginView = null;
    private View mUserDetail = null;

    private User mCurrentUser = null;
    private WeiboLoginButton mWeiboLogin = null;
    private ImageView mQQLogin = null;
    /** 登陆认证对应的listener */
    private AuthListener mLoginListener = new AuthListener();
    /** 登出操作对应的listener */
    private LogOutRequestListener mLogoutListener = new LogOutRequestListener();
    private Oauth2AccessToken mAccessToken = null;

    public static final int DIALOG_NEW_REGISTER = 1;
    public static final int OTHEN_LOGIN_WEIBO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = ThemeUtil.getCurrentTheme(this);
        switch (theme) {
        case ThemeUtil.THEME_NIGHT:
            setContentView(R.layout.user_login_view_purple);
            break;
        case ThemeUtil.THEME_DURING:
            setContentView(R.layout.user_login_view);
            break;
        default:
            setContentView(R.layout.user_login_view_purple);
            break;
        }
        init();
        initWeiboLogin();
    }

    private void initWeiboLogin() {
        // 创建授权认证信息
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        AuthInfo authInfo = new AuthInfo(this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
        mWeiboLogin = (WeiboLoginButton) findViewById(R.id.weibo_login);
        mWeiboLogin.setWeiboAuthInfo(authInfo, mLoginListener);
        mWeiboLogin.setExternalOnClickListener(mButtonClickListener);
        mWeiboLogin.setVisibility(View.VISIBLE);
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
        mQQLogin = (ImageView) findViewById(R.id.qq_login);
        mQQLogin.setOnClickListener(this);
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
            if (mCurrentUser.getLogintype() != null && mCurrentUser.getLogintype() == User.LOGIN_TYPE_WEIBO) {
                userManager.weiboLogout(mLogoutListener, mAccessToken);
            } else {
                userManager.logout();
                onBackPressed();
            }
        } else if (v == mQQLogin) {
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
                SharePreferenceUtil mSp = CustomApplcation.getInstance().getSpUtil();
                Installation in = new Installation(UserLoginActivity.this);
                in.setUser(userManager.getCurrentUser());
                in.update(UserLoginActivity.this, mSp.getInstallationObjectId() , new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        showLog(DateQueryService.TAG, "login onSuccess");
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog(DateQueryService.TAG, "login onFailure arg1:" + arg1
                                + " arg0:" + arg0);
                    }
                });
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

    private Button mCurrentClickedButton;

    private OnClickListener mButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof Button) {
                mCurrentClickedButton = (Button) v;
            }
        }
    };


    /**
     * 登入按钮的监听器，接收授权结果。
     */
    @SuppressWarnings("deprecation")
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            showLog(WeiboConstants.TAG, "AuthListener onComplete:" + values);
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                showDialog(DIALOG_NEW_REGISTER);
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), accessToken);
                mAccessToken = accessToken;
                userManager.weiboLogin(mAccessToken, new UserManagerListener() {
                    @Override
                    public void onSuccess(User u) {
                        SharePreferenceUtil mSp = CustomApplcation.getInstance().getSpUtil();
                        Installation in = new Installation(UserLoginActivity.this);
                        in.setUser(userManager.getCurrentUser());
                        in.update(UserLoginActivity.this, mSp.getInstallationObjectId() , new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                showLog(WeiboConstants.TAG, "login onSuccess");
                            }

                            @Override
                            public void onFailure(int arg0, String arg1) {
                                showLog(WeiboConstants.TAG, "login onFailure arg1:" + arg1
                                        + " arg0:" + arg0);
                            }
                        });
                        removeDialog(DIALOG_NEW_REGISTER);
                        finish();
                    }
                    @Override
                    public void onError(int arg0, String arg1) {
                        CommonUtils.showLog(WeiboConstants.TAG,
                                "singup onError arg0:" + arg0 + " arg1:" + arg1);
                        String str = getString(R.string.other_login_fail);
                        ShowToast(str);
                        removeDialog(DIALOG_NEW_REGISTER);
                    }
                });
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            showLog(WeiboConstants.TAG, "AuthListener onWeiboException:" + e.getMessage());
        }

        @Override
        public void onCancel() {
            showLog(WeiboConstants.TAG, "AuthListener onCancel");
        }
    }
    /**
     * 登出按钮的监听器，接收登出处理结果。（API 请求结果的监听器）
     */
    private class LogOutRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            showLog(WeiboConstants.TAG, "LogOutRequestListener response:" + response);
            if (!TextUtils.isEmpty(response)) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");
                    if ("true".equalsIgnoreCase(value)) {
                        AccessTokenKeeper.clear(UserLoginActivity.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            userManager.logout();
            onBackPressed();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            showLog(WeiboConstants.TAG, "LogOutRequestListener onWeiboException:" + e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentClickedButton != null) {
            if (mCurrentClickedButton instanceof WeiboLoginButton) {
                ((WeiboLoginButton) mCurrentClickedButton).onActivityResult(
                        requestCode, resultCode, data);
            }
        }
    }

}
