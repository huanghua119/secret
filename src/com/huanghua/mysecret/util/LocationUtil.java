package com.huanghua.mysecret.util;

import com.huanghua.mysecret.CustomApplcation;

import android.content.Context;
import cn.bmob.v3.datatype.BmobGeoPoint;

public class LocationUtil {
    private Context mContext;

    public LocationUtil(Context context) {
        mContext = context;
    }

    public BmobGeoPoint findLocation() {
        return CustomApplcation.getLocation();
    }

    public String getAddress(BmobGeoPoint bp) {
        return CustomApplcation.getAddress();
    }
}
