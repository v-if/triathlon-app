package com.tkpark86.runch;

import org.json.JSONException;
import org.json.JSONObject;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.model.LogIn_In;
import com.tkpark86.runch.model.LogIn_Res;
import com.tkpark86.runch.model.Notice_In;
import com.tkpark86.runch.model.Notice_Out;
import com.tkpark86.runch.model.Notice_Res;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 1500;

    private SplashTask mSplashTask;

    private boolean mIsStop;
    private boolean mIsAutoLogIn;
    private boolean mIsLogInChecking;
    private static boolean mIsNoticeChecking;
    private static boolean mIsExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mIsExit = false;
        notice();

        // Auto Login
        String email = ComFunc.getMemberEmail(SplashActivity.this);
        String password = ComFunc.getMemberPw(SplashActivity.this);
        if(!email.equals("") && !password.equals("")) {
            mIsAutoLogIn = true;
            logIn(email, password);
        }

        mSplashTask = new SplashTask();
        mSplashTask.execute();
    }

    @Override
    public void onBackPressed() {
        if(mSplashTask != null) {
            mIsStop = true;
            mSplashTask.cancel(true);
        }
        super.onBackPressed();
    }

    private String getVersionName() {
        String version = "";
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pkgInfo.versionName;
        } catch (NameNotFoundException e) {
            //Log.d(LOG_TAG, "SplashActivity.getVersionName: NameNotFoundException="+e.getMessage());
        }
        return version;
    }

    public String getPackageName() {
        return getApplicationContext().getPackageName();
    }

    private void notice() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // 검색조건 설정
        Notice_In input = new Notice_In();
        input.notice_id = "";

        String url = "http://vveb5u.cafe24.com/notice/notice.json";

        // json 객체 변환하기
        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(LOG_TAG, "SplashActivity.notice: e.getMessage()="+e.getMessage());
        }

        mIsNoticeChecking = true;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                Notice_Res res = gson.fromJson(response.toString(), Notice_Res.class);

                if(res.returnCode.equals("000")) {
                    Notice_Out output = res.output;

                    // APP상태(0.정상, 1.공지사항, 2.업데이트있음, 3.필수업데이트, 4.점검중)
                    if(output.app_state.equals("0")) {
                        mIsNoticeChecking = false;
                    } else if(output.app_state.equals("1")) {
                        // Show Popup
                        new AlertDialogWrapper.Builder(SplashActivity.this)
                                .setTitle(R.string.notice)
                                .setMessage(output.content)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Log.d(TAG, "onClick: ok");
                                    }
                                }).show();
                    } else if(output.app_state.equals("2")) {
                        String currentVersionName = getVersionName();
                        if(output.version_name.compareTo(currentVersionName) > 0) {
                            // (현재버전:0.8.0, 최신버전:0.8.1)
                            StringBuffer sb = new StringBuffer();
                            sb.append(output.content)
                                    .append("\n")
                                    .append("(현재버전:")
                                    .append(currentVersionName)
                                    .append(", 최신버전:")
                                    .append(output.version_name)
                                    .append(")");
                            // Show Popup
                            new AlertDialogWrapper.Builder(SplashActivity.this)
                                    .setTitle(R.string.notice)
                                    .setMessage(sb.toString())
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mIsNoticeChecking = false;
                                        }
                                    })
                                    .setNegativeButton(R.string.google_play, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mIsExit = true;
                                            finish();

                                            String packageName = getPackageName();
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id=" + packageName));
                                            startActivity(intent);
                                        }
                                    }).show();
                        }
                    } else if(output.app_state.equals("3")) {
                        String currentVersionName = getVersionName();
                        if(output.version_name.compareTo(currentVersionName) > 0) {
                            // (현재버전:0.8.0, 최신버전:0.8.1)
                            StringBuffer sb = new StringBuffer();
                            sb.append(output.content)
                                    .append("\n")
                                    .append("(현재버전:")
                                    .append(currentVersionName)
                                    .append(", 최신버전:")
                                    .append(output.version_name)
                                    .append(")");
                            // Show Popup
                            new AlertDialogWrapper.Builder(SplashActivity.this)
                                    .setTitle(R.string.notice)
                                    .setMessage(sb.toString())
                                    .setNegativeButton(R.string.google_play, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mIsExit = true;
                                            finish();

                                            String packageName = getPackageName();
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("market://details?id=" + packageName));
                                            startActivity(intent);
                                        }
                                    }).show();
                        }
                    } else if(output.app_state.equals("4")) {
                        // Show Popup
                        new AlertDialogWrapper.Builder(SplashActivity.this)
                                .setTitle(R.string.notice)
                                .setMessage(output.content)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Log.d(TAG, "onClick: ok");
                                    }
                                }).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show Popup
                new AlertDialogWrapper.Builder(SplashActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                notice();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: cancel");
                            }
                        })
                        .show();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    private void logIn(String email, String password) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // 검색조건 설정
        LogIn_In input = new LogIn_In();
        input.email = email;
        input.password = password;

        // SERVICE_URL + service.do
        String url = ComFunc.getServiceURL("login");

        // json 객체 변환하기
        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(LOG_TAG, "SplashActivity.logIn: e.getMessage()="+e.getMessage());
        }

        mIsLogInChecking = true;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                LogIn_Res res = gson.fromJson(response.toString(), LogIn_Res.class);

                if(res.returnCode.equals("000")) {
                    String nickname = res.output.nickname;
                    String profileImgUrl = res.output.profile_img_url;
                    ComFunc.setMemberNickname(SplashActivity.this, nickname);
                    ComFunc.setMemberProfileImgUrl(SplashActivity.this, profileImgUrl);
                    mIsLogInChecking = false;
                } else {
                    // Show Popup
                    new AlertDialogWrapper.Builder(SplashActivity.this)
                            .setTitle(R.string.notice)
                            .setMessage(res.returnMessage)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Log.d(TAG, "onClick: ok");
                                }
                            }).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.d(LOG_TAG, "SplashActivity.logIn().new ErrorListener() {...}.onErrorResponse: error="+error.getMessage());
            }
        });

        requestQueue.add(jsObjRequest);
    }

    public class SplashTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                int time = 0;
                while(mIsExit || mIsNoticeChecking || mIsLogInChecking || SPLASH_TIME > time) {
                    Thread.sleep(100);
                    time += 100;
                }
            } catch (InterruptedException e) {
                //Log.d(LOG_TAG, "SplashActivity.SplashTask.doInBackground: InterruptedException e="+e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mSplashTask = null;

            if(result) {
                if(!mIsStop) {
                    // auto login
                    if(mIsAutoLogIn) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, StartActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mSplashTask = null;
        }
    }
}
