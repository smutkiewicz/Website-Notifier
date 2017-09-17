package com.smutkiewicz.pagenotifier.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.*;

public class MyStringRequest {
    private static final String REQUEST_TAG = "Response";

    private Context context;
    private ResponseInterface responseInterface;

    public MyStringRequest(Context context, ResponseInterface responseInterface) {
        this.context = context;
        this.responseInterface = responseInterface;
    }

    public void startRequestForWebsite(String url) {
        RequestQueue requestQueue = initRequestQueue();
        StringRequest stringRequest = createStringRequestForWebsite(url);
        requestQueue.add(stringRequest);
    }

    private RequestQueue initRequestQueue() {
        RequestQueue mRequestQueue;
        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
        return mRequestQueue;
    }

    private StringRequest createStringRequestForWebsite(String url) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(REQUEST_TAG, "Interface: success");
                        responseInterface.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(REQUEST_TAG, "Interface: failed");
                        responseInterface.onErrorResponse(error);
                    }
                });
    }

    public interface ResponseInterface {
        void onResponse(String response);
        void onErrorResponse(VolleyError error);
    }
}
