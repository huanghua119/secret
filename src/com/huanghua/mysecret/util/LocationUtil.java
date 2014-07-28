package com.huanghua.mysecret.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.huanghua.mysecret.R;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import cn.bmob.v3.datatype.BmobGeoPoint;

public class LocationUtil {
    private double latitude = 0.0;
    private double longitude = 0.0;
    private static Context mContext;

    public LocationUtil(Context context) {
        mContext = context;
    }

    public BmobGeoPoint findLocation() throws Exception {
        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                return new BmobGeoPoint(longitude, latitude);
            }
        }
        return null;
    }

    public String getAddress(BmobGeoPoint bp) {
        if (bp == null) {
            return mContext.getResources().getString(R.string.unknown_address);
        }
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    bp.getLatitude(), bp.getLongitude(), 1);
            StringBuilder sb = new StringBuilder();
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                // for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                // sb.append(address.getAddressLine(i)).append("\n");
                // }
                // sb.append("---");
                // sb.append(address.getCountryName()).append("\n");
                sb.append(address.getLocality());
                // sb.append(address.getSubLocality()).append("\n");
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mContext.getResources().getString(R.string.unknown_address);
    }
}
