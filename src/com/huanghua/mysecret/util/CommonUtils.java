package com.huanghua.mysecret.util;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;
import com.huanghua.mysecret.bean.ApkBean;

public class CommonUtils {
    /** 检查是否有网络 */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /** 检查是否是WIFI */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    /** 检查是否是移动网络 */
    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /** 检查SD卡是否存在 */
    public static boolean checkSdCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    public static Dialog createLoadingDialog(Context context, String msg) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);

        loadingDialog.setCancelable(false);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }

    public static int getCurrentVersion(Context mContext) {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getCurrentVersionName(Context mContext) {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getXmlFromUrl(String url) {
        String xml = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity, "utf-8");
        } catch (Exception e) {
            return "error";
        }
        return xml;
    }

    public static Map<String, ApkBean> parseXml(String xml) {
        XmlPullParser parser = Xml.newPullParser();
        Map<String, ApkBean> mResult = null;
        ApkBean apkBean = null;
        try {
            parser.setInput(new ByteArrayInputStream(xml.getBytes()), "UTF-8");
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    mResult = new HashMap<String, ApkBean>();
                    break;
                case XmlPullParser.START_TAG:
                    String tag_name = parser.getName();
                    if (tag_name != null && tag_name.equals("apk")) {
                        String tag_value = parser.getAttributeValue(0);
                        apkBean = new ApkBean();
                        apkBean.setName(tag_value);
                    } else if (tag_name != null && tag_name.equals("path")) {
                        String tag_text = parser.nextText();
                        apkBean.setPath(tag_text);
                    } else if (tag_name != null
                            && tag_name.equals("versionCode")) {
                        String tag_text = parser.nextText();
                        apkBean.setVersionCode(Integer.parseInt(tag_text));
                    } else if (tag_name != null
                            && tag_name.equals("versionName")) {
                        String tag_text = parser.nextText();
                        apkBean.setVersionName(tag_text);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String tag_name_end = parser.getName();
                    if (tag_name_end != null && tag_name_end.equals("apk")) {
                        mResult.put(apkBean.getName(), apkBean);
                    }
                    break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            return null;
        }
        return mResult;
    }

    public static void showLog(String msg) {
        if (CustomApplcation.DEBUG) {
            Log.i(CustomApplcation.TAG, msg);
        }
    }

    public static void showLog(String tag, String msg) {
        if (CustomApplcation.DEBUG) {
            Log.i(tag, msg);
        }
    }
}
