package com.dwv.alterroute.Misc;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dwv.alterroute.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by walterjgsp on 15/06/16.
 */
public abstract class GetElevation {

    public static final String TAG = "GetElevation";

    public static void getElevation(String requisition, Context mContext) throws MalformedURLException {
        StringBuffer uri = new StringBuffer();
        uri.append("https://api.mapbox.com/v4/surface/mapbox.mapbox-terrain-v1.json?layer=contour&fields=ele&points=");
        uri.append(requisition);
        uri.append("&access_token=");
        uri.append(mContext.getResources().getString(R.string.accessToken));

        URL url = new URL(uri.toString());

        //URL url = new URL("https://api.mapbox.com/v4/surface/mapbox.mapbox-terrain-v1.json?layer=contour&fields=ele&points=-116.64267,36.23935;-116.64898,36.24107&access_token=pk.eyJ1Ijoid2FsdGVyamdzcCIsImEiOiJjaW83NzEwdnIwMm5jdnBrajB5Ym5iZmlmIn0.mwJPij-Zrh4R5y1wd-3CVw");

        RequestQueue queue = Volley.newRequestQueue(mContext);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.e(TAG,"Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"That didn't work!");
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
