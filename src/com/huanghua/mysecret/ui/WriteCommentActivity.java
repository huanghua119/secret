package com.huanghua.mysecret.ui;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.CommentListAdapter;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.load.DateLoad;
import com.huanghua.mysecret.util.CommonUtils;
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
    private XListView mCommentListView = null;
    private Secret mCurrentSecret = null;
    private View mSecretView = null;
    private CommentListAdapter mListAdapter = null;
    private List<Comment> mCommentList = new ArrayList<Comment>();
    private SupportView mSupportView = null;
    private BmobQuery<Comment> mQueryComent = null;
    private int mListPage = 1;
    private static final int LIST_DEFALUT_LIMIT = 20;
    private FindListener<Comment> mFindCommentListener = new FindListener<Comment>() {
        @Override
        public void onSuccess(List<Comment> arg0) {
            showLog("query comment success:" + arg0.size());
            mListAdapter.setList(arg0);
            Integer commentCount2 = DateLoad.getComment(mCurrentSecret
                    .getObjectId());
            int commentCount = commentCount2 == null ? 0 : commentCount2;
            if (commentCount > arg0.size()) {
                mCommentListView.setPullLoadEnable(true);
            } else {
                mCommentListView.setPullLoadEnable(false);
            }
            refreshPull();
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
        mCommentListView = (XListView) findViewById(R.id.commit_list);
        mCommentListView.setPullLoadEnable(false);
        mCommentListView.setPullRefreshEnable(false);
        mCommentListView.setXListViewListener(this);
        mCommentListView.pullRefreshing();
        mListAdapter = new CommentListAdapter(this, mCommentList, mCommentListView, mCurrentSecret);
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
        mPhoto.setImageResource(CommonUtils.HEAD_RESOURS[mCurrentSecret.getRandomHead()]);
        ImageView mItemPicView = (ImageView) mSecretView.findViewById(R.id.item_pic);
        String pic = mCurrentSecret.getPic();
        if (pic != null && !pic.equals("")) {
            ImageLoader.getInstance().displayImage(pic, mItemPicView,
                    ImageLoadOptions.getOptions());
            mItemPicView.setVisibility(View.VISIBLE);
        } else {
            mItemPicView.setVisibility(View.GONE);
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
        TextView mLocation = (TextView) mSecretView.findViewById(R.id.item_location);
        mLocation.setText(mCurrentSecret.getAddress());
    }

    @Override
    public void onClick(View v) {
        if (v == mGoToComment) {
            if (!checkNetwork()) {
                return;
            }
            if (checkUserLogin()) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("write_type", WriteSecretActivity.WRITE_TYPE_COMMENT);
            intent.putExtra("toUser", mCurrentSecret.getUser());
            intent.putExtra("secret", mCurrentSecret);
            intent.setClass(this, WriteSecretActivity.class);
            startAnimActivity(intent);
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        mListPage++;
        mQueryComent.setLimit(mListPage * LIST_DEFALUT_LIMIT);
        mQueryComent.setCachePolicy(CachePolicy.NETWORK_ONLY);
        mQueryComent.findObjects(this, mFindCommentListener);
    }

    private void refreshPull() {
        if (mCommentListView.getPullLoading()) {
            mCommentListView.stopLoadMore();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQueryComent == null) {
            mQueryComent = new BmobQuery<Comment>();
            mQueryComent.addWhereEqualTo("secret", mCurrentSecret);
            mQueryComent.order("-createdAt");
            boolean hasNetWork = CommonUtils.isNetworkAvailable(this);
            if (hasNetWork) {
                mQueryComent.setCachePolicy(CachePolicy.NETWORK_ONLY);
            } else {
                mQueryComent.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
            }
            mQueryComent.include("fromUser,parentComment,parentComment.fromUser");
            mListPage = 1;
            mQueryComent.setLimit(mListPage * LIST_DEFALUT_LIMIT);
        }
        mQueryComent.findObjects(this, mFindCommentListener);
        List<SecretSupport> allss = DateLoad.get(mCurrentSecret.getObjectId());
        if (allss != null) {
            mSupportView.refreshInCache(mCurrentSecret, allss);
            Integer count = DateLoad.getComment(mCurrentSecret.getObjectId());
            if (count != null) {
                mSupportView.setCommentCount(count);
            }
        }
        mSupportView.startQuery();
    }

    @Override
    protected void onDestroy() {
        showLog("write_comment onDestroy");
        DateLoad.clearCommentSupport();
        super.onDestroy();
    }

}
