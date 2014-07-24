package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobObject;

public class CommentSupport extends BmobObject {

    private static final long serialVersionUID = 1L;
    private User fromUser;
    private User toUser;
    private Comment comment;
    private boolean support;

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support = support;
    }
}
