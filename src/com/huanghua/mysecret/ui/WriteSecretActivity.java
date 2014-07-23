package com.huanghua.mysecret.ui;

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
    private int mContentsCount = 140;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_secret_view);

        mWriteType = getIntent().getIntExtra("write_type", WRITE_TYPE_SECRET);

        mTitle = (TextView) findViewById(R.id.title);
        if (mWriteType == WRITE_TYPE_SECRET) {
            mTitle.setText(R.string.publication_secret);
        } else if (mWriteType == WRITE_TYPE_COMMENT) {
            mTitle.setText(R.string.publication_comment);
        }
        mContents = (EditText) findViewById(R.id.contents);
        mContents.addTextChangedListener(this);
        mContentsCountView = (TextView) findViewById(R.id.contents_count);
    }

    public void onPublication(View v) {
        String content = mContents.getText().toString();
        if (content != null && !"".endsWith(content)) {
            if (mWriteType == WRITE_TYPE_SECRET) {
                Secret s = new Secret();
                s.setContents(content);
                s.setUser(userManager.getCurrentUser());
                s.setStatus(0);
                s.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        showLog("save secret success ");
                        ShowToastOld(R.string.publication_success);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2000);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save secret failure " + arg1);
                        ShowToast(R.string.publication_faile);
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
                        ShowToastOld(R.string.publication_success);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2000);
                    }

                    @Override
                    public void onFailure(int arg0, String arg1) {
                        showLog("save comment failure " + arg1);
                        ShowToast(R.string.publication_faile);
                    }
                });
            }
        }
    }

    public void onCancel(View v) {
        onBackPressed();
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
