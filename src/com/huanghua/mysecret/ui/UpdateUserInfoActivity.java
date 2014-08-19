package com.huanghua.mysecret.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.huanghua.mysecret.R;

/**
 * UpdateUserInfo
 * 
 * @author huanghua
 * 
 */
public class UpdateUserInfoActivity extends BaseActivity implements
        OnClickListener {

    public static final int UPDATE_TYPE_USER_EMAIL = 1;
    private int mUpdateType = 0;

    private TextView mSave = null;
    private TextView mTitle = null;
    private View mUpdateEmail = null;
    private TextView mEmailAlert = null;
    private EditText mEamil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user_info_view);
        mUpdateType = getIntent().getIntExtra("update_type",
                UPDATE_TYPE_USER_EMAIL);
        init();
    }

    private void init() {
        mTitle = (TextView) findViewById(R.id.title);
        mSave = (TextView) findViewById(R.id.btn_ok);
        mSave.setOnClickListener(this);
        mUpdateEmail = findViewById(R.id.update_email);
        mEmailAlert = (TextView) findViewById(R.id.emial_alert);
        mEamil = (EditText) findViewById(R.id.email);
        if (mUpdateType == UPDATE_TYPE_USER_EMAIL) {
            mUpdateEmail.setVisibility(View.VISIBLE);
            mTitle.setText(R.string.update_email2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == mSave) {
            String email = mEamil.getText().toString();
            if (TextUtils.isEmpty(email)) {
                return;
            }
        }
    }

}
