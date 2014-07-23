package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.view.SupportView;

public class ChoicenessListAdapter extends BaseListAdapter<Secret> {

    public ChoicenessListAdapter(Context context, List<Secret> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.secret_item_view, null);
        }
        List<Secret> list = getList();
        Secret secret = list.get(position);
        User user = secret.getUser();
        TextView mName = (TextView) view.findViewById(R.id.item_name);
        TextView mDate = (TextView) view.findViewById(R.id.item_date);
        TextView mContents = (TextView) view.findViewById(R.id.item_contents);

        mDate.setText(secret.getCreatedAt());
        mContents.setText(secret.getContents());
        mName.setText(user.getUsername());
        Drawable drawable = mContext.getResources().getDrawable(
                user.isSex() ? R.drawable.man : R.drawable.women);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mName.setCompoundDrawables(drawable, null, null, null);

        SupportView sv = (SupportView) view.findViewById(R.id.item_bottom);
        sv.setSecret(secret);
        return view;
    }

}
