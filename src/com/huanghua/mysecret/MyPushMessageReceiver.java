package com.huanghua.mysecret;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import cn.bmob.push.BmobPushMessageReceiver;

import com.huanghua.mysecret.util.CommonUtils;

public class MyPushMessageReceiver extends BmobPushMessageReceiver {

    private static final String TAG = "my_push_message";
    public static final String PUSH_ACTION_MESSAGE = "cn.bmob.pushservice.action.MESSAGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        CommonUtils.showLog(TAG, "action: " + action);
        if (action != null && PUSH_ACTION_MESSAGE.equals(action)) {
            String json = intent.getStringExtra("msg");
            CommonUtils.showLog(TAG, "json: " + json);
            parseMessage(context, json);
        }
    }

    @Override
    public void onMessage(Context arg0, String arg1) {
        CommonUtils.showLog(TAG, "onMessage: " + arg1);
    }

    private void parseMessage(final Context context, String json) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            jsonObject = jsonObject.getJSONObject("msg");
            String tag = jsonObject.getString("alert");
            CommonUtils.showLog(TAG, "tag: " + tag);
        } catch (JSONException e) {
            CommonUtils.showLog(TAG, "parseMessage error: " + e.toString());
            e.printStackTrace();
        }
    }
}
