package com.tkpark86.runch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonRectangle;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "Goodruns";

    //requestCode
    private static final int REQUESTCODE_LOGIN = 1;
    private static final int REQUESTCODE_SIGNUP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Log.d(TAG, "StartActivity.onCreate: ");

        ButtonRectangle btnLogin = (ButtonRectangle) findViewById(R.id.btn_start_login);
        btnLogin.setRippleSpeed(36f);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivityForResult(intent, REQUESTCODE_LOGIN);
            }
        });

        ButtonFlat tvSignup = (ButtonFlat) findViewById(R.id.btn_start_signup);
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, SignupActivity.class);
                startActivityForResult(intent, REQUESTCODE_SIGNUP);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "StartActivity.onResume: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUESTCODE_LOGIN) {
            if(resultCode == RESULT_OK) {
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        } else if(requestCode == REQUESTCODE_SIGNUP) {
            if(resultCode == RESULT_OK) {
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
