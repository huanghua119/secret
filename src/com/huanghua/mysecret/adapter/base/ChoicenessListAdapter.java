package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.huanghua.mysecret.bean.BmobSecret;

public class ChoicenessListAdapter extends BaseListAdapter<BmobSecret> {

    public ChoicenessListAdapter(Context context, List<BmobSecret> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        List<BmobSecret> list = getList();
        Log.i("huanghua", "list:" + list.size());
        return null;
    }

}
