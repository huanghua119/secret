package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {

    private static final long serialVersionUID = 1L;

    private boolean sex;

    public User() {

    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    @Override
    public boolean equals(Object o) {
        User u = (User) o;
        if (u.getObjectId().equals(this.getObjectId())) {
            return true;
        } else {
            return false;
        }
    }

}
