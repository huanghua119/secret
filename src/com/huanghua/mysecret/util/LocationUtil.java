package com.huanghua.mysecret.util;

import com.huanghua.mysecret.CustomApplcation;
import com.huanghua.mysecret.R;

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

    public boolean isValidLocation() {
        if (findLocation() == null
                || getAddress(findLocation()).equals(
                        mContext.getString(R.string.unknown_address))) {
            return false;
        }
        return true;
    }

    private static final double EARTH_RADIUS = 6378137.0;

    public static double gps2m(double lat_a, double lng_a, double lat_b,
            double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

}
