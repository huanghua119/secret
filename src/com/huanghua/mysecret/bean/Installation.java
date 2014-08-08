package com.huanghua.mysecret.bean;

import android.content.Context;
import cn.bmob.v3.BmobInstallation;

public class Installation extends BmobInstallation {

    private static final long serialVersionUID = 1L;
    private User user;

    public Installation(Context arg0) {
        super(arg0);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
