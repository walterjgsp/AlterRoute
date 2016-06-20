package com.dwv.alterroute.Data;

import android.location.Location;
import android.util.Log;

/**
 * Created by walterjgsp on 20/06/16.
 */
public class GeoINFO {

    public static final String TAG = "GeoINFO";

    private double lat;
    private double lng;
    private double elevation;

    public GeoINFO(){};

    public GeoINFO(double lat, double lng, double eleve){
        this.lat=lat;
        this.lng=lng;
        this.elevation=eleve;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double inclinationTo(GeoINFO g2){
        double incl = 0;

        Location loc1 = new Location("Actual");
        loc1.setLatitude(this.lat);
        loc1.setLongitude(this.lng);

        Location loc2 = new Location("To");
        loc2.setLatitude(g2.getLat());
        loc2.setLongitude(g2.getLng());

        double distanceInMetters = loc1.distanceTo(loc2);
        double high = Math.abs(g2.getElevation()-this.elevation);

        incl = 100*(high/distanceInMetters);

        Log.d(TAG,"Distance: "+distanceInMetters+" high: "+high+" Atan: "+Math.toDegrees(high/distanceInMetters)+" Incl: "+incl);

        return incl;
    }

    //Reference to the distanceTo
    //https://developer.android.com/reference/android/location/Location.html#distanceTo%28android.location.Location%29
}
