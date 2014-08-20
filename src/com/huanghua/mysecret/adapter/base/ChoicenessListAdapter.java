package com.huanghua.mysecret.adapter.base;

import java.text.DecimalFormat;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.PushMessage;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.load.DateLoad;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.LocationUtil;
import com.huanghua.mysecret.util.ViewHolder;
import com.huanghua.mysecret.view.DateTextView;
import com.huanghua.mysecret.view.SupportView;

public class ChoicenessListAdapter extends BaseListAdapter<Secret> {

    private boolean mNearSecret = false;
    private boolean mMySecret = false;
    protected Handler mMainThreadHandler;
    private SparseArray<SupportView> mAllSupportView = new SparseArray<SupportView>();
    private DateLoad.OnDateLoadCompleteListener mDateLoadListener = new DateLoad.OnDateLoadCompleteListener() {

        public void OnLoadSecretSupportComplete(int position,
                List<SecretSupport> list) {
            SupportView sv = mAllSupportView.get(position);
            sv.setSecretSupportList(list);
            sv.refresh();
        }

        @Override
        public void OnLoadSecretCommentComplete(int position, int count) {
            SupportView sv = mAllSupportView.get(position);
            sv.setCommentCount(count);
        }
    };

    public ChoicenessListAdapter(Context context, List<Secret> list) {
        super(context, list);
        mMainThreadHandler = new Handler(context.getApplicationContext()
                .getMainLooper());
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.secret_item_view, null);
        }
        List<Secret> list = getList();
        final Secret secret = list.get(position);
        User user = secret.getUser();
        ImageView mPhoto = ViewHolder.get(view, R.id.item_photo);
        TextView mName = ViewHolder.get(view, R.id.item_name);
        TextView mLocation = ViewHolder.get(view, R.id.item_location);
        DateTextView mDate = ViewHolder.get(view, R.id.item_date);
        mDate.setInitDate(secret.getCreatedAt());
        TextView mContents = ViewHolder.get(view, R.id.item_contents);
        ImageView mDeleteView = ViewHolder.get(view, R.id.item_delete);
        mPhoto.setImageResource(CommonUtils.HEAD_RESOURS[secret.getRandomHead()]);

        mContents.setText(secret.getContents());
        mName.setText(user.getUsername());

        TextView mDistance = ViewHolder.get(view, R.id.item_distance);
        if (mNearSecret && secret.getLocation() != null) {
            double m = LocationUtil.gps2m(secret.getLocation().getLatitude(),
                    secret.getLocation().getLongitude(), CustomApplcation
                            .getLocation().getLatitude(), CustomApplcation
                            .getLocation().getLongitude());
            if (m > 1000) {
                DecimalFormat fnum = new DecimalFormat("##0.0");
                String dd = fnum.format(m / 1000);
                mDistance.setText(dd + mContext.getString(R.string.kilometer));
            } else {
                mDistance.setText((int) m + mContext.getString(R.string.meter));
            }
            mDistance.setVisibility(View.VISIBLE);
        } else {
            mDistance.setVisibility(View.GONE);
        }
        if (mMySecret) {
            mLocation.setVisibility(View.GONE);
            mDeleteView.setVisibility(View.VISIBLE);
            setOnInViewClickListener(R.id.item_delete,
                    new onInternalClickListener() {
                        @Override
                        public void OnClickListener(View parentV, View v,
                                Integer position, Object values) {
                            Dialog dialog = new AlertDialog.Builder(mContext)
                                    .setTitle(R.string.tips)
                                    .setMessage(R.string.delete_confirm)
                                    .setPositiveButton(
                                            android.R.string.ok,
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    deleteSecret(secret);
                                                }
                                            })
                                    .setNegativeButton(android.R.string.cancel,
                                            null).create();
                            dialog.show();
                        }
                    });
        } else {
            mLocation.setVisibility(View.VISIBLE);
            mDeleteView.setVisibility(View.GONE);
        }
        mLocation.setText(secret.getAddress());

        Drawable drawable = mContext.getResources().getDrawable(
                user.isSex() ? R.drawable.man : R.drawable.women);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        mName.setCompoundDrawables(drawable, null, null, null);

        final SupportView sv = ViewHolder.get(view, R.id.item_bottom);
        List<SecretSupport> allss = DateLoad.get(secret.getObjectId());
        if (allss == null) {
            mAllSupportView.put(position, sv);
            sv.setSecret(secret, mDateLoadListener, mMainThreadHandler,
                    position);
        } else {
            sv.refreshInCache(secret, allss);
            Integer commentlist = DateLoad.getComment(secret.getObjectId());
            sv.setCommentCount(commentlist == null ? 0 : commentlist);
        }
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

    public void setNearSecret(boolean nearSecret) {
        mNearSecret = nearSecret;
    }

    public void setMySecret(boolean mySecret) {
        mMySecret = mySecret;
    }

    private void deleteSecret(final Secret secret) {

        final BmobQuery<SecretSupport> querySs = new BmobQuery<SecretSupport>();
        querySs.addWhereEqualTo("secret", secret);
        querySs.setLimit(1000);
        querySs.findObjects(mContext, new FindListener<SecretSupport>() {
            @Override
            public void onError(int arg0, String arg1) {
                ShowToast(mContext.getString(R.string.delete_fail));
            }

            @Override
            public void onSuccess(List<SecretSupport> list) {
                for (SecretSupport ss : list) {
                    ss.delete(mContext);
                }
                BmobQuery<PushMessage> querypm = new BmobQuery<PushMessage>();
                querypm.include("comment.secret");
                BmobQuery<Comment> querypc = new BmobQuery<Comment>();
                querypc.addWhereEqualTo("secret", secret);
                querypm.addWhereMatchesQuery("comment", Comment.class.getSimpleName(),
                        querypc);
                querypm.findObjects(mContext, new FindListener<PushMessage>() {
                    @Override
                    public void onError(int arg0, String arg1) {
                    }

                    @Override
                    public void onSuccess(List<PushMessage> list) {
                        for (PushMessage cs : list) {
                            cs.delete(mContext);
                        }
                    }
                });
                secret.delete(mContext, new DeleteListener() {
                    @Override
                    public void onSuccess() {
                        getList().remove(secret);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        ShowToast(mContext.getString(R.string.delete_fail));
                    }
                });
            }
        });

        /*
        final BmobQuery<Comment> queryComment = new BmobQuery<Comment>();
        queryComment.addWhereEqualTo("secret", secret);
        BmobQuery<CommentSupport> querycs = new BmobQuery<CommentSupport>();
        querycs.addWhereMatchesQuery("comment", Comment.class.getSimpleName(),
                queryComment);
        querycs.setLimit(1000);
        querycs.findObjects(mContext, new FindListener<CommentSupport>() {
            @Override
            public void onError(int arg0, String arg1) {
                ShowToast(mContext.getString(R.string.delete_fail));
            }

            @Override
            public void onSuccess(List<CommentSupport> list) {
                showLog("delete_mysecret", "CommentSupport list:" + list.size());
                for (CommentSupport cs : list) {
                    cs.delete(mContext);
                }
                queryComment.findObjects(mContext, new FindListener<Comment>() {
                    @Override
                    public void onSuccess(List<Comment> list) {
                        showLog("delete_mysecret", "list:" + list.size());
                        for (Comment c : list) {
                            c.delete(mContext);
                        }
                        secret.delete(mContext, new DeleteListener() {
                            @Override
                            public void onSuccess() {
                                getList().remove(secret);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(int arg0, String arg1) {
                                ShowToast(mContext
                                        .getString(R.string.delete_fail));
                            }
                        });
                    }

                    @Override
                    public void onError(int arg0, String arg1) {
                        ShowToast(mContext.getString(R.string.delete_fail));
                    }
                });
                querySs.findObjects(mContext,
                        new FindListener<SecretSupport>() {
                            @Override
                            public void onError(int arg0, String arg1) {

                            }

                            @Override
                            public void onSuccess(List<SecretSupport> list) {
                                for (SecretSupport ss : list) {
                                    ss.delete(mContext);
                                }
                            }
                        });
            }
        });
         */
    }
}
