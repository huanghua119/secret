package com.huanghua.mysecret.manager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import cn.bmob.v3.listener.OtherLoginListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;

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
    }

    public void weiboLogin(final UserManagerListener userListener, final int step) {
        User.weiboLogin(sContext, WEIBO_APPID, WEIBO_RESULT_URI, new OtherLoginListener() {
            @Override
            public void onSuccess(final JSONObject userAuth) {
                mCurrentUser = User.getCurrentUser(sContext, User.class);
                CommonUtils.showLog("weibo_login","第三方登陆成功:"+userAuth);
                CommonUtils.showLog("weibo_login","mCurrentUser:"+mCurrentUser);
                if (mCurrentUser != null && step == 2) {
                    try {
                        JSONObject weibo = userAuth.getJSONObject("weibo");
                        mAccessToken = weibo.getString("access_token");
                        uid = weibo.getLong("uid");
                        show(uid, new RequestListener() {
                            @Override
                            public void onWeiboException(WeiboException arg0) {
                                CommonUtils.showLog("weibo_login",
                                        "onWeiboException" + arg0);
                                userListener.onSuccess(mCurrentUser);
                            }

                            @Override
                            public void onComplete(String arg0) {
                                CommonUtils.showLog("weibo_login",
                                        "onComplete arg0:" + arg0);

                                try {
                                    JSONObject jsonObject = new JSONObject(
                                            arg0);
                                    String screen_name = jsonObject
                                            .optString("screen_name", "");
                                    String gender = jsonObject.optString("gender", "");
                                    String avatar_large = jsonObject.optString("avatar_large", "");
                                    mCurrentUser.setUsername(screen_name);
                                    mCurrentUser.setSex("m".equals(gender));
                                    mCurrentUser.setAvatar(avatar_large);
                                    mCurrentUser.update(sContext);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                userListener.onSuccess(mCurrentUser);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    weiboLogin(userListener, 2);
                }
            }

            @Override
            public void onFailure(int code, String msg) {
                CommonUtils.showLog("weibo_login","第三方登陆失败："+msg);
                userListener.onError(code,msg);
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

    /** 访问微博服务接口的地址 */
    private static final String API_SERVER = "https://api.weibo.com/2";
    /** GET 请求方式 */
    private static final String HTTPMETHOD_GET = "GET";
    /** POST 请求方式 */
    private static final String HTTPMETHOD_POST = "POST";
    /** HTTP 参数 */
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String API_BASE_URL = API_SERVER + "/users/show.json";
    private static final String WEIBO_APPID = "2935574131";
    private static final String WEIBO_RESULT_URI = "https://api.weibo.com/oauth2/default.html";
    /** 注销地址（URL） */
    private static final String REVOKE_OAUTH_URL = "https://api.weibo.com/oauth2/revokeoauth2";
    /** 当前的 Token */
    private String mAccessToken;
    private long uid;

    /**
     * 根据用户ID获取用户信息。
     *
     * @param uid
     *            需要查询的用户ID
     * @param listener
     *            异步请求回调接口
     */
    public void show(long uid, RequestListener listener) {
        WeiboParameters params = new WeiboParameters();
        params.put("uid", uid);
        requestAsync(API_BASE_URL, params, HTTPMETHOD_GET, listener);
    }

    /**
     * 异步取消用户的授权。
     *
     * @param listener
     *            异步请求回调接口
     */
    public void weiboLogout(RequestListener listener) {
        requestAsync(REVOKE_OAUTH_URL, new WeiboParameters(), HTTPMETHOD_POST,
                listener);
    }
    private void requestAsync(String url, WeiboParameters params,
            String httpMethod, RequestListener listener) {
        if (null == mAccessToken || TextUtils.isEmpty(url) || null == params
                || TextUtils.isEmpty(httpMethod) || null == listener) {
            return;
        }
        params.put(KEY_ACCESS_TOKEN, mAccessToken);
        AsyncWeiboRunner.requestAsync(url, params, httpMethod, listener);
    }
}
