package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobGeoPoint;

public class Secret extends BmobObject {

    private static final long serialVersionUID = 1L;

    private Integer status;
    private String contents;
    private User user;
    private BmobGeoPoint location;
    private String address;
    private Integer commentCount;
    private Integer randomHead;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BmobGeoPoint getLocation() {
        return location;
    }

    public void setLocation(BmobGeoPoint location) {
        this.location = location;
    }

    public Secret() {
        // setTableName("Secret");
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getRandomHead() {
        if (randomHead == null) {
            return 0;
        }
        return randomHead;
    }

    public void setRandomHead(Integer randomHead) {
        this.randomHead = randomHead;
    }
}
