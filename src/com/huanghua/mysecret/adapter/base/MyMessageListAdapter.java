package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.PushMessage;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.util.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyMessageListAdapter extends BaseListAdapter<PushMessage> {

    private Context mContext;

    public MyMessageListAdapter(Context context, List<PushMessage> list) {
        super(context, list);
        mContext = context;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.mymessage_item_view, null);
        }
        List<PushMessage> list = getList();
        final PushMessage message = list.get(position);
        Comment comment = message.getComment();
        Comment parentComment = comment.getParentComment();
        User user = comment.getFromUser();
        Secret secret = comment.getSecret();
        int type = message.getType();
        ImageView mUserPhoto = ViewHolder.get(view, R.id.item_message_photo);
        TextView mUserNameView = ViewHolder.get(view, R.id.item_message_name);
        TextView mDateView = ViewHolder.get(view, R.id.item_date);
        TextView mContentOne = ViewHolder.get(view, R.id.item_contents_one);
        TextView mContentTwo = ViewHolder.get(view, R.id.item_contents_two);

        String avatar = user.getAvatar();
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mUserPhoto,
                    ImageLoadOptions.getOptions());
        } else {
            mUserPhoto.setImageResource(R.drawable.user_photo_default);
        }
        String name = user.getUsername() + "  ";
        String contentOne = mContext.getString(R.string.reply_you) + comment.getContents();
        if (type == PushMessage.PUSH_MESSAGE_TYPE_COMMENT) {
            name += mContext.getString(R.string.comment_your_secret);
            mContentOne.setText(contentOne);
            mContentTwo.setText(mContext.getString(R.string.your_secret) + secret.getContents());
        } else if (type == PushMessage.PUSH_MESSAGE_TYPE_REPLY_COMMENT) {
            name += mContext.getString(R.string.comment_your_comment);
            mContentOne.setText(contentOne);
            mContentTwo.setText(mContext.getString(R.string.your_comment) + parentComment.getContents());
        }
        mUserNameView.setText(name);
        mDateView.setText(message.getCreatedAt());
        return view;
    }

}
