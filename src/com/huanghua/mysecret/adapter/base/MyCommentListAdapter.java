package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;

public class MyCommentListAdapter extends BaseListAdapter<Comment> {

    private boolean mNearSecret = false;

    public MyCommentListAdapter(Context context, List<Comment> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.secret_item_view, null);
        }
        List<Comment> list = getList();
        Comment comment = list.get(position);
        return view;
    }

}
