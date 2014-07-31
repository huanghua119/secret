package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.CommentSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.load.DateLoad;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.util.ViewHolder;
import com.huanghua.mysecret.view.xlist.XListView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommentListAdapter extends BaseListAdapter<Comment> {

    private XListView mListView;

    public CommentListAdapter(Context context, List<Comment> list, XListView listView) {
        super(context, list);
        mListView = listView;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.comment_item_view, null);
        }
        Comment coment = (Comment) getList().get(position);

        User user = coment.getFromUser();
        ImageView mPhoto = ViewHolder.get(view, R.id.item_comment_photo);
        TextView mName = ViewHolder.get(view, R.id.item_comment_name);
        TextView mContents = ViewHolder.get(view, R.id.item_comment_contents);

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
        mName.setText(user.getUsername() + mContext.getString(R.string.say));

        TextView csView = ViewHolder.get(view, R.id.item_comment_support);
        List<CommentSupport> csList = DateLoad.getCommentSupport(coment.getObjectId());
        if (csList == null) {
            setDingTextView(coment, csView);
        } else {
            int count = csList.size();
            csView.setSelected(false);
            for (CommentSupport cs : csList) {
                if (cs.getFromUser().equals(
                        UserManager.getInstance(mContext).getCurrentUser())) {
                    csView.setSelected(true);
                    csView.setClickable(false);
                    break;
                }
            }
            if (count == 0) {
                csView.setText(R.string.add_one);
            } else {
                csView.setText("" + count);
            }
        }
        setOnInViewClickListener(R.id.item_comment_support,
                new onInternalClickListener() {
                    @Override
                    public void OnClickListener(View parentV, View v,
                            Integer position, Object values) {
                        if (((BaseActivity) mContext).checkUserLogin()) {
                            return;
                        }
                        final TextView vv = (TextView) v;
                        final Comment c = (Comment) values;
                        CommentSupport cs = new CommentSupport();
                        cs.setComment(c);
                        cs.setToUser(c.getFromUser());
                        cs.setFromUser(UserManager.getInstance(mContext)
                                .getCurrentUser());
                        cs.save(mContext, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                showLog("save commentSupport success");
                                vv.setSelected(true);
                                vv.setClickable(false);
                                setDingTextView(c, vv);
                            }

                            @Override
                            public void onFailure(int arg0, String arg1) {
                                showLog("save commentSupport failure");
                                vv.setClickable(true);
                            }
                        });
                        vv.setClickable(false);
                    }
                });
        View noMoreView = ViewHolder.get(view, R.id.item_no_more);
        if (!mListView.getPullLoadEnable() && position == (getCount() - 1)) {
            noMoreView.setVisibility(View.VISIBLE);
        } else {
            noMoreView.setVisibility(View.GONE);
        }
        return view;
    }

    private void setDingTextView(final Comment comment, final TextView csView) {
        csView.setSelected(false);
        BmobQuery<CommentSupport> queryCs = new BmobQuery<CommentSupport>();
        queryCs.addWhereEqualTo("comment", comment);
        queryCs.findObjects(mContext, new FindListener<CommentSupport>() {
            @Override
            public void onSuccess(List<CommentSupport> arg0) {
                DateLoad.updateCommentSupport(comment.getObjectId(), arg0);
                int count = arg0.size();
                for (CommentSupport cs : arg0) {
                    if (cs.getFromUser().equals(
                            UserManager.getInstance(mContext).getCurrentUser())) {
                        csView.setSelected(true);
                        csView.setClickable(false);
                        break;
                    }
                }
                if (count == 0) {
                    csView.setText(R.string.add_one);
                } else {
                    csView.setText("" + arg0.size());
                }
            }

            @Override
            public void onError(int arg0, String arg1) {

            }
        });
    }
}
