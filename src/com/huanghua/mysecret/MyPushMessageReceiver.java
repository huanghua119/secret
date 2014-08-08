package com.huanghua.mysecret;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.bmob.push.PushConstants;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.SaveListener;

import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.bean.PushMessage;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.MyMessageActivity;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;

public class MyPushMessageReceiver extends BroadcastReceiver {

    public static final String TAG = "my_push_message";
    public static final String PUSH_ACTION_MESSAGE = "cn.bmob.push.action.MESSAGE";
    public static final String PUSH_ACTION_SEND_COMMENT = "push_action_send_comment";
    public static final String PUSH_ACTION_NEW_MESSAGE = "push_action_new_comment";

    public static final int NOTIFICATION_MESSAGE_COMMENT = 1;
    private BmobPushManager mBmobPush = null;
    private SharePreferenceUtil mSp = null;
    private NotificationManager mNotificationManager = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (mBmobPush == null) {
            mBmobPush = new BmobPushManager(context);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        CommonUtils.showLog(TAG, "action: " + action);
        if (action != null && PUSH_ACTION_MESSAGE.equals(action)) {
            String json = intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            CommonUtils.showLog(TAG, "json: " + json);
            parseMessage(context, json);
        } else if (action != null && PUSH_ACTION_SEND_COMMENT.equals(action)) {
            final User user = (User) intent.getSerializableExtra("toUser");
            final Comment comment = (Comment) intent
                    .getSerializableExtra("comment");
            int type = intent.getIntExtra("type", PushMessage.PUSH_MESSAGE_TYPE_COMMENT);
            String message = "";
            switch (type) {
            case PushMessage.PUSH_MESSAGE_TYPE_COMMENT:
                message = UserManager.getInstance(context).getCurrentUser().getUsername() + context.getString(R.string.comment_your_secret);
            case PushMessage.PUSH_MESSAGE_TYPE_REPLY_COMMENT:
                message = UserManager.getInstance(context).getCurrentUser().getUsername() + context.getString(R.string.comment_your_comment);
                final String message2 = message;
                PushMessage pm = new PushMessage();
                pm.setToUser(user);
                pm.setType(type);
                pm.setComment(comment);
                pm.save(context, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        BmobQuery<BmobInstallation> query = Installation.getQuery();
                        CommonUtils.showLog(TAG, "user:" + user.getUsername());
                        query.addWhereEqualTo("user", user);
                        mBmobPush.setQuery(query);
                        mBmobPush.pushMessage("<#MESSAGE_COMMENT#>" + message2);
                    }
                    @Override
                    public void onFailure(int arg0, String arg1) {
                    }
                });
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void parseMessage(final Context context, String json) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            String tag = jsonObject.getString("alert");
            CommonUtils.showLog(TAG, "tag: " + tag);
            if (tag != null && tag.startsWith("<#MESSAGE_COMMENT#>")) {
                if (mSp == null) {
                    mSp = CustomApplcation.getInstance().getSpUtil();
                }
                mSp.setNewMessage(true);
                if (CommonUtils.isBackgroundRunning(context)) {
                    String msg = tag.substring(19, tag.length());
                    Notification baseNF = new Notification();
                    Intent intent = new Intent();
                    intent.setClassName(context.getPackageName(), MyMessageActivity.class.getName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pd = PendingIntent.getActivity(context, 0, intent, 0);

                    baseNF.icon = R.drawable.l_email;

                    baseNF.tickerText = msg;

                    baseNF.defaults |= Notification.DEFAULT_SOUND;
                    baseNF.defaults |= Notification.DEFAULT_VIBRATE;
                    baseNF.defaults |= Notification.DEFAULT_LIGHTS;

                    baseNF.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
                    baseNF.flags |= Notification.FLAG_ONGOING_EVENT;
                    baseNF.flags |= Notification.FLAG_AUTO_CANCEL;

                    baseNF.setLatestEventInfo(context, context.getString(R.string.message_tips), msg, pd);

                    mNotificationManager.notify(NOTIFICATION_MESSAGE_COMMENT, baseNF);
                } else {
                    context.sendBroadcast(new Intent(PUSH_ACTION_NEW_MESSAGE));
                }
            }
        } catch (JSONException e) {
            CommonUtils.showLog(TAG, "parseMessage error: " + e.toString());
            e.printStackTrace();
        }
    }
}
