package com.tkpark86.runch.common;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

public class RaceJsonObjectRequest extends JsonObjectRequest {
	
	private static final String LOG_TAG = "barcelona";
	
	private static final int TIMEOUT_MS = 5000;
	private static final int MAX_RETRIES = 3;
	private static final float BACKOFF_MULT = 1.0f;
	
	private static ProgressDialog mDialog;
	
	public RaceJsonObjectRequest(Context context, int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
		Log.d(LOG_TAG, "RaceJsonObjectRequest.RaceJsonObjectRequest: ");
		
		// set Retry Policy
		setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS
											, MAX_RETRIES
											, BACKOFF_MULT));
		
		// show
		if(mDialog != null) {
			try {
				mDialog.dismiss();
				mDialog = null;
			} catch(Exception e) {
				Log.d(LOG_TAG, "RaceJsonObjectRequest.RaceJsonObjectRequest: dismiss error="+e.getMessage());
			}
		}
		if(mDialog == null) {
			mDialog = new ProgressDialog(context);
			mDialog.show();
		}
	}

	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		// dismiss
		if(mDialog != null) {
			if(mDialog.isShowing()) {
				mDialog.dismiss();
				mDialog = null;
			}
		}
		Log.d(LOG_TAG, "RaceJsonObjectRequest.parseNetworkResponse:");
		return super.parseNetworkResponse(response);
	}

	@Override
	protected VolleyError parseNetworkError(VolleyError volleyError) {
		Log.d(LOG_TAG, "RaceJsonObjectRequest.parseNetworkError: error="+volleyError.getMessage());
		// dismiss
		if(mDialog != null) {
			if(mDialog.isShowing()) {
				mDialog.dismiss();
				mDialog = null;
			}
		}
		return super.parseNetworkError(volleyError);
	}
	
}
