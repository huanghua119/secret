package com.huanghua.mysecret.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.huanghua.mysecret.ui.BaseActivity;

public class ThemeUtil {

    public static final int THEME_NIGHT = 1;
    public static final int THEME_DURING = 2;
    public static final String THEME_PREFERENCES_KEY = "theme_key";
    public static final String THEME_FINISH_KEY = "theme_finish_key";

    public static int getCurrentTheme(Context context) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(
                "mytheme", Context.MODE_PRIVATE);
        int theme = mSharedPreferences.getInt(THEME_PREFERENCES_KEY,
                THEME_DURING);
        return theme;
    }

    public static void setTheme(Context context, int value) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(
                "mytheme", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(THEME_PREFERENCES_KEY, value);
        editor.commit();
    }

    public static void switchTheme(BaseActivity activity) {
        int theme = getCurrentTheme(activity);
        switch (theme) {
        case ThemeUtil.THEME_NIGHT:
            setTheme(activity, THEME_DURING);
            break;
        case ThemeUtil.THEME_DURING:
            setTheme(activity, THEME_NIGHT);
            break;
        }
        activity.setSwitchTheme(true);
        setThemeFinish(activity, true);
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void setThemeFinish(Context context, boolean value) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(
                "mytheme", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(THEME_FINISH_KEY, value);
        editor.commit();
    }

    public static boolean isThemeFinish(Context context) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(
                "mytheme", Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(THEME_FINISH_KEY, false);
    }
}
