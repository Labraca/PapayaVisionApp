package com.example.papayavision.DBUtilities;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton sInstance = null;
    private RequestQueue mRequestQue;

    private VolleySingleton(Context context){
        mRequestQue = Volley.newRequestQueue(context);
    }

    public static VolleySingleton getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VolleySingleton(context);
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQue;
    }

}