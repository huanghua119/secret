package com.huanghua.mysecret.frament;

import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.ApkBean;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.config.Config;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.ui.MyCommentActivity;
import com.huanghua.mysecret.ui.MySecretActivity;
import com.huanghua.mysecret.ui.UserLoginActivity;
import com.huanghua.mysecret.ui.WriteSecretActivity;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.util.ThemeUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/***
 * 更多设置
 * 
 * @author huanghua
 * 
 */
public class MoreFragment extends FragmentBase implements View.OnClickListener,
        View.OnTouchListener {

    private View mUserLogin = null;
    private View mToWriteSecret = null;
    private View mToMySecret = null;
    private View mToMyComment = null;
    private View mSwitchTheme = null;
    private TextView mCurrentTheme = null;

    private View mAboutOur = null;
    private View mCheckUpdate = null;
    private TextView mVersion = null;
    private TextView mUserName = null;
    private ImageView mUserPhoto = null;
    private Dialog mDialog = null;
    private String mCurrentVersion = "";

    private static final int MESSAGE_GET_UPDATE_XML = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MESSAGE_GET_UPDATE_XML:
                boolean success = msg.getData().getBoolean("success");
                if (success) {
                    ApkBean apkBean = (ApkBean) msg.getData().getSerializable(
                            "apk");
                    if (mCurrentVersion.equals(apkBean.getVersionName())) {
                        ShowToast(R.string.is_new_version,
                                R.drawable.popup_warning);
                    } else {
                        CommonUtils.createUpdateVersionDialog(getActivity(),
                                apkBean).show();
                    }
                } else {
                    ShowToast(R.string.check_fail);
                }
                mDialog.dismiss();
                break;
            default:
                break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_more, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init() {
        mUserLogin = findViewById(R.id.user_login);
        mUserLogin.setOnTouchListener(this);
        mUserLogin.setOnClickListener(this);
        mUserName = (TextView) findViewById(R.id.user_name);
        mUserPhoto = (ImageView) findViewById(R.id.user_photo);
        mToWriteSecret = findViewById(R.id.to_write_secret);
        mToMySecret = findViewById(R.id.to_my_secret);
        mToMyComment = findViewById(R.id.to_my_comment);
        mCheckUpdate = findViewById(R.id.check_update);
        mVersion = (TextView) findViewById(R.id.current_version);
        mAboutOur = findViewById(R.id.about_out);
        mSwitchTheme = findViewById(R.id.switch_theme);
        mCurrentTheme = (TextView) findViewById(R.id.current_theme);
        mToWriteSecret.setOnTouchListener(this);
        mToWriteSecret.setOnClickListener(this);
        mToMySecret.setOnTouchListener(this);
        mToMySecret.setOnClickListener(this);
        mToMyComment.setOnTouchListener(this);
        mToMyComment.setOnClickListener(this);
        mCheckUpdate.setOnTouchListener(this);
        mCheckUpdate.setOnClickListener(this);
        mAboutOur.setOnClickListener(this);
        mAboutOur.setOnTouchListener(this);
        mSwitchTheme.setOnClickListener(this);
        mSwitchTheme.setOnTouchListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = UserManager.getInstance(getActivity()).getCurrentUser();
        if (user != null) {
            mUserName.setText(user.getUsername());
            String avatar = user.getAvatar();
            if (avatar != null && !avatar.equals("")) {
                ImageLoader.getInstance().displayImage(avatar, mUserPhoto,
                        ImageLoadOptions.getOptions());
            } else {
                mUserPhoto.setImageResource(R.drawable.user_photo_default);
            }
        } else {
            mUserName.setText(R.string.user_nologin);
            mUserPhoto.setImageResource(R.drawable.default_icon);
        }
        setShowThemeToUI();
        showCurrentVersion();
    }

    private void setShowThemeToUI() {
        int theme = ThemeUtil.getCurrentTheme(getActivity());
        String toTheme = getString(R.string.switch_theme_to);
        switch (theme) {
        case ThemeUtil.THEME_NIGHT:
            mCurrentTheme.setText(toTheme
                    + getString(R.string.switch_theme_to_during));
            break;
        case ThemeUtil.THEME_DURING:
            mCurrentTheme.setText(toTheme
                    + getString(R.string.switch_theme_to_night));
            break;
        }
    }

    private void showCurrentVersion() {
        mCurrentVersion = CommonUtils.getCurrentVersionName(getActivity());
        mVersion.setText(getString(R.string.current_version) + mCurrentVersion);
    }

    public void onClick(View v) {
        if (v == mCheckUpdate) {
            mDialog = CommonUtils.createLoadingDialog(getActivity(),
                    getString(R.string.check_update_now));
            mDialog.show();
            startCheckVersion();
        } else if (v == mAboutOur) {
            String installationId = "CF86DE3F8A9F6F6605A0D2CB6AEE5B10";
            BmobPushManager bmobPush = new BmobPushManager(getActivity());
            BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
            query.addWhereEqualTo("installationId", installationId);
            bmobPush.setQuery(query);
            bmobPush.pushMessage("消息内容");
        } else if (v == mSwitchTheme) {
            ThemeUtil.switchTheme((BaseActivity) getActivity());
        } else if (((BaseActivity) getActivity()).checkUserLogin()) {
            return;
        }
        if (v == mUserLogin) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), UserLoginActivity.class);
            startAnimActivity(intent);
        } else if (v == mToWriteSecret) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WriteSecretActivity.class);
            startAnimActivity(intent);
        } else if (v == mToMySecret) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), MySecretActivity.class);
            startAnimActivity(intent);
        } else if (v == mToMyComment) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), MyCommentActivity.class);
            startAnimActivity(intent);
        }
    }

    private void startCheckVersion() {
        new Thread() {
            @Override
            public void run() {
                String xml = CommonUtils.getXmlFromUrl(Config.UPDATE_XML_PATH);
                boolean success = false;
                ApkBean apk = null;
                if (xml != null && !xml.equals("") && !xml.equals("error")) {
                    Map<String, ApkBean> result = CommonUtils.parseXml(xml);
                    if (result != null) {
                        success = true;
                        apk = result.get("secret");
                    }
                }
                Message m = new Message();
                m.what = MESSAGE_GET_UPDATE_XML;
                Bundle b = new Bundle();
                if (success) {
                    b.putSerializable("apk", apk);
                }
                b.putBoolean("success", success);
                m.setData(b);
                mHandler.sendMessage(m);
            }
        }.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (v instanceof RelativeLayout) {
                RelativeLayout r = ((RelativeLayout) v);
                r.setPressed(true);
                for (int i = 0; i < r.getChildCount(); i++) {
                    r.getChildAt(i).setPressed(true);
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (v instanceof RelativeLayout) {
                RelativeLayout r = ((RelativeLayout) v);
                r.setPressed(false);
                for (int i = 0; i < r.getChildCount(); i++) {
                    r.getChildAt(i).setPressed(false);
                }
            }
            break;
        }
        return false;
    }
}
