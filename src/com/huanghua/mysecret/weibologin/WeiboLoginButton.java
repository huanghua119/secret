package com.huanghua.mysecret.weibologin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.utils.LogUtil;

public class WeiboLoginButton extends Button implements OnClickListener {
    private static final String TAG = WeiboConstants.TAG;

    /** 微博授权时，启动 SSO 界面的 Activity */
    private Context mContext;
    /** 授权认证所需要的信息 */
    private AuthInfo mAuthInfo;
    /** SSO 授权认证实例 */
    private SsoHandler mSsoHandler;
    /** 微博授权认证回调 */
    private WeiboAuthListener mAuthListener;
    /** 点击 Button 时，额外的 Listener */
    private OnClickListener mExternalOnClickListener;

    /**
     * 创建一个登录按钮。
     * 
     * @see View#View(Context)
     */
    public WeiboLoginButton(Context context) {
        this(context, null);
    }

    /**
     * 从 XML 配置文件中创建一个按钮。
     * 
     * @see View#View(Context, AttributeSet)
     */
    public WeiboLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 从 XML 配置文件以及样式中创建一个按钮。
     * 
     * @see View#View(Context, AttributeSet, int)
     */
    public WeiboLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    /**
     * 设置微博授权所需信息以及回调函数。
     * 
     * @param authInfo
     *            用于保存授权认证所需要的信息
     * @param authListener
     *            微博授权认证回调接口
     */
    public void setWeiboAuthInfo(AuthInfo authInfo,
            WeiboAuthListener authListener) {
        mAuthInfo = authInfo;
        mAuthListener = authListener;
    }

    /**
     * 设置微博授权所需信息。
     * 
     * @param appKey
     *            第三方应用的 APP_KEY
     * @param redirectUrl
     *            第三方应用的回调页
     * @param scope
     *            第三方应用申请的权限
     * @param authListener
     *            微博授权认证回调接口
     */
    public void setWeiboAuthInfo(String appKey, String redirectUrl,
            String scope, WeiboAuthListener authListener) {
        mAuthInfo = new AuthInfo(mContext, appKey, redirectUrl, scope);
        mAuthListener = authListener;
    }

    /**
     * 设置一个额外的 Button 点击时的 Listener。 当触发 Button 点击事件时，会先调用该
     * Listener，给使用者一个可访问的机会， 然后再调用内部默认的处理。
     * <p>
     * <b>注意：一般情况下，使用者不需要调用该方法，除非有其它必要性。<b>
     * </p>
     * 
     * @param listener
     *            Button 点击时的 Listener
     */
    public void setExternalOnClickListener(OnClickListener listener) {
        mExternalOnClickListener = listener;
    }

    /**
     * 按钮被点击时，调用该函数。
     */
    @Override
    public void onClick(View v) {
        // Give a chance to external listener
        if (mExternalOnClickListener != null) {
            mExternalOnClickListener.onClick(v);
        }

        if (null == mSsoHandler && mAuthInfo != null) {
            WeiboAuth weiboAuth = new WeiboAuth(mContext, mAuthInfo);
            mSsoHandler = new SsoHandler((Activity) mContext, weiboAuth);
        }

        if (mSsoHandler != null) {
            mSsoHandler.authorize(mAuthListener);
        } else {
            LogUtil.e(TAG, "Please setWeiboAuthInfo(...) for first");
        }
    }

    /**
     * 使用该控件进行授权登陆时，需要手动调用该函数。
     * <p>
     * 重要：使用该控件的 Activity 必须重写
     * {@link Activity#onActivityResult(int, int, Intent)}， 并在内部调用该函数，否则无法授权成功。
     * </p>
     * <p>
     * Sample Code：
     * </p>
     * 
     * <pre class="prettyprint">
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     super.onActivityResult(requestCode, resultCode, data);
     * 
     *     // 在此处调用
     *     mLoginButton.onActivityResult(requestCode, resultCode, data);
     * }
     * </pre>
     * 
     * @param requestCode
     *            请查看 {@link Activity#onActivityResult(int, int, Intent)}
     * @param resultCode
     *            请查看 {@link Activity#onActivityResult(int, int, Intent)}
     * @param data
     *            请查看 {@link Activity#onActivityResult(int, int, Intent)}
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    /**
     * 按钮初始化函数。
     * 
     * @param context
     *            上下文环境，一般为放置该 Button 的 Activity
     */
    private void initialize(Context context) {
        mContext = context;
        setOnClickListener(this);
    }
}
