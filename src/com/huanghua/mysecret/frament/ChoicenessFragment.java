package com.huanghua.mysecret.frament;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.ChoicenessListAdapter;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.ui.WriteSecretActivity;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

/***
 * 精选秘密
 * 
 * @author huanghua
 * 
 */
public class ChoicenessFragment extends FragmentBase implements
        IXListViewListener, View.OnClickListener {
    private InputMethodManager inputMethodManager;

    private XListView mListChoiceness;
    private ChoicenessListAdapter mChoicenessAdapter;
    private List<Secret> mSecretList = new ArrayList<Secret>();
    private BmobQuery<Secret> mQuerySecret = null;
    private ImageButton mWriteSecret = null;
    private User mUser = null;

    private FindListener<Secret> mFindSecretListener = new FindListener<Secret>() {
        @Override
        public void onSuccess(List<Secret> list) {
            showLog("query secret success:" + list.size());
            mChoicenessAdapter.setList(list);
            refreshPull();
            mListChoiceness.setPullRefreshEnable(true);
        }

        @Override
        public void onError(int arg0, String arg1) {
            showLog("query secret error:" + arg1);
            refreshPull();
            mListChoiceness.setPullRefreshEnable(true);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_choiceness, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        /*
         * BmobQuery<User> query = new BmobQuery<User>();
         * query.getObject(getActivity(), "m57yAAAe", new GetListener<User>() {
         * 
         * @Override public void onSuccess(User arg0) { mUser = arg0;
         * showLog("query user onSuccess:" + arg0); }
         * 
         * @Override public void onFailure(int arg0, String arg1) {
         * showLog("query user onFailure:" + arg1); } });
         * 
         * BmobQuery<Secret> q = new BmobQuery<Secret>();
         * q.findObjects(getActivity(), new FindListener<Secret>() {
         * 
         * @Override public void onSuccess(List<Secret> list) {
         * //mChoicenessAdapter.setList(list); showLog("query secret success:" +
         * list.size()); Secret s = list.get(0); s.setUser(mUser);
         * s.update(getActivity(), new UpdateListener() {
         * 
         * @Override public void onSuccess() { showLog("update secret success");
         * }
         * 
         * @Override public void onFailure(int arg0, String arg1) {
         * showLog("update secret onFailure:" + arg1); } }); }
         * 
         * @Override public void onError(int arg0, String arg1) {
         * showLog("error:" + arg1); } });
         */
        init();
    }

    private void init() {
        mWriteSecret = (ImageButton) findViewById(R.id.write_secret);
        mWriteSecret.setOnClickListener(this);
        mListChoiceness = (XListView) findViewById(R.id.list_choiceness);
        mListChoiceness.setPullLoadEnable(false);
        mListChoiceness.setPullRefreshEnable(false);
        mListChoiceness.setXListViewListener(this);
        mListChoiceness.pullRefreshing();
        mChoicenessAdapter = new ChoicenessListAdapter(getActivity(),
                mSecretList);
        mListChoiceness.setAdapter(mChoicenessAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Secret>();
            mQuerySecret.include("user");
        }
        mQuerySecret.findObjects(getActivity(), mFindSecretListener);
    }

    @Override
    public void onRefresh() {
        showLog("choiceness onRefresh");
        mQuerySecret.findObjects(getActivity(), mFindSecretListener);
    }

    @Override
    public void onLoadMore() {
        showLog("choiceness onLoadMore");
    }

    private void refreshPull() {
        showLog("refreshPull :" + mListChoiceness.getPullRefreshing());
        if (mListChoiceness.getPullRefreshing()) {
            mListChoiceness.stopRefresh();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mWriteSecret) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), WriteSecretActivity.class);
            startAnimActivity(intent);
        }
    }
}
