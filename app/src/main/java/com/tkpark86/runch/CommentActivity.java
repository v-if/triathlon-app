package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.GetRaceDetail_Res;
import com.tkpark86.runch.model.RequestComment_In;

public class CommentActivity extends AppCompatActivity {

    private static final int MAX_COMMENT_LENGTH = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        // received data
        Bundle bundle = getIntent().getExtras();
        final String procTp = bundle.getString("procTp"); // 1.등록, 2.수정, 3.삭제, 4.조회
        final String raceId = bundle.getString("raceId");
        String raceNm = bundle.getString("raceNm");
        final String commentId = bundle.getString("commentId");
        final int position = bundle.getInt("position");
        String content = bundle.getString("content");

        // Title
        setTitle(raceNm);

        ButtonRectangle btnDone = (ButtonRectangle) findViewById(R.id.btn_comment_done);
        btnDone.setRippleSpeed(36f);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(procTp.equals("1")) {
                    requestComment(raceId);
                } else {
                    modifyComment(raceId, commentId, position);
                }
            }
        });

        final TextView tvCommentLength = (TextView) findViewById(R.id.tv_comment_length);
        tvCommentLength.setText(String.valueOf(MAX_COMMENT_LENGTH));

        EditText etContent = (EditText) findViewById(R.id.et_comment_content);
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                int inputLength = s.toString().length();

                String strLength = String.valueOf(MAX_COMMENT_LENGTH - inputLength);
                tvCommentLength.setText(strLength);
            }
        });

        if(procTp.equals("2")) {
            etContent.setText(content);
                etContent.setSelection(content.length());
        }
    }

    private void requestComment(final String raceId) {
        EditText etContent = (EditText) findViewById(R.id.et_comment_content);

        if(etContent.getText().toString().length() == 0) {
            SnackBar snackbar = new SnackBar(CommentActivity.this, getResources().getString(R.string.msg_content));
            snackbar.show();
            etContent.requestFocus();
            return;
        }

        if(etContent.getText().toString().length() > 100) {
            SnackBar snackbar = new SnackBar(CommentActivity.this, getResources().getString(R.string.comment_hint));
            snackbar.show();
            etContent.requestFocus();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        RequestComment_In input = new RequestComment_In();
        input.proc_tp = "1"; // 1.Write, 2.Modify, 3.Delete, 4.Query
        input.race_id = raceId;
        input.member_id = ComFunc.getMemberId(this);
        input.content = etContent.getText().toString();

        String url = ComFunc.getServiceURL("requestComment");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(TAG, "getRaceList: JSONException e="+e.getMessage());
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        GetRaceDetail_Res res = gson.fromJson(response.toString(), GetRaceDetail_Res.class);

                        if(res.returnCode.equals("101")) {
                            Intent intent = new Intent();
                            intent.putExtra("raceId", raceId);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentActivity.this)
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
                // Show Popup
                new AlertDialogWrapper.Builder(CommentActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                requestComment(raceId);
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

    private void modifyComment(final String raceId, final String commentId, final int position) {
        EditText etContent = (EditText) findViewById(R.id.et_comment_content);

        if(etContent.getText().toString().length() == 0) {
            SnackBar snackbar = new SnackBar(CommentActivity.this, getResources().getString(R.string.msg_content));
            snackbar.show();
            etContent.requestFocus();
            return;
        }

        if(etContent.getText().toString().length() > 100) {
            SnackBar snackbar = new SnackBar(CommentActivity.this, getResources().getString(R.string.comment_hint));
            snackbar.show();
            etContent.requestFocus();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        RequestComment_In input = new RequestComment_In();
        input.proc_tp = "2"; // 1.Write, 2.Modify, 3.Delete, 4.Query
        input.comment_id = commentId;
        input.content = etContent.getText().toString();

        String url = ComFunc.getServiceURL("requestComment");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(TAG, "getRaceList: JSONException e="+e.getMessage());
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        GetRaceDetail_Res res = gson.fromJson(response.toString(), GetRaceDetail_Res.class);

                        if(res.returnCode.equals("103")) {
                            Intent intent = new Intent();
                            intent.putExtra("raceId", raceId);
                            intent.putExtra("content", res.output.content);
                            intent.putExtra("position", position);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentActivity.this)
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
                // Show Popup
                new AlertDialogWrapper.Builder(CommentActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                modifyComment(raceId, commentId, position);
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
}
