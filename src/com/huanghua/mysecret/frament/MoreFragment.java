package com.huanghua.mysecret.frament;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.UserLoginActivity;
import com.huanghua.mysecret.util.ImageLoadOptions;
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
    private TextView mUserName = null;
    private ImageView mUserPhoto = null;

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
    }

    public void onClick(View v) {
        if (v == mUserLogin) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), UserLoginActivity.class);
            startAnimActivity(intent);
        }
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
