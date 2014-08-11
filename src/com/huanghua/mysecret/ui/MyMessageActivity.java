package com.huanghua.mysecret.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
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

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.MyMessageListAdapter;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.PushMessage;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.util.SharePreferenceUtil;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

public class MyMessageActivity extends BaseActivity implements OnClickListener,
        IXListViewListener, OnItemClickListener {

    private XListView mMessageListView = null;
    private List<PushMessage> mCommentList = new ArrayList<PushMessage>();
    private MyMessageListAdapter mListAdapter = null;
    private BmobQuery<PushMessage> mQueryMessage = null;
    private int mSecretCount = 0;
    private int mListPage = 1;
    private boolean mQueryIng = false;
    private static final int LIST_DEFALUT_LIMIT = 20;
    private View mTopView = null;
    private TextView mEmptyText = null;

    private NotificationManager mNotificationManager;

    private FindListener<PushMessage> mFindMessageListener = new FindListener<PushMessage>() {
        @Override
        public void onSuccess(List<PushMessage> list) {
            mListAdapter.setList(list);
            if (list.size() < mSecretCount) {
                mMessageListView.setPullLoadEnable(true);
            } else {
                mMessageListView.setPullLoadEnable(false);
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
                mMessageListView.setPullLoadEnable(true);
            } else {
                mMessageListView.setPullLoadEnable(false);
            }
        }

        @Override
        public void onFailure(int arg0, String arg1) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mymessage_view);
        if (userManager.getCurrentUser() == null) {
            finish();
        }
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        init();
    }

    private void init() {
        mMessageListView = (XListView) findViewById(R.id.mymessage_list);
        mMessageListView.setPullLoadEnable(false);
        mMessageListView.setPullRefreshEnable(false);
        mMessageListView.setXListViewListener(this);
        mMessageListView.pullRefreshing();
        mMessageListView.setOnItemClickListener(this);
        mListAdapter = new MyMessageListAdapter(this, mCommentList);
        mMessageListView.setAdapter(mListAdapter);
        mTopView = findViewById(R.id.top_view);
        mTopView.setOnClickListener(this);
        mEmptyText = (TextView) findViewById(R.id.empty_text);
        mEmptyText.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == mTopView) {
            if (mMessageListView != null && mListAdapter.getCount() != 0) {
                mMessageListView.setSelection(0);
            }
        } else if (v == mEmptyText) {
            mListPage = 1;
            mQueryMessage.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQueryMessage.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQueryMessage.findObjects(this, mFindMessageListener);
            mQueryMessage.count(this, Comment.class, mCountListener);
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
            mQueryMessage.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQueryMessage.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQueryMessage.findObjects(this, mFindMessageListener);
        }
    }

    private void refreshPull() {
        if (mMessageListView.getPullLoading()) {
            mMessageListView.stopLoadMore();
        }
        mQueryIng = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mQueryMessage == null) {
            mQueryMessage = new BmobQuery<PushMessage>();
            mQueryMessage.order("-createdAt");
            mQueryMessage.include("comment,comment.secret,comment.fromUser,comment.parentComment,comment.secret.user");
            mListPage = 1;
            mQueryMessage.addWhereEqualTo("toUser",
                    userManager.getCurrentUser());
            mQueryMessage.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQueryMessage.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQueryMessage.findObjects(this, mFindMessageListener);
            mQueryMessage.count(this, Comment.class, mCountListener);
        }
        SharePreferenceUtil mSp = CustomApplcation.getInstance().getSpUtil();
        mSp.setNewMessage(false);
        mNotificationManager.cancel(DateQueryService.NOTIFICATION_MESSAGE_COMMENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        int headCount = mMessageListView.getHeaderViewsCount();
        if (position < headCount) {
            return;
        }
        PushMessage pm = mListAdapter.getList().get(position - headCount);
        Secret s = pm.getComment().getSecret();
        if (s != null && s.getContents() != null && !s.getContents().equals("")) {
            Intent intent = new Intent();
            intent.setClass(this, WriteCommentActivity.class);
            intent.putExtra("secret", s);
            startAnimActivity(intent);
        }
    }

}
