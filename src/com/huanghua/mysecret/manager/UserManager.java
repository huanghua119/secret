package com.huanghua.mysecret.manager;

import java.util.List;

import android.content.Context;
import android.util.Log;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.bean.User;

public class UserManager {

    private static UserManager mUserManager;
    private static Context sContext;
    private User mCurrentUser;

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
        mCurrentUser = new User();
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", "HH");
        query.addWhereEqualTo("password", "123456");
        query.findObjects(sContext, new FindListener<User>() {
            @Override
            public void onSuccess(List<User> arg0) {
                mCurrentUser = arg0.get(0);
                showLog("query currentUser success:" + arg0);
            }

            @Override
            public void onError(int arg0, String arg1) {
                showLog("query currentUser error:" + arg0);
            }
        });
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    public void showLog(String msg) {
        if (CustomApplcation.DEBUG) {
            Log.i(CustomApplcation.TAG, msg);
        }
    }
}
