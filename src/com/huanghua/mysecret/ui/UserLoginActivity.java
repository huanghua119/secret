package com.huanghua.mysecret.ui;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.EmailVerifyListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.manager.UserManager.UserManagerListener;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.tencentlogin.TencentConstants;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.huanghua.mysecret.util.ThemeUtil;
import com.huanghua.mysecret.weibologin.AccessTokenKeeper;
import com.huanghua.mysecret.weibologin.WeiboConstants;
import com.huanghua.mysecret.weibologin.WeiboLoginButton;
import com.nostra13.universalimageloader.core.ImageLoader;
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

    private View mUpdateHead = null;
    private View mUpdateName = null;
    private View mUpdateEmail = null;
    private View mUpdatePass = null;
    private View mUpdateSex = null;
    private TextView mUserName2 = null;
    private ImageView mUserPhoto = null;
    private TextView mUserSex = null;
    private TextView mLoginType = null;
    private TextView mEmail = null;
    private int mWhichSex = 0;

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

    @SuppressWarnings("deprecation")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                showDialog(DIALOG_NEW_REGISTER);
                break;
            }
            super.handleMessage(msg);
        }
    };
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
        initUpdate();
    }

    private void initWeiboLogin() {
        // 创建授权认证信息
        mCurrentUser = userManager.getCurrentUser();
        if (mCurrentUser != null && mCurrentUser.getLogintype() == User.LOGIN_TYPE_WEIBO ) {
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
        }
        AuthInfo authInfo = new AuthInfo(this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
        mWeiboLogin = (WeiboLoginButton) findViewById(R.id.weibo_login);
        mWeiboLogin.setWeiboAuthInfo(authInfo, mLoginListener);
        mWeiboLogin.setExternalOnClickListener(mButtonClickListener);
        mWeiboLogin.setVisibility(View.VISIBLE);
        mQQLogin = (ImageView) findViewById(R.id.qq_login);
        mQQLogin.setOnClickListener(this);
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

    private void initUpdate() {
        mUpdateHead = findViewById(R.id.update_head);
        mUpdateName = findViewById(R.id.update_name);
        mUpdateEmail = findViewById(R.id.update_email);
        mUpdatePass = findViewById(R.id.update_password);
        mUpdateSex = findViewById(R.id.update_sex);
        mUpdateHead.setOnClickListener(this);
        mUpdateName.setOnClickListener(this);
        mUpdateEmail.setOnClickListener(this);
        mUpdatePass.setOnClickListener(this);
        mUpdateSex.setOnClickListener(this);
        mUserName2 = (TextView) findViewById(R.id.user_name2);
        mUserPhoto = (ImageView) findViewById(R.id.user_photo);
        mUserSex = (TextView) findViewById(R.id.user_sex);
        mLoginType = (TextView) findViewById(R.id.login_type);
        mEmail = (TextView) findViewById(R.id.email);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUser = userManager.getCurrentUser();
        if (mCurrentUser != null && mCurrentUser.getObjectId() != null) {
            mLoginView.setVisibility(View.GONE);
            mUserDetail.setVisibility(View.VISIBLE);
            String avatar = mCurrentUser.getAvatar();
            if (avatar != null && !avatar.equals("")) {
                ImageLoader.getInstance().displayImage(avatar, mUserPhoto,
                        ImageLoadOptions.getOptions());
            } else {
                mUserPhoto.setImageResource(R.drawable.user_photo_default);
            }
            mUserName2.setText(mCurrentUser.getUsername());
            mUserSex.setText(mCurrentUser.isSex() ? R.string.sex_man : R.string.sex_woman);
            int loginType = mCurrentUser.getLogintype() != null ? mCurrentUser.getLogintype() : 0;
            String type = getString(R.string.baimi);
            mUpdateEmail.setVisibility(View.GONE);
            mUpdatePass.setVisibility(View.GONE);
            if (loginType == User.LOGIN_TYPE_TENCENT_QQ) {
                type = getString(R.string.qq);
            } else if (loginType == User.LOGIN_TYPE_WEIBO) {
                type = getString(R.string.weibo);
            } else {
                mUpdateEmail.setVisibility(View.VISIBLE);
                mUpdatePass.setVisibility(View.VISIBLE);
            }
            mLoginType.setText(type);
            BmobQuery<User> query = new BmobQuery<User>();
            query.addWhereEndsWith("objectId", mCurrentUser.getObjectId());
            query.findObjects(this, new FindListener<User>() {
                @Override
                public void onSuccess(List<User> arg0) {
                    if (arg0.size() < 0) {
                        return;
                    }
                    User u = arg0.get(0);
                    String email = u.getEmail();
                    if (email != null && !"".equals(email)) {
                        if (u.getEmailVerified()) {
                            mEmail.setText(email);
                        } else {
                            mEmail.setText(email + " ("
                                    + getString(R.string.no_verified) + ")");
                        }
                    } else {
                        mEmail.setText(R.string.no_settings);
                    }
                    mCurrentUser.setEmailVerified(u.getEmailVerified());
                }

                public void onError(int arg0, String arg1) {
                }
            });
            String email = mCurrentUser.getEmail();
            if (email != null && !"".equals(email)) {
                if (mCurrentUser.getEmailVerified()) {
                    mEmail.setText(email);
                } else {
                    mEmail.setText(email + " (" + getString(R.string.no_verified) + ")");
                }
            } else {
                mEmail.setText(R.string.no_settings);
            }
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
            } else if (mCurrentUser.getLogintype() != null && mCurrentUser.getLogintype() == User.LOGIN_TYPE_TENCENT_QQ) {
                userManager.tencentLogout(this, null);
                userManager.logout();
                onBackPressed();
            } else {
                userManager.logout();
                onBackPressed();
            }
        } else if (v == mQQLogin) {
            tencentLogin();
        } else if (v == mUpdateHead) {
        } else if (v == mUpdateName) {
        } else if (v == mUpdatePass) {
            showUpdatePassDialog();
        } else if (v == mUpdateEmail) {
            showUpdateEmailDialog();
        } else if (v == mUpdateSex) {
            showUpdateSexDialog();
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

    @SuppressWarnings("deprecation")
    private void tencentLogin() {
        userManager.tencentLogin(this, new UserManagerListener() {
            @Override
            public void onSuccess(User u) {
                SharePreferenceUtil mSp = CustomApplcation.getInstance().getSpUtil();
                Installation in = new Installation(UserLoginActivity.this);
                in.setUser(userManager.getCurrentUser());
                in.update(UserLoginActivity.this, mSp.getInstallationObjectId() , new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        showLog(TencentConstants.TAG, "login onSuccess");
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog(TencentConstants.TAG, "login onFailure arg1:" + arg1
                                + " arg0:" + arg0);
                    }
                });
                removeDialog(DIALOG_NEW_REGISTER);
                finish();
            }
            
            @Override
            public void onError(int arg0, String arg1) {
                showLog(TencentConstants.TAG, "user login failure arg0:" + arg0 + " arg1:"+ arg0);
                String str = getString(R.string.no_conn_network);
                if (arg0 == 101 || arg0 == 0) {
                    str = getString(R.string.other_login_fail);
                }
                ShowToast(str);
            }
        }, mHandler);
    }

    private void showUpdateSexDialog() {
        CharSequence[] items = { getString(R.string.sex_man),
                getString(R.string.sex_woman) };
        boolean isMan = mCurrentUser.isSex();
        mWhichSex = isMan ? 0 : 1;
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.sex)
                .setSingleChoiceItems(items, isMan ? 0 : 1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                mWhichSex = which;
                            }
                        })
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                boolean isMan = mCurrentUser.isSex();
                                int tempWhichSex = isMan ? 0 : 1;
                                if (tempWhichSex == mWhichSex) {
                                    return;
                                }
                                mCurrentUser.setSex(mWhichSex == 0);
                                mCurrentUser.update(UserLoginActivity.this,
                                        new UpdateListener() {
                                            @Override
                                            public void onSuccess() {
                                                mUserSex.setText(mCurrentUser
                                                        .isSex() ? R.string.sex_man
                                                        : R.string.sex_woman);
                                                ShowToast(R.string.update_success, R.drawable.tenpay_toast_logo_success);
                                            }

                                            @Override
                                            public void onFailure(int arg0,
                                                    String arg1) {
                                                ShowToast(R.string.update_fail);
                                            }
                                        });
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void showUpdateEmailDialog() {
        Boolean isVerified = mCurrentUser.getEmailVerified();
        if (isVerified == null || isVerified) {
            Intent intent = new Intent();
            intent.putExtra("update_type", UpdateUserInfoActivity.UPDATE_TYPE_USER_EMAIL);
            intent.setClass(this, UpdateUserInfoActivity.class);
            startAnimActivity(intent);
        } else if (!isVerified) {
            Dialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.update_email)
                    .setMessage(R.string.send_verified_email)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    User.requestEmailVerify(UserLoginActivity.this, mCurrentUser.getEmail(), new EmailVerifyListener() {
                                        @Override
                                        public void onSuccess() {
                                            ShowToast(R.string.send_verified_email_success,  R.drawable.tenpay_toast_logo_success);
                                        }
                                        @Override
                                        public void onFailure(int arg0, String arg1) {
                                            ShowToast(R.string.send_verified_email_fail);
                                        }
                                    });
                                }
                            }).create();
            dialog.show();
        }
    }

    private void showUpdatePassDialog() {
        View view = getLayoutInflater()
                .inflate(R.layout.update_pass_view, null);
        final EditText old_edit = (EditText) view.findViewById(R.id.old_pass);
        final EditText new_edit = (EditText) view.findViewById(R.id.new_pass);
        Dialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.update_password2)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                String old_pass = old_edit.getText().toString();
                                String new_pass = new_edit.getText().toString();
                                if (TextUtils.isEmpty(old_pass)) {
                                    ShowToast(R.string.not_old_pass);
                                    return;
                                }
                                String old_pass2 = User.getCurrentUser(UserLoginActivity.this).getPassword();
                                showLog("update_pass", "old_pass2: " + old_pass2);
                                if (!old_pass.equals(old_pass2)) {
                                    ShowToast(R.string.correct_old_pass);
                                    return;
                                }
                                if (TextUtils.isEmpty(new_pass)) {
                                    ShowToast(R.string.not_new_pass);
                                    return;
                                }
                                if (old_pass2.equals(new_pass)) {
                                    return;
                                }
                                mCurrentUser.setPassword(new_pass);
                                mCurrentUser.update(UserLoginActivity.this, new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                    }
                                    @Override
                                    public void onFailure(int arg0, String arg1) {
                                    }
                                });
                            }
                        }).create();
        dialog.show();
    }
}
