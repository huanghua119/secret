package com.huanghua.mysecret.adapter.base;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.CommentSupport;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.load.DateLoad;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.BaseActivity;
import com.huanghua.mysecret.util.ImageLoadOptions;
import com.huanghua.mysecret.util.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MyCommentListAdapter extends BaseListAdapter<Comment> {

    private Context mContext;

    public MyCommentListAdapter(Context context, List<Comment> list) {
        super(context, list);
        mContext = context;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.mycomment_item_view, null);
        }
        List<Comment> list = getList();
        final Comment comment = list.get(position);
        User user = comment.getFromUser();
        Secret secret = comment.getSecret();

        ImageView mUserPhoto = (ImageView) view
                .findViewById(R.id.item_comment_photo);
        TextView mUserNameView = (TextView) view
                .findViewById(R.id.item_comment_name);
        setOnInViewClickListener(R.id.item_delete,
                new onInternalClickListener() {

                    @Override
                    public void OnClickListener(View parentV, View v,
                            Integer position, Object values) {
                        BmobQuery<CommentSupport> querycs = new BmobQuery<CommentSupport>();
                        querycs.addWhereEqualTo("comment", comment);
                        querycs.setLimit(1000);
                        querycs.findObjects(mContext, new FindListener<CommentSupport>() {
                            @Override
                            public void onError(int arg0, String arg1) {
                                ShowToast(mContext.getString(R.string.delete_fail));
                            }

                            @Override
                            public void onSuccess(List<CommentSupport> list) {
                                for (CommentSupport cs : list) {
                                    cs.delete(mContext);
                                }
                                comment.delete(mContext, new DeleteListener() {
                                    @Override
                                    public void onSuccess() {
                                        Dialog dialog = new AlertDialog.Builder(
                                                mContext)
                                                .setTitle(R.string.tips)
                                                .setMessage(R.string.delete_confirm)
                                                .setPositiveButton(
                                                        android.R.string.ok,
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                getList().remove(
                                                                        comment);
                                                                notifyDataSetChanged();
                                                            }
                                                        })
                                                .setNegativeButton(
                                                        android.R.string.cancel, null)
                                                .create();
                                        dialog.show();
                                    }

                                    @Override
                                    public void onFailure(int arg0, String arg1) {
                                        ShowToast(mContext.getString(R.string.delete_fail));
                                    }
                                });
                            }
                        });
                    }
                });

        String avatar = user.getAvatar();
        if (avatar != null && !avatar.equals("")) {
            ImageLoader.getInstance().displayImage(avatar, mUserPhoto,
                    ImageLoadOptions.getOptions());
        } else {
            mUserPhoto.setImageResource(R.drawable.user_photo_default);
        }
        mUserNameView.setText(user.getUsername());

        TextView commentContents = (TextView) view
                .findViewById(R.id.item_comment_contents);
        TextView secretContents = (TextView) view
                .findViewById(R.id.item_secret_contents);
        commentContents.setText(comment.getContents());
        secretContents.setText(secret.getContents());

        TextView csView = ViewHolder.get(view, R.id.item_comment_support);
        List<CommentSupport> csList = DateLoad.getCommentSupport(comment
                .getObjectId());
        if (csList == null) {
            setDingTextView(comment, csView);
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
                        final CommentSupport cs = new CommentSupport();
                        cs.setComment(c);
                        cs.setToUser(c.getFromUser());
                        cs.setFromUser(UserManager.getInstance(mContext)
                                .getCurrentUser());
                        cs.save(mContext, new SaveListener() {
                            @Override
                            public void onSuccess() {
                                showLog("save commentSupport success");
                                BmobRelation relation = new BmobRelation();
                                relation.add(cs);
                                c.setCommentSupport(relation);
                                c.update(mContext);
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

        return view;
    }

    private void setDingTextView(final Comment comment, final TextView csView) {
        csView.setSelected(false);
        BmobQuery<CommentSupport> queryCs = new BmobQuery<CommentSupport>();
        queryCs.addWhereRelatedTo("commentSupport", new BmobPointer(comment));
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
