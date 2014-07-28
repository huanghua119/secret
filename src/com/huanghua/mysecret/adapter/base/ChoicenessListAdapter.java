package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.util.ViewHolder;
import com.huanghua.mysecret.view.DateTextView;
import com.huanghua.mysecret.view.SupportView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChoicenessListAdapter extends BaseListAdapter<Secret> {

    public ChoicenessListAdapter(Context context, List<Secret> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.secret_item_view, null);
        }
        List<Secret> list = getList();
        Secret secret = list.get(position);
        User user = secret.getUser();
        ImageView mPhoto = ViewHolder.get(view, R.id.item_photo);
        TextView mName = ViewHolder.get(view, R.id.item_name);
        TextView mLocation = ViewHolder.get(view, R.id.item_location);
        DateTextView mDate = ViewHolder.get(view, R.id.item_date);
        mDate.setInitDate(secret.getCreatedAt());
        TextView mContents = ViewHolder.get(view, R.id.item_contents);

        String avatar = user.getAvatar();
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mPhoto,
                    ImageLoadOptions.getOptions());
        } else {
            mPhoto.setImageResource(R.drawable.user_photo_default);
        }

        mContents.setText(secret.getContents());
        mName.setText(user.getUsername());

        mLocation.setText(secret.getAddress());

        Drawable drawable = mContext.getResources().getDrawable(
                user.isSex() ? R.drawable.man : R.drawable.women);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mName.setCompoundDrawables(drawable, null, null, null);

        final SupportView sv = ViewHolder.get(view, R.id.item_bottom);
        sv.setSecret(secret);
        setOnInViewClickListener(R.id.item_commit,
                new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v,
                            Integer position, Object values) {
                        sv.onClick(v);
                    }
                });
        setOnInViewClickListener(R.id.item_support_cry,
                new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v,
                            Integer position, Object values) {
                        sv.onClick(v);
                    }
                });
        setOnInViewClickListener(R.id.item_support_happy,
                new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v,
                            Integer position, Object values) {
                        sv.onClick(v);
                    }
                });
        return view;
    }

}
