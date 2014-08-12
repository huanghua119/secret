package com.huanghua.mysecret.frament;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.ChoicenessListAdapter;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.load.DateLoadThreadManager;
import com.huanghua.mysecret.service.DateQueryService;
import com.huanghua.mysecret.ui.WriteCommentActivity;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

/***
 * 热门秘密
 *
 * @author huanghua
 *
 */
public class VeryTopFragment extends FragmentBase2 implements
        IXListViewListener, View.OnClickListener, OnItemClickListener {

    private XListView mListChoiceness;
    private ChoicenessListAdapter mChoicenessAdapter;
    private List<Secret> mSecretList = new ArrayList<Secret>();
    private BmobQuery<Secret> mQuerySecret = null;
    private boolean mQueryIng = false;
    private View mLoadView = null;
    private ImageView mLoadImage = null;
    private static final int LIST_DEFALUT_LIMIT = 20;
    private int mListPage = 1;
    private int mSecretCount = 0;

    private FindListener<Secret> mFindSecretListener = new FindListener<Secret>() {
        @Override
        public void onSuccess(List<Secret> list) {
            showLog("query secret success:" + list.size());
            mChoicenessAdapter.setList(list);
            refreshPull();
            if (mLoadView.getVisibility() == View.VISIBLE) {
                mLoadView.setVisibility(View.GONE);
                mLoadImage.clearAnimation();
            }
            if (list.size() > 0) {
                DateQueryService.mLastSecretId = list.get(0).getObjectId();
            }
            if (list.size() < mSecretCount) {
                mListChoiceness.setPullLoadEnable(true);
            } else {
                mListChoiceness.setPullLoadEnable(false);
            }
            mListChoiceness.setPullRefreshEnable(true);
        }

        @Override
        public void onError(int arg0, String arg1) {
            showLog("query secret error:" + arg1 + " arg0:" + arg0);
            if (mListPage > 1) {
                mListPage--;
            }
            if (arg0 == 9010) {
                ShowToast(R.string.no_check_network);
            }
            refreshPull();
            mListChoiceness.setPullRefreshEnable(true);
        }
    };

    private CountListener mCountListener = new CountListener() {
        @Override
        public void onSuccess(int arg0) {
            mSecretCount = arg0;
            if (mSecretCount > mListPage * LIST_DEFALUT_LIMIT) {
                mListChoiceness.setPullLoadEnable(true);
            } else {
                mListChoiceness.setPullLoadEnable(false);
            }
        }

        @Override
        public void onFailure(int arg0, String arg1) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_verynew_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init() {
        mLoadView = findViewById(R.id.load_view);
        mLoadImage = (ImageView) findViewById(R.id.load_img);
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                getActivity(), R.anim.loading_animation);
        mLoadImage.startAnimation(hyperspaceJumpAnimation);

        mListChoiceness = (XListView) findViewById(R.id.list_choiceness);
        mListChoiceness.setPullLoadEnable(false);
        mListChoiceness.setPullRefreshEnable(true);
        mListChoiceness.setXListViewListener(this);
        mListChoiceness.pullRefreshing();
        mChoicenessAdapter = new ChoicenessListAdapter(getActivity(),
                mSecretList);
        mListChoiceness.setAdapter(mChoicenessAdapter);
        mListChoiceness.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Secret>();
            mQuerySecret.include("user");
            mQuerySecret.addWhereExists("user");
            mQuerySecret.addWhereGreaterThanOrEqualTo("commentCount",
                    Integer.parseInt(getString(R.string.def_top_sercet_count)));
            mQuerySecret.order("-createdAt");
            mListPage = 1;
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.CACHE_ELSE_NETWORK);
            mQuerySecret.findObjects(getActivity(), mFindSecretListener);
            mQuerySecret.count(getActivity(), Secret.class, mCountListener);
        }

    }

    @Override
    public void onRefresh() {
        showLog("choiceness onRefresh");
        if (!mQueryIng) {
            mListPage = 1;
            mQueryIng = true;
            DateLoadThreadManager.removeAllTask();
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(getActivity(), mFindSecretListener);
            mQuerySecret.count(getActivity(), Secret.class, mCountListener);
        }
    }

    @Override
    public void onLoadMore() {
        showLog("choiceness onLoadMore:" + mListChoiceness.getPullLoading());
        if (!mQueryIng) {
            mListPage++;
            mQueryIng = true;
            mQuerySecret.setLimit(mListPage * LIST_DEFALUT_LIMIT);
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
            mQuerySecret.findObjects(getActivity(), mFindSecretListener);
        }
    }

    private void refreshPull() {
        showLog("refreshPull :" + mListChoiceness.getPullRefreshing());
        if (mListChoiceness.getPullRefreshing()) {
            mListChoiceness.stopRefresh();
            if (DateQueryService.sHasNewSecret && mQueryIng) {
                DateQueryService.sHasNewSecret = false;
                getActivity().sendBroadcast(
                        new Intent(DateQueryService.QUERY_NEW_SECRTE_ACTION));
            }
        }
        if (mListChoiceness.getPullLoading()) {
            mListChoiceness.stopLoadMore();
        }
        mQueryIng = false;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        showLog("choicess", "onItemClick position:" + position);
        Secret s = mChoicenessAdapter.getList().get(position - 1);
        if (s != null) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WriteCommentActivity.class);
            intent.putExtra("secret", s);
            startAnimActivity(intent);
        }
    }

    public void toTopSelect() {
        if (mListChoiceness != null && mListChoiceness.getCount() > 0
                && !mListChoiceness.getPullRefreshing()) {
            mListChoiceness.setSelection(0);
            mListChoiceness.pullRefreshing();
            mListChoiceness.startRefresh();
        }
    }

    public void toListViewTop() {
        if (mListChoiceness != null && mChoicenessAdapter.getCount() != 0) {
            mListChoiceness.setSelection(0);
        }
    }
}
