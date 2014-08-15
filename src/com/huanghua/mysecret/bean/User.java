package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {

    private static final long serialVersionUID = 1L;
    public static final int LOGIN_TYPE_WEIBO = 1;
    public static final int LOGIN_TYPE_TENCENT_QQ = 2;

    private boolean sex;

    private String avatar;

    private String othername;

    private Integer logintype;

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
        if (u != null && u.getObjectId() != null && getObjectId() != null
                && u.getObjectId().equals(this.getObjectId())) {
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

    public String getOthername() {
        return othername;
    }

    public void setOthername(String othername) {
        this.othername = othername;
    }

    public Integer getLogintype() {
        return logintype;
    }

    public void setLogintype(Integer logintype) {
        this.logintype = logintype;
    }

    @Override
    public String getUsername() {
        if (logintype != null && (logintype == LOGIN_TYPE_WEIBO || logintype == LOGIN_TYPE_TENCENT_QQ)) {
            return this.othername;
        } else {
            return super.getUsername();
        }
    }

}
