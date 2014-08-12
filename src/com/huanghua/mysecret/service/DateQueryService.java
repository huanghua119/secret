package com.huanghua.mysecret.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import cn.bmob.push.PushConstants;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.ValueEventListener;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.ApkBean;
import com.huanghua.mysecret.bean.Comment;
import com.huanghua.mysecret.bean.Installation;
import com.huanghua.mysecret.bean.PushMessage;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.User;
import com.huanghua.mysecret.config.Config;
import com.huanghua.mysecret.manager.UserManager;
import com.huanghua.mysecret.ui.MyMessageActivity;
import com.huanghua.mysecret.util.CommonUtils;
import com.huanghua.mysecret.util.SharePreferenceUtil;

public class DateQueryService extends Service {

    public static final String TAG = "query_service";

    public static final int NOTIFICATION_MESSAGE_COMMENT = 1;
    public static final String PUSH_ACTION_MESSAGE = "cn.bmob.push.action.MESSAGE";
    public static final String PUSH_ACTION_SEND_COMMENT = "push_action_send_comment";
    public static final String PUSH_ACTION_NEW_MESSAGE = "push_action_new_comment";
    private BmobPushManager mBmobPush = null;
    private SharePreferenceUtil mSp = null;
    private NotificationManager mNotificationManager = null;

    private BroadcastReceiver mPushMessageReceiver = new BroadcastReceiver() {
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
    };

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

    @Override
    public IBinder onBind(Intent intent) {
        CommonUtils.showLog(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        CommonUtils.showLog(TAG, "onCreate");
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(PUSH_ACTION_MESSAGE);
        mFilter.addAction(PUSH_ACTION_SEND_COMMENT);
        registerReceiver(mPushMessageReceiver, mFilter);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (CommonUtils.isNetworkAvailable(DateQueryService.this)) {
                    queryNewSecret();
                }
            }
        }, 0, QUERY_PERIOD_TIME);
        queryNewSecret2();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        CommonUtils.showLog(TAG, "onDestroy");
        mTimer.cancel();
        unregisterReceiver(mPushMessageReceiver);
        mRtd.unsubTableUpdate(Secret.class.getSimpleName());
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CommonUtils.showLog(TAG, "onStartCommand");
        if (mBmobPush == null) {
            mBmobPush = new BmobPushManager(this);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mSp == null) {
            mSp = CustomApplcation.getInstance().getSpUtil();
        }
        startCheckNewVersion();
        BmobQuery<BmobInstallation> query = Installation.getQuery();
        query.addWhereEqualTo("installationId", BmobInstallation.getCurrentInstallation(this).getInstallationId());
        query.setLimit(1);
        query.findObjects(this, new FindListener<BmobInstallation>() {
            @Override
            public void onSuccess(List<BmobInstallation> list) {
                if (list.size() > 0) {
                    String objectId = list.get(0).getObjectId();
                    SharePreferenceUtil mSp = CustomApplcation.getInstance().getSpUtil();
                    mSp.setInstallationObjectId(objectId);
                }
            }
             @Override
            public void onError(int arg0, String arg1) {
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    private Timer mTimer = null;
    private BmobQuery<Secret> mQuerySecret = null;
    public static String mLastSecretId = null;
    public static boolean sHasNewSecret = false;
    public static int sSecretCount = 0;
    public static final String QUERY_NEW_SECRTE_ACTION = "query_new_secret_action";
    public static final String CHECK_NEW_VERSION_UPDATE = "check_new_version_update";
    private static final int QUERY_PERIOD_TIME = 10 * 1000 * 60;
    private BmobRealTimeData mRtd = null;
    private FindListener<Secret> mQuerySecretListener = new FindListener<Secret>() {
        @Override
        public void onError(int arg0, String arg1) {
        }

        @Override
        public void onSuccess(List<Secret> arg0) {
            if (arg0.size() != 0) {
                Secret secret = arg0.get(0);
                if (sHasNewSecret) {
                    mLastSecretId = secret.getObjectId();
                    return;
                }
                sHasNewSecret = false;
                CommonUtils.showLog(TAG, "mLastSecretId:" + mLastSecretId + " newSecretId:" + secret.getObjectId());
                if (mLastSecretId != null) {
                    if (!secret.getObjectId().equals(mLastSecretId)) {
                        sHasNewSecret = true;
                    }
                }
                mLastSecretId = secret.getObjectId();
                if (sHasNewSecret) {
                    sendBroadcast(new Intent(QUERY_NEW_SECRTE_ACTION));
                }
            }
        }
    };
    private CountListener mQuerySecretCountListener = new CountListener() {

        @Override
        public void onSuccess(int count) {
            sSecretCount = count;
        }

        @Override
        public void onFailure(int arg0, String arg1) {

        }
    };

    private void queryNewSecret() {
        CommonUtils.showLog(TAG, "queryNewSecret");
        if (mQuerySecret == null) {
            mQuerySecret = new BmobQuery<Secret>();
            mQuerySecret.order("-createdAt");
            mQuerySecret.addWhereGreaterThanOrEqualTo("commentCount",
                    Integer.parseInt(getString(R.string.def_top_sercet_count)));
            mQuerySecret.setCachePolicy(CachePolicy.NETWORK_ONLY);
        }
        mQuerySecret.findObjects(this, mQuerySecretListener);
        mQuerySecret.count(this, Secret.class, mQuerySecretCountListener);
    }

    private void startCheckNewVersion() {
        if (isCheckedVersion()) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String xml = CommonUtils
                            .getXmlFromUrl(Config.UPDATE_XML_PATH);
                    boolean success = false;
                    ApkBean apk = null;
                    if (xml != null && !xml.equals("") && !xml.equals("error")) {
                        Map<String, ApkBean> result = CommonUtils.parseXml(xml);
                        if (result != null) {
                            success = true;
                            apk = result.get("secret");
                        }
                    }
                    if (success) {
                        if (!CommonUtils.getCurrentVersionName(
                                DateQueryService.this).equals(
                                apk.getVersionName())) {
                            Intent intent = new Intent(CHECK_NEW_VERSION_UPDATE);
                            intent.putExtra("apk", apk);
                            sendBroadcast(intent);
                            setCheckVersionDate();
                        }
                    }
                }
            }, 3000);
        }
    }

    private boolean isCheckedVersion() {
        SharedPreferences mSharedPreferences = getSharedPreferences("mysecret",
                Context.MODE_PRIVATE);
        String date = mSharedPreferences.getString("check_last_date", "");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String newDate = month + ":" + day;
        if (date.equals("") || !newDate.equals(date)) {
            return true;
        }
        return false;
    }

    private void setCheckVersionDate() {
        SharedPreferences mSharedPreferences = getSharedPreferences("mysecret",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        editor.putString("check_last_date", month + ":" + day);
        editor.commit();
    }

    private void queryNewSecret2() {
        mRtd = new BmobRealTimeData();
        mRtd.start(this, new ValueEventListener() {
            @Override
            public void onDataChange(JSONObject arg0) {
                CommonUtils.showLog(TAG, "onDataChange arg0:" +  arg0);
                JSONObject jsonObject = arg0;
                try {
                    String tag = jsonObject.getString("data");
                    CommonUtils.showLog(TAG, "onDataChange tag:" +  tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onConnectCompleted() {
                if (mRtd.isConnected()) {
                    mRtd.subTableUpdate(Secret.class.getSimpleName());
                }
            }
        });
        if (mRtd.isConnected()) {
            mRtd.subTableUpdate(Secret.class.getSimpleName());
        }
    }
}
