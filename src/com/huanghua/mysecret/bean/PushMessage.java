package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobObject;

public class PushMessage extends BmobObject {

    private static final long serialVersionUID = 1L;
    public static final int PUSH_MESSAGE_TYPE_COMMENT = 1;
    public static final int PUSH_MESSAGE_TYPE_REPLY_COMMENT = 2;

    private User toUser;
    private int type;
    private Comment comment;

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
