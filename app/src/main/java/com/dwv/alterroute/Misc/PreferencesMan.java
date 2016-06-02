package com.dwv.alterroute.Misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

/**
 * Created by walterjgsp on 02/06/16.
 */
public class PreferencesMan {

    private static final String TAG ="PreferencesMan";

    public static final String MyPREFERENCES = "AlterRoutePreferences" ;
    public static final String LastLatitude="LastLatitude";
    public static final String LastLongitude="LastLongitude";

    private SharedPreferences sharedpreferences;
    private Context mContext;

    public PreferencesMan(Context mContext){
        this.mContext=mContext;
        sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
    }


    public Location getLocation(){
        Location lastLocation = new Location("userLocation");
        lastLocation.setLatitude(sharedpreferences.getFloat(LastLatitude, 0));
        lastLocation.setLongitude(sharedpreferences.getFloat(LastLongitude, 0));
        return lastLocation;
    }

    public void setLocation(Location loc){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat(LastLatitude,(float)loc.getLatitude());
        editor.putFloat(LastLongitude,(float)loc.getLongitude());
        editor.commit();
    }
}
