package com.huanghua.mysecret.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.ChoicenessListAdapter;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MySecretActivity extends BaseActivity implements OnClickListener,
        IXListViewListener, OnItemClickListener {

    private XListView mSecretListView = null;
    private List<Secret> mSecretList = new ArrayList<Secret>();
    private ChoicenessListAdapter mSecretAdapter = null;
    private BmobQuery<Secret> mQuerySecret = null;
    private int mSecretCount = 0;
    private int mListPage = 1;
    private boolean mQueryIng = false;
    private static final int LIST_DEFALUT_LIMIT = 20;

    private ImageView mUserPhoto = null;
    private TextView mUserNameView = null;
    private TextView mUserSexView = null;
    private TextView mSecretCountView = null;
    private TextView mEmptyText = null;
    private View mTopView = null;

    private FindListener<Secret> mFindSecretListener = new FindListener<Secret>() {
        @Override
        public void onSuccess(List<Secret> list) {
            mSecretAdapter.setList(list);
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
            if (mListPage > 1) {
                mListPage--;
            }
            if (arg0 == 9010) {
                ShowToast(R.string.no_check_network);
            }
            refreshPull();
        }
    };
    private CountListener mCountListener = new CountListener() {
        @Override
        public void onSuccess(int arg0) {
            mSecretCountView.setText(arg0 + "");
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
        setContentView(R.layout.mysecret_view);
        if (userManager.getCurrentUser() == null) {
            finish();
        }
        init();
    }

    private void init() {
        mSecretListView = (XListView) findViewById(R.id.secret_list);
        mSecretListView.setPullLoadEnable(false);
        mSecretListView.setPullRefreshEnable(false);
        mSecretListView.setXListViewListener(this);
        mSecretListView.pullRefreshing();
        mSecretAdapter = new ChoicenessListAdapter(this, mSecretList);
        View topView = mInFlater.inflate(R.layout.mysecret_top_view, null);
        mUserPhoto = (ImageView) topView.findViewById(R.id.user_photo);
        mUserNameView = (TextView) topView.findViewById(R.id.user_name);
        mSecretCountView = (TextView) topView.findViewById(R.id.mysecret_count);
        mUserSexView = (TextView) topView.findViewById(R.id.user_sex);
        // mSecretListView.addHeaderView(topView);
        mSecretListView.setAdapter(mSecretAdapter);
        mSecretListView.setOnItemClickListener(this);
        mTopView = findViewById(R.id.top_view);
        mTopView.setOnClickListener(this);
        mEmptyText = (TextView) findViewById(R.id.empty_text);
        mEmptyText.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == mTopView) {
            if (mSecretListView != null && mSecretAdapter.getCount() != 0) {
                mSecretListView.setSelection(0);
            }
        } else if (v == mEmptyText) {
            mListPage = 1;
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.findObjects(this, mFindSecretListener);
            mQuerySecret.count(this, Secret.class, mCountListener);
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
            mQuerySecret.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
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
        User user = userManager.getCurrentUser();
        if (user != null) {
            mUserNameView.setText(user.getUsername());
            String avatar = user.getAvatar();
            if (avatar != null && !avatar.equals("")) {
                ImageLoader.getInstance().displayImage(avatar, mUserPhoto,
                        ImageLoadOptions.getOptions());
            } else {
                mUserPhoto.setImageResource(R.drawable.user_photo_default);
            }
            mUserSexView.setText(user.isSex() ? R.string.sex_man
                    : R.string.sex_woman);
        }
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Secret>();
            mQuerySecret.order("-createdAt");
            mQuerySecret.include("user");
            mListPage = 1;
            mQuerySecret.addWhereEqualTo("user", userManager.getCurrentUser());
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
            mQuerySecret.findObjects(this, mFindSecretListener);
            mQuerySecret.count(this, Secret.class, mCountListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if (position < 1) {
            return;
        }
        Secret s = mSecretAdapter.getList().get(position - 1);
        if (s != null) {
            Intent intent = new Intent();
            intent.setClass(this, WriteCommentActivity.class);
            intent.putExtra("secret", s);
            startAnimActivity(intent);
        }
    }

}
