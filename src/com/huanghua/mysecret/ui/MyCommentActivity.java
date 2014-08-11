package com.huanghua.mysecret.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.MyCommentListAdapter;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

public class MyCommentActivity extends BaseActivity implements OnClickListener,
        IXListViewListener, OnItemClickListener {

    private XListView mSecretListView = null;
    private List<Comment> mCommentList = new ArrayList<Comment>();
    private MyCommentListAdapter mListAdapter = null;
    private BmobQuery<Comment> mQuerySecret = null;
    private int mSecretCount = 0;
    private int mListPage = 1;
    private boolean mQueryIng = false;
    private static final int LIST_DEFALUT_LIMIT = 20;
    private View mTopView = null;
    private TextView mEmptyText = null;

    private FindListener<Comment> mFindSecretListener = new FindListener<Comment>() {
        @Override
        public void onSuccess(List<Comment> list) {
            mListAdapter.setList(list);
            if (list.size() < mSecretCount) {
                mSecretListView.setPullLoadEnable(true);
            } else {
                mSecretListView.setPullLoadEnable(false);
            }
            if (list.size() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
            } else {
                mEmptyText.setVisibility(View.GONE);
            }
            refreshPull();
        }

        @Override
        public void onError(int arg0, String arg1) {
            showLog("comment_list", "error:" + arg1);
            if (mListPage > 1) {
                mListPage--;
            }
            if (arg0 == 9010) {
                ShowToast(R.string.no_check_network);
            }
            if (mListAdapter.getList() == null || mListAdapter.getList().size() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
            } else {
                mEmptyText.setVisibility(View.GONE);
            }
            refreshPull();
        }
    };

    private CountListener mCountListener = new CountListener() {
        @Override
        public void onSuccess(int arg0) {
            mSecretCount = arg0;
            if (mSecretCount > mListPage * LIST_DEFALUT_LIMIT) {
                mSecretListView.setPullLoadEnable(true);
            } else {
                mSecretListView.setPullLoadEnable(false);
            }
        }

        @Override
        public void onFailure(int arg0, String arg1) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mycomment_view);
        if (userManager.getCurrentUser() == null) {
            finish();
        }
        init();
    }

    private void init() {
        mSecretListView = (XListView) findViewById(R.id.mycommnet_list);
        mSecretListView.setPullLoadEnable(false);
        mSecretListView.setPullRefreshEnable(false);
        mSecretListView.setXListViewListener(this);
        mSecretListView.pullRefreshing();
        mSecretListView.setOnItemClickListener(this);
        mListAdapter = new MyCommentListAdapter(this, mCommentList);
        mSecretListView.setAdapter(mListAdapter);
        mTopView = findViewById(R.id.top_view);
        mTopView.setOnClickListener(this);
        mEmptyText = (TextView) findViewById(R.id.empty_text);
        mEmptyText.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == mTopView) {
            if (mSecretListView != null && mListAdapter.getCount() != 0) {
                mSecretListView.setSelection(0);
            }
        } else if (v == mEmptyText) {
            mListPage = 1;
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(this, mFindSecretListener);
            mQuerySecret.count(this, Comment.class, mCountListener);
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        if (!mQueryIng) {
            mListPage++;
            mQueryIng = true;
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(this, mFindSecretListener);
        }
    }

    private void refreshPull() {
        if (mSecretListView.getPullLoading()) {
            mSecretListView.stopLoadMore();
        }
        mQueryIng = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Comment>();
            mQuerySecret.order("-createdAt");
            mQuerySecret.include("secret,secret.user,fromUser");
            mListPage = 1;
            mQuerySecret.addWhereEqualTo("fromUser",
                    userManager.getCurrentUser());
            //mQuerySecret.addWhereExists("secret");
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(this, mFindSecretListener);
            mQuerySecret.count(this, Comment.class, mCountListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Comment c = mListAdapter.getList().get(position - 1);
        Secret s = c.getSecret();
        if (s != null && s.getContents() != null && !s.getContents().equals("")) {
            Intent intent = new Intent();
            intent.setClass(this, WriteCommentActivity.class);
            intent.putExtra("secret", s);
            startAnimActivity(intent);
        }
    }

}
