package com.huanghua.mysecret.ui;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.CommentListAdapter;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.view.DateTextView;
import com.huanghua.mysecret.view.SupportView;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class WriteCommentActivity extends BaseActivity implements
        OnClickListener, IXListViewListener {

    private TextView mGoToComment;
    private TextView mBack;
    private XListView mCommentListView = null;
    private Secret mCurrentSecret = null;
    private View mSecretView = null;
    private CommentListAdapter mListAdapter = null;
    private List<Comment> mCommentList = new ArrayList<Comment>();
    private SupportView mSupportView = null;
    private BmobQuery<Comment> mQueryComent = null;
    private FindListener<Comment> mFindCommentListener = new FindListener<Comment>() {
        @Override
        public void onSuccess(List<Comment> arg0) {
            showLog("query comment success:" + arg0.size());
            mListAdapter.setList(arg0);
        }

        @Override
        public void onError(int arg0, String arg1) {
            showLog("query comment error:" + arg1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_comment_view);
        mCurrentSecret = (Secret) getIntent().getSerializableExtra("secret");
        init();
    }

    private void init() {
        mGoToComment = (TextView) findViewById(R.id.goToComment);
        mGoToComment.setOnClickListener(this);
        mBack = (TextView) findViewById(R.id.btn_cancel);
        mBack.setOnClickListener(this);
        mCommentListView = (XListView) findViewById(R.id.commit_list);
        mCommentListView.setPullLoadEnable(false);
        mCommentListView.setPullRefreshEnable(false);
        mCommentListView.setXListViewListener(this);
        mCommentListView.pullRefreshing();
        mListAdapter = new CommentListAdapter(this, mCommentList);
        mSecretView = mInFlater.inflate(R.layout.secret_item_view, null);
        initSecretView();
        mCommentListView.addHeaderView(mSecretView);
        mCommentListView.setAdapter(mListAdapter);
    }

    private void initSecretView() {
        User user = mCurrentSecret.getUser();
        ImageView mPhoto = (ImageView) mSecretView
                .findViewById(R.id.item_photo);
        TextView mName = (TextView) mSecretView.findViewById(R.id.item_name);
        DateTextView mDate = (DateTextView) mSecretView.findViewById(R.id.item_date);
        mDate.setInitDate(mCurrentSecret.getCreatedAt());
        TextView mContents = (TextView) mSecretView
                .findViewById(R.id.item_contents);
        String avatar = user.getAvatar();
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mPhoto,
                    ImageLoadOptions.getOptions());
        } else {
            mPhoto.setImageResource(R.drawable.user_photo_default);
        }

        mContents.setText(mCurrentSecret.getContents());
        mName.setText(user.getUsername());
        Drawable drawable = getResources().getDrawable(
                user.isSex() ? R.drawable.man : R.drawable.women);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mName.setCompoundDrawables(drawable, null, null, null);
        mSupportView = (SupportView) mSecretView.findViewById(R.id.item_bottom);
        mSupportView.setSecret(mCurrentSecret);
        mSupportView.setInComment(true);
    }

    @Override
    public void onClick(View v) {
        if (v == mGoToComment) {
            if (!checkNetwork()) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("write_type", WriteSecretActivity.WRITE_TYPE_COMMENT);
            intent.putExtra("toUser", mCurrentSecret.getUser());
            intent.putExtra("secret", mCurrentSecret);
            intent.setClass(this, WriteSecretActivity.class);
            startAnimActivity(intent);
        } else if (v == mBack) {
            onBackPressed();
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQueryComent == null) {
            mQueryComent = new BmobQuery<Comment>();
            mQueryComent.addWhereEqualTo("secret", mCurrentSecret);
            mQueryComent.order("-createdAt");
            mQueryComent.include("fromUser");
        }
        mQueryComent.findObjects(this, mFindCommentListener);
    }

}
