package com.dwv.alterroute.Misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by walterjgsp on 16/06/16.
 */
public class VolleySingleton {

    private RequestQueue mRequestQueue;
    private static Context mCtx;
    private static VolleySingleton mInstance;

    private VolleySingleton(Context context)
    {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
    public  void addToRequestQueue(Request req) {
        getRequestQueue().add(req);
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }
}
