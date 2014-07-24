package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {

    private static final long serialVersionUID = 1L;

    private boolean sex;

    private String avatar;

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
        if (u != null && u.getObjectId() != null && getObjectId() != null && u.getObjectId().equals(this.getObjectId())) {
            return true;
        } else {
            return false;
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
