package com.huanghua.mysecret.load;

import android.content.Context;
import android.os.Handler;

import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.util.CommonUtils;

public class DateLoad {

    private static final String TAG = "date_load";

    public interface OnDateLoadCompleteListener {
        public void OnDateLoadComplete();
    }

    public DateLoad() {
    }

    public static void loadDate(Context context,
            OnDateLoadCompleteListener listener, Handler handler,
            Secret secret, long priority) {

        DateLoadTask task = new DateLoadTask(context, listener, handler,
                secret, priority);
        // new Thread(task).start();
        DateLoadThreadManager.submitTask(secret.getObjectId(), task);
        CommonUtils.showLog(TAG, "execute a ImageLoadTask !");
    }

}
