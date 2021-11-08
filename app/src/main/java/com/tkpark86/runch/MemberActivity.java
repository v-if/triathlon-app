package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.views.ButtonRectangle;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.ChangePassword_In;
import com.tkpark86.runch.model.ChangePassword_Res;

public class MemberActivity extends AppCompatActivity {

    private EditText etOriPassword;
    private EditText etNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        String email = ComFunc.getMemberEmail(this);
        String nickname = ComFunc.getMemberNickname(this);
        String imgUrl = ComFunc.getMemberProfileImgUrl(this);

        AutoCompleteTextView tvEmailView = (AutoCompleteTextView) findViewById(R.id.member_tv_email);
        tvEmailView.setText(email);

        AutoCompleteTextView tvNicknameView = (AutoCompleteTextView) findViewById(R.id.member_tv_nickname);
        tvNicknameView.setText(nickname);

        ImageView ivProfile = (ImageView) findViewById(R.id.member_profile_iv);
        if(imgUrl.equals("")) {
            ivProfile.setImageResource(R.drawable.default_profile_round);
        } else {
            Picasso.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_empty)
                    .error(R.drawable.ic_error)
                    .into(ivProfile);
        }

        ButtonRectangle btnDone = (ButtonRectangle) findViewById(R.id.btn_member_send);
        btnDone.setRippleSpeed(36f);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_password_change) {
            openPasswordChangeDialog("", "", "ori", 0);
        } else if (id == R.id.action_logout) {
            // Show Popup
            new AlertDialogWrapper.Builder(this)
                    .setTitle(R.string.notice)
                    .setMessage(R.string.msg_logout_warring)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d(TAG, "onClick: cancel");
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPasswordChangeDialog(String oriPassword, String newPassword, String focus, int msgId) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.password_change)
                .customView(R.layout.dialog_password_change, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        changePassword();
                    }
                }).build();

        etOriPassword = (EditText) dialog.getCustomView().findViewById(R.id.dialog_original_password);
        etOriPassword.setText(oriPassword);
        etOriPassword.setError(null);

        etNewPassword = (EditText) dialog.getCustomView().findViewById(R.id.dialog_new_password);
        etNewPassword.setText(newPassword);
        etNewPassword.setError(null);

        if(focus.equals("ori")) {
            if(msgId != 0) {
                etOriPassword.setError(getString(msgId));
            }
            etOriPassword.requestFocus();
        } else if(focus.equals("new")) {
            if(msgId != 0) {
                etNewPassword.setError(getString(msgId));
            }
            etNewPassword.requestFocus();
        }

        dialog.show();
    }

    private void done() {

    }

    private void logout() {
        ComFunc.logOut(this);
        Intent intent = new Intent();
        intent.putExtra("logout", "Y");
        setResult(RESULT_OK, intent);
        finish();
    }

    private void changePassword() {
        final String oriPassword = etOriPassword.getText().toString();
        final String newPassword = etNewPassword.getText().toString();

        if (TextUtils.isEmpty(oriPassword)) {
            openPasswordChangeDialog(oriPassword, newPassword, "ori", R.string.error_field_required);
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            openPasswordChangeDialog(oriPassword, newPassword, "new", R.string.error_field_required);
            return;
        }

        if (!ComFunc.isPasswordValidator(oriPassword)) {
            openPasswordChangeDialog(oriPassword, newPassword, "ori", R.string.error_invalid_password);
            return;
        }
        if (!ComFunc.isPasswordValidator(newPassword)) {
            openPasswordChangeDialog(oriPassword, newPassword, "new", R.string.error_invalid_password);
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        ChangePassword_In input = new ChangePassword_In();
        input.member_id = ComFunc.getMemberId(this);
        input.email = ComFunc.getMemberEmail(this);
        input.ori_password = ComFunc.encodePassword(oriPassword);
        input.new_password = ComFunc.encodePassword(newPassword);

        String url = ComFunc.getServiceURL("changePassword");

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
                        ChangePassword_Res res = gson.fromJson(response.toString(), ChangePassword_Res.class);

                        if(res.returnCode.equals("106")) {
                            ComFunc.setMemberPw(MemberActivity.this, res.output.new_password);
                            // Show Popup
                            new AlertDialogWrapper.Builder(MemberActivity.this)
                                    .setTitle(R.string.notice)
                                    .setMessage(res.returnMessage)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Success
                                        }
                                    }).show();
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(MemberActivity.this)
                                    .setTitle(R.string.notice)
                                    .setMessage(res.returnMessage)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            openPasswordChangeDialog(oriPassword, newPassword, "ori", 0);
                                        }
                                    }).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show Popup
                new AlertDialogWrapper.Builder(MemberActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                changePassword();
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
