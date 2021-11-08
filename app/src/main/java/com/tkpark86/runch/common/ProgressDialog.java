package com.tkpark86.runch.common;

import com.tkpark86.runch.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout.LayoutParams;

public class ProgressDialog extends Dialog {
	
	private static final String LOG_TAG = "barcelona";
	
	public ProgressDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		Log.d(LOG_TAG, "ProgressDialog.ProgressDialog:");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "ProgressDialog.onCreate:");
		
		setContentView(R.layout.dialog_progress);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setCanceledOnTouchOutside(false);
		setCancelable(true);
	}
	
	@Override
	public void show() {
		super.show();
		Log.d(LOG_TAG, "ProgressDialog.show:");
	}

	@Override
	public void dismiss() {
		super.dismiss();
		Log.d(LOG_TAG, "ProgressDialog.dismiss:");
	}
	
}
