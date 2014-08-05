package com.huanghua.mysecret.ui;

import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.huanghua.mysecret.manager.UserManager.UserManagerListener;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.ThemeUtil;

public class UserRegisterActivity extends BaseActivity implements
        OnClickListener {

    private Button mCommit;
    private EditText mUserName;
    private EditText mPass;
    private EditText mPassTwo;
    private RadioButton mRadioMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = ThemeUtil.getCurrentTheme(this);
        switch (theme) {
        case ThemeUtil.THEME_NIGHT:
            setContentView(R.layout.user_register_view_purple);
            break;
        case ThemeUtil.THEME_DURING:
            setContentView(R.layout.user_register_view);
            break;
        default:
            setContentView(R.layout.user_register_view);
            break;
        }
        init();
    }

    private void init() {
        mCommit = (Button) findViewById(R.id.commit_register);
        mCommit.setOnClickListener(this);
        mUserName = (EditText) findViewById(R.id.userName);
        mPass = (EditText) findViewById(R.id.pass);
        mPassTwo = (EditText) findViewById(R.id.twopass);
        mRadioMan = (RadioButton) findViewById(R.id.six_man);
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
        if (v == mCommit) {
            startRegister();
        }
    }

    private void startRegister() {
        String name = mUserName.getText().toString();
        String password = mPass.getText().toString();
        String pwd_again = mPassTwo.getText().toString();

        if (TextUtils.isEmpty(name)) {
            ShowToast(R.string.namenotnull);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            ShowToast(R.string.passnotnull);
            return;
        } else if (password.length() < 6) {
            ShowToast(R.string.pass_so_easy);
            return;
        }
        if (!pwd_again.equals(password)) {
            ShowToast(R.string.twopassnotpass);
            return;
        }

        boolean isNetConnected = CommonUtils.isNetworkAvailable(this);
        if (!isNetConnected) {
            ShowToast(R.string.no_conn_network);
            return;
        }

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.beingRegister));
        progress.setCanceledOnTouchOutside(false);
        progress.show();
        User bu = new User();
        bu.setUsername(name);
        bu.setPassword(password);
        bu.setSex(mRadioMan.isChecked() ? true : false);
        userManager.signUp(bu, new UserManagerListener() {
            @Override
            public void onSuccess(User u) {
                progress.dismiss();
                ShowToastOld(R.string.register_succes);
                startActivity(new Intent(UserRegisterActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(int arg0, String arg1) {
                String str = getString(R.string.registerFail);
                if (arg0 == 202) {
                    str = getString(R.string.register_fail_username);
                }
                progress.dismiss();
                ShowToast(str);
            }
        });
    }
}
