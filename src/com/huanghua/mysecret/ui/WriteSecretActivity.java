package com.huanghua.mysecret.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import cn.bmob.v3.listener.SaveListener;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.LocationUtil;
import com.huanghua.mysecret.view.SupportView;

/***
 * 发布秘密
 * 
 * @author huanghua
 * 
 */
public class WriteSecretActivity extends BaseActivity implements TextWatcher {

    public static final int WRITE_TYPE_SECRET = 1;
    public static final int WRITE_TYPE_COMMENT = 2;

    private int mWriteType = WRITE_TYPE_SECRET;
    private TextView mContentsCountView = null;
    private TextView mTitle = null;
    private EditText mContents;
    private int mContentsCount = 300;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_secret_view);

        mWriteType = getIntent().getIntExtra("write_type", WRITE_TYPE_SECRET);

        mTitle = (TextView) findViewById(R.id.title);
        mContents = (EditText) findViewById(R.id.contents);
        mContents.addTextChangedListener(this);
        mContentsCountView = (TextView) findViewById(R.id.contents_count);
        if (mWriteType == WRITE_TYPE_SECRET) {
            mTitle.setText(R.string.publication_secret);
            mContents.setHint(R.string.write_secret_hint);
        } else if (mWriteType == WRITE_TYPE_COMMENT) {
            mTitle.setText(R.string.publication_comment);
            mContents.setHint(R.string.write_comment_hint);
        }
    }

    public void onPublication(final View v) {
        if (!checkNetwork()) {
            return;
        }
        String content = mContents.getText().toString();
        LocationUtil lu = new LocationUtil(this);
        if (content != null && !"".endsWith(content)) {
            final Dialog dialog = CommonUtils.createLoadingDialog(this, getString(R.string.cominting));
            dialog.show();
            if (mWriteType == WRITE_TYPE_SECRET) {
                Secret s = new Secret();
                s.setContents(content);
                s.setUser(userManager.getCurrentUser());
                s.setStatus(0);
                s.setLocation(lu.findLocation());
                showLog("lu.findLocation ===" + lu.findLocation());
                s.setAddress(lu.getAddress(lu.findLocation()));
                showLog("lu.getAddress ===" + lu.getAddress(lu.findLocation()));
                s.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save secret success ");
                        dialog.dismiss();
                        ShowToastOld(R.string.publication_success);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save secret failure " + arg1);
                        dialog.dismiss();
                        ShowToast(R.string.publication_faile);
                        v.setClickable(true);
                    }
                });
            } else {
                User user = (User) getIntent().getSerializableExtra("toUser");
                Secret secret = (Secret) getIntent().getSerializableExtra(
                        "secret");
                Comment comment = new Comment();
                comment.setContents(content);
                comment.setFromUser(userManager.getCurrentUser());
                comment.setToUser(user);
                comment.setSecret(secret);
                comment.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save comment success ");
                        dialog.dismiss();
                        ShowToastOld(R.string.publication_success);
                        sendBroadcast(new Intent(SupportView.DATE_COMMENT_CHANGER));
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save comment failure " + arg1);
                        dialog.dismiss();
                        ShowToast(R.string.publication_faile);
                        v.setClickable(true);
                    }
                });
            }
            v.setClickable(false);
        }
    }

    public void onCancel(View v) {
        onBackPressed();
    }

    public void onBackPressed(){
        String content = mContents.getText().toString();
        if (content != null && content.length() > 0) {
            showExitConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(mTitle.getText().toString())
                .setMessage(R.string.exit_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                WriteSecretActivity.super.onBackPressed();
                            }
                        }).setCancelable(false).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        mContentsCount += count;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mContentsCount -= count;
    }

    @Override
    public void afterTextChanged(Editable s) {
        mContentsCountView.setText(mContentsCount + "");
    }
}