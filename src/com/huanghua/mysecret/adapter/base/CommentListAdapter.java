package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommentListAdapter extends BaseListAdapter<Comment> {

    public CommentListAdapter(Context context, List<Comment> list) {
        super(context, list);
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.comment_item_view, null);
        }
        Comment coment = (Comment) getList().get(position);

        User user = coment.getFromUser();
        ImageView mPhoto = (ImageView) view
                .findViewById(R.id.item_comment_photo);
        TextView mName = (TextView) view.findViewById(R.id.item_comment_name);
        TextView mContents = (TextView) view
                .findViewById(R.id.item_comment_contents);

        String avatar = user.getAvatar();
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mPhoto,
                    ImageLoadOptions.getOptions());
        } else {
            mPhoto.setImageResource(R.drawable.user_photo_default);
        }

        mContents.setText(coment.getContents());
        Drawable drawable = mContext.getResources().getDrawable(
                user.isSex() ? R.drawable.man : R.drawable.women);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mName.setCompoundDrawables(drawable, null, null, null);
        mName.setText(user.getUsername());
        return view;
    }

}
