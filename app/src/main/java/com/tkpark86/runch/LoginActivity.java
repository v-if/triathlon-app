package com.tkpark86.runch;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.LogIn_In;
import com.tkpark86.runch.model.LogIn_Res;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Goodruns";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "LoginActivity.onCreate: ");
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.password || id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        ButtonRectangle btnLogin = (ButtonRectangle) findViewById(R.id.btn_login_send);
        btnLogin.setRippleSpeed(36f);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        String email = ComFunc.getMemberEmail(this);
        if(!email.equals("")) {
            mEmailView.setText(email);
            mEmailView.setSelection(email.length());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LoginActivity.onResume: ");
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!ComFunc.isPasswordValidator(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!ComFunc.isEmailValidator(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        //
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        LogIn_In input = new LogIn_In();
        input.email = email;
        final String encodePassword = ComFunc.encodePassword(password);
        input.password = encodePassword;

        // SERVICE_URL + service.do
        String url = ComFunc.getServiceURL("login");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            Log.d(TAG, "attemptLogin: JSONException e=" + e.getMessage());
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        LogIn_Res res = gson.fromJson(response.toString(), LogIn_Res.class);
                        Log.d(TAG, "attemptLogin.onResponse: "+res.returnMessage+"("+res.returnCode+")");

                        if(res.returnCode.equals("000")) {
                            ComFunc.logIn(LoginActivity.this, res.output.member_id, res.output.email, encodePassword, res.output.nickname, res.output.profile_img_url);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(LoginActivity.this)
                                    .setTitle(R.string.notice)
                                    .setMessage(res.returnMessage)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.d(TAG, "onClick: ok");
                                        }
                                    }).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "attemptLogin.onErrorResponse: VolleyError error="+error.getMessage());
            }
        });

        requestQueue.add(jsObjRequest);
    }
}

