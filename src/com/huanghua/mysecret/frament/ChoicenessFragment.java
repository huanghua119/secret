package com.huanghua.mysecret.frament;

import java.util.ArrayList;
import java.util.List;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.adapter.base.ChoicenessListAdapter;
import com.huanghua.mysecret.bean.BmobSecret;
import com.huanghua.mysecret.view.xlist.XListView;
import com.huanghua.mysecret.view.xlist.XListView.IXListViewListener;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class ChoicenessFragment extends FragmentBase implements IXListViewListener{
    private InputMethodManager inputMethodManager;
    
    private XListView mListChoiceness;
    private ChoicenessListAdapter mChoicenessAdapter;
    private List<BmobSecret> mSecretList = new ArrayList<BmobSecret>();

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
        init();
    }

    private void init() {
        mListChoiceness = (XListView) findViewById(R.id.list_choiceness);
        mListChoiceness.setPullLoadEnable(false);
        mListChoiceness.setPullRefreshEnable(false);
        mListChoiceness.setXListViewListener(this);
        mListChoiceness.pullRefreshing();
        mChoicenessAdapter = new ChoicenessListAdapter(getActivity(), mSecretList);
        mListChoiceness.setAdapter(mChoicenessAdapter);
    }

    @Override
    public void onRefresh() {
        
    }

    @Override
    public void onLoadMore() {
        
    }
}
