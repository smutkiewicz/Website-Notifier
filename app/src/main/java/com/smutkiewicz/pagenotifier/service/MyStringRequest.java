package com.smutkiewicz.pagenotifier.service;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Admin on 2017-09-14.
 */

public class MyStringRequest {
    private Context context;
    private ResponseInterface responseInterface;

    public interface ResponseInterface {
        void onResponse(String response);
        void onErrorResponse(VolleyError error);
    }

    public MyStringRequest(Context context, ResponseInterface responseInterface) {
        this.context = context;
        this.responseInterface = responseInterface;
    }

    private void startRequestForWebsite(int jobId, String url) {
        RequestQueue requestQueue = initRequestQueue();
        StringRequest stringRequest = createStringRequestForWebsite(jobId, url);
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

    private StringRequest createStringRequestForWebsite(final int jobId, String url) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseInterface.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseInterface.onErrorResponse(error);
                    }
                });
    }
}
