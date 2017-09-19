package com.smutkiewicz.pagenotifier.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.*;

public class MyStringRequest {
    private static final String REQUEST_TAG = "Response";

    private Context mContext;
    private ResponseInterface mResponseInterface;

    public MyStringRequest(Context context, ResponseInterface responseInterface) {
        this.mContext = context;
        this.mResponseInterface = responseInterface;
    }

    public void startRequestForWebsite(String url) {
        initMyRequestQueue();
        StringRequest stringRequest = createStringRequestForWebsite(url);
        addToMyRequestQueue(stringRequest);
    }

    private RequestQueue initMyRequestQueue() {
        return MyRequestQueue.getInstance(mContext).getRequestQueue();
    }

    private StringRequest createStringRequestForWebsite(String url) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(REQUEST_TAG, "Interface: success");
                        mResponseInterface.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(REQUEST_TAG, "Interface: failed");
                        mResponseInterface.onErrorResponse(error);
                    }
                });
    }

    private void addToMyRequestQueue(StringRequest stringRequest) {
        MyRequestQueue.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public interface ResponseInterface {
        void onResponse(String response);
        void onErrorResponse(VolleyError error);
    }
}
