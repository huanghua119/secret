package com.huanghua.mysecret.manager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.config.Config;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.tencentlogin.TencentConstants;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.huanghua.mysecret.weibologin.WeiboConstants;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class UserManager {

    private static UserManager mUserManager;
    private static Context sContext;
    private User mCurrentUser;

    public interface UserManagerListener {
        public void onError(int arg0, String arg1);

        public void onSuccess(User u);
    }

    private UserManager() {
    }

    public static UserManager getInstance(Context context) {
        if (mUserManager == null) {
            synchronized (UserManager.class) {
                if (mUserManager == null) {
                    mUserManager = new UserManager();
                }
            }
        }
        sContext = context;
        return mUserManager;
    }

    public void initCurrentUser() {
        mCurrentUser = User.getCurrentUser(sContext, User.class);
        if (mCurrentUser != null
                && mCurrentUser.getLogintype() == User.LOGIN_TYPE_TENCENT_QQ) {
            mTencent = Tencent
                    .createInstance(TencentConstants.APP_ID, sContext);
        }
    }

    public void weiboLogin(final Oauth2AccessToken mAccessToken, final UserManagerListener userListener) {
        mCurrentUser = new User();
        mCurrentUser.setUsername(WeiboConstants.USER_NAME_HEAD + mAccessToken.getUid());
        mCurrentUser.setPassword(Config.applicationId);
        mCurrentUser.login(sContext, new SaveListener() {
            @Override
            public void onSuccess() {
                mCurrentUser = User.getCurrentUser(sContext, User.class);
                if (userListener != null) {
                    userListener.onSuccess(mCurrentUser);
                }
            }
            @Override
            public void onFailure(int arg0, String arg1) {
                if (arg0 == 101) {
                    show(Long.parseLong(mAccessToken.getUid()), new RequestListener() {
                        @Override
                        public void onWeiboException(WeiboException arg0) {
                            CommonUtils.showLog(WeiboConstants.TAG, "singup onWeiboException:" + arg0.getMessage());
                        }
                        @Override
                        public void onComplete(String arg0) {
                            try {
                                JSONObject jsonObject = new JSONObject(
                                        arg0);
                                String screen_name = jsonObject
                                        .optString("screen_name", "");
                                String gender = jsonObject.optString("gender", "");
                                String avatar_large = jsonObject.optString("avatar_large", "");
                                User u = new User();
                                u.setUsername(WeiboConstants.USER_NAME_HEAD + mAccessToken.getUid());
                                u.setPassword(Config.applicationId);
                                u.setSex("m".equals(gender));
                                u.setAvatar(avatar_large);
                                u.setOthername(screen_name);
                                u.setLogintype(User.LOGIN_TYPE_WEIBO);
                                u.signUp(sContext, new SaveListener() {
                                    @Override
                                    public void onSuccess() {
                                        if (userListener != null) {
                                            userListener.onSuccess(mCurrentUser);
                                        }
                                    }
                                    @Override
                                    public void onFailure(int arg0, String arg1) {
                                        mCurrentUser = null;
                                        if (userListener != null) {
                                            userListener.onError(arg0, arg1);
                                        }
                                    }
                                });
                                mCurrentUser = u;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mCurrentUser = null;
                                if (userListener != null) {
                                    userListener.onError(0, "");
                                }
                            }
                        }
                    }, mAccessToken);
                } else {
                    mCurrentUser = null;
                    if (userListener != null) {
                        userListener.onError(0, "");
                    }
                }
            }
        });
    }

    public void login(String userName, String passWord,
            final UserManagerListener userListener) {
        mCurrentUser = new User();
        mCurrentUser.setUsername(userName);
        mCurrentUser.setPassword(passWord);
        mCurrentUser.login(sContext, new SaveListener() {
            @Override
            public void onSuccess() {
                mCurrentUser = User.getCurrentUser(sContext, User.class);
                if (userListener != null) {
                    userListener.onSuccess(mCurrentUser);
                }
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                mCurrentUser = null;
                if (userListener != null) {
                    userListener.onError(arg0, arg1);
                }
            }
        });
    }

    public void logout() {
        User.logOut(sContext);
        mCurrentUser = null;
        SharePreferenceUtil mSp = CustomApplcation.getInstance().getSpUtil();
        Installation in = new Installation(sContext);
        in.setUser(new User());
        in.update(sContext, mSp.getInstallationObjectId(),
                new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        CommonUtils.showLog(DateQueryService.TAG, "logout onSuccess");
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        CommonUtils.showLog(DateQueryService.TAG, "logout onFailure arg0:" + arg0 + " arg1:" + arg1);
                    }
                });
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    public void signUp(User u, final UserManagerListener userListener) {
        u.signUp(sContext, new SaveListener() {
            @Override
            public void onSuccess() {
                if (userListener != null) {
                    userListener.onSuccess(mCurrentUser);
                }
            }

            @Override
            public void onFailure(int arg0, String arg1) {
                if (userListener != null) {
                    userListener.onError(arg0, arg1);
                }
                mCurrentUser = null;
            }
        });
        mCurrentUser = u;
    }

    public void setCreentUser(User u) {
        this.mCurrentUser = u;
    }

    public void showLog(String msg) {
        if (CustomApplcation.DEBUG) {
            Log.i(CustomApplcation.TAG, msg);
        }
    }

    /**
     * 异步取消用户的授权。
     *
     * @param listener
     *            异步请求回调接口
     */
    public void weiboLogout(RequestListener listener,
            Oauth2AccessToken accessToken) {
        requestAsync(WeiboConstants.REVOKE_OAUTH_URL, new WeiboParameters(),
                WeiboConstants.HTTPMETHOD_POST, listener, accessToken);
    }

    /**
     * 根据用户ID获取用户信息。
     *
     * @param uid
     *            需要查询的用户ID
     * @param listener
     *            异步请求回调接口
     */
    public void show(long uid, RequestListener listener,
            Oauth2AccessToken accessToken) {
        WeiboParameters params = new WeiboParameters();
        params.put("uid", uid);
        requestAsync(WeiboConstants.API_BASE_URL, params,
                WeiboConstants.HTTPMETHOD_GET, listener, accessToken);
    }

    private void requestAsync(String url, WeiboParameters params,
            String httpMethod, RequestListener listener,
            Oauth2AccessToken accessToken) {
        if (null == accessToken || TextUtils.isEmpty(url) || null == params
                || TextUtils.isEmpty(httpMethod) || null == listener) {
            return;
        }
        params.put(WeiboConstants.KEY_ACCESS_TOKEN, accessToken.getToken());
        AsyncWeiboRunner.requestAsync(url, params, httpMethod, listener);
    }

    public static Tencent mTencent;
    private UserInfo mInfo;

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            CommonUtils.showLog(TencentConstants.TAG, "登录成功:" +  response.toString());
            doComplete((JSONObject)response);
        }
        protected void doComplete(JSONObject values) {
        }
        @Override
        public void onError(UiError e) {
            CommonUtils.showLog(TencentConstants.TAG, "onError: " + e.errorDetail);
        }

        @Override
        public void onCancel() {
            CommonUtils.showLog(TencentConstants.TAG, "onCancel: ");
        }
    }
    public void tencentLogin(Activity activity, final UserManagerListener userListener, final Handler handler) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(TencentConstants.APP_ID, activity);
        }
        IUiListener listener = new BaseUiListener() {
            @Override
            protected void doComplete(JSONObject values) {
                CommonUtils.showLog(TencentConstants.TAG,
                        "tencentLogin doComplete values:" + values);
                try {
                    handler.sendEmptyMessage(1);
                    final String user_name = values.getString("access_token");
                    mCurrentUser = new User();
                    mCurrentUser.setUsername(TencentConstants.USER_NAME_HEAD + user_name);
                    mCurrentUser.setPassword(Config.applicationId);
                    mCurrentUser.login(sContext, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            mCurrentUser = User.getCurrentUser(sContext,
                                    User.class);
                            if (userListener != null) {
                                userListener.onSuccess(mCurrentUser);
                            }
                        }

                        @Override
                        public void onFailure(int arg0, String arg1) {
                            CommonUtils.showLog(TencentConstants.TAG,
                                    "tencentLogin onFailure:" + arg0 + " arg1:"
                                            + arg1);
                            if (arg0 == 101) {
                                updateUserInfo(userListener,
                                        TencentConstants.USER_NAME_HEAD
                                                + user_name);
                            } else {
                                mCurrentUser = null;
                                if (userListener != null) {
                                    userListener.onError(0, "");
                                }
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        mTencent.login(activity, "all", listener);
    }

    public void tencentLogout(Activity activity,
            final UserManagerListener userListener) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(TencentConstants.APP_ID, activity);
        }
        mTencent.logout(activity);
    }

    private void updateUserInfo(final UserManagerListener userListener, final String username) {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {
                @Override
                public void onError(UiError e) {
                    CommonUtils.showLog(TencentConstants.TAG, "updateUserInfo onError:" + e.toString());
                    if (userListener != null) {
                        userListener.onError(0, "");
                    }
                }
                @Override
                public void onComplete(final Object response) {
                    final JSONObject json = (JSONObject) response;
                    CommonUtils.showLog(TencentConstants.TAG, "updateUserInfo onComplete:" + json);
                    String gender = json.optString("gender", "男");
                    String avatar_large = json.optString("figureurl_qq_2", "");
                    String othnername = json.optString("nickname");
                    User u = new User();
                    u.setUsername(username);
                    u.setPassword(Config.applicationId);
                    u.setSex("男".equals(gender));
                    u.setAvatar(avatar_large);
                    u.setOthername(othnername);
                    u.setLogintype(User.LOGIN_TYPE_TENCENT_QQ);
                    u.signUp(sContext, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            if (userListener != null) {
                                userListener.onSuccess(mCurrentUser);
                            }
                        }
                        @Override
                        public void onFailure(int arg0, String arg1) {
                            if (userListener != null) {
                                userListener.onError(arg0, arg1);
                            }
                            mCurrentUser = null;
                        }
                    });
                    mCurrentUser = u;
                }
                @Override
                public void onCancel() {
                    CommonUtils.showLog(TencentConstants.TAG, "updateUserInfo onCancel");
                    userListener.onError(0, "");
                }
            };
            mInfo = new UserInfo(sContext, mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        } else {
            CommonUtils.showLog(TencentConstants.TAG, "updateUserInfo fail");
            userListener.onError(0, "");
        }
    }
}
