package com.huanghua.mysecret.bean;

import java.io.Serializable;

public class ApkBean implements Serializable {

    private static final long serialVersionUID = -6489256995125618854L;
    private String name = null;
    private String path = null;
    private int versionCode = 0;
    private String versionName = null;
    private String detail = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

}
