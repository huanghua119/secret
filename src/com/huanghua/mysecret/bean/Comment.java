package com.huanghua.mysecret.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Comment extends BmobObject {

    private static final long serialVersionUID = 1L;
    private User fromUser;
    private User toUser;
    private String contents;
    private Secret secret;
    private BmobRelation commentSupport;
    private Comment parentComment;

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

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Secret getSecret() {
        return secret;
    }

    public void setSecret(Secret secret) {
        this.secret = secret;
    }

    public BmobRelation getCommentSupport() {
        return commentSupport;
    }

    public void setCommentSupport(BmobRelation commentSupport) {
        this.commentSupport = commentSupport;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }
}
