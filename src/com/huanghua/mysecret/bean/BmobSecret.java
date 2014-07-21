package com.huanghua.mysecret.bean;

import android.R.integer;
import cn.bmob.v3.BmobObject;

public class BmobSecret extends BmobObject {

    private static final long serialVersionUID = 1L;

    private String userId;
    private integer status;
    private String contents;

    public BmobSecret() {
        setTableName("Secret");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public integer getStatus() {
        return status;
    }

    public void setStatus(integer status) {
        this.status = status;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
