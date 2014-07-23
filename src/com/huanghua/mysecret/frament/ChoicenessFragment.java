package com.huanghua.mysecret.frament;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.ChoicenessListAdapter;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class ChoicenessFragment extends FragmentBase implements
        IXListViewListener {
    private InputMethodManager inputMethodManager;

    private XListView mListChoiceness;
    private ChoicenessListAdapter mChoicenessAdapter;
    private List<Secret> mSecretList = new ArrayList<Secret>();
    User mUser = null;

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
        mListChoiceness = (XListView) findViewById(R.id.list_choiceness);
        mListChoiceness.setPullLoadEnable(false);
        mListChoiceness.setPullRefreshEnable(false);
        mListChoiceness.setXListViewListener(this);
        mListChoiceness.pullRefreshing();
        mChoicenessAdapter = new ChoicenessListAdapter(getActivity(),
                mSecretList);
        mListChoiceness.setAdapter(mChoicenessAdapter);
        BmobQuery<Secret> query = new BmobQuery<Secret>();
        final BmobQuery<SecretSupport> ssQuery = new BmobQuery<SecretSupport>();
        query.include("user");
        query.findObjects(getActivity(), new FindListener<Secret>() {
            @Override
            public void onSuccess(List<Secret> list) {
                mChoicenessAdapter.setList(list);
                showLog("query secret success:" + list.size() + " ssQuery:" + ssQuery);
            }

            @Override
            public void onError(int arg0, String arg1) {
                showLog("query secret error:" + arg1);
            }
        });
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }
}
