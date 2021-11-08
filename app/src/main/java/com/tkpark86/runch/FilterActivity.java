package com.tkpark86.runch;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

import com.tkpark86.runch.common.ComFunc;

public class FilterActivity extends AppCompatActivity {

    // City overlays
    //private static final int CITY_0 = 0;
    private static final int CITY_1 = 1;
    private static final int CITY_2 = 2;
    private static final int CITY_3 = 4;
    private static final int CITY_4 = 8;
    private static final int CITY_5 = 16;
    private static final int CITY_6 = 32;
    private static final int CITY_7 = 64;
    private static final int CITY_8 = 128;
    private static final int CITY_9 = 256;
    private static final int CITY_10 = 512;
    private static final int CITY_11 = 1024;
    private static final int CITY_12 = 2048;
    private static final int CITY_13 = 4096;
    private static final int CITY_14 = 8192;
    private static final int CITY_15 = 16384;
    private static final int CITY_16 = 32768;
    private static final int CITY_17 = 65536;

    private Button mEtRaceDtYear;
    private Button mEtRaceDtMonth;

    private Button mBtnSearchTp1;
    private Button mBtnSearchTp2;
    private Button mBtnSearchTp3;

    private Button mBtnCity1;
    private Button mBtnCity2;
    private Button mBtnCity3;
    private Button mBtnCity4;
    private Button mBtnCity5;
    private Button mBtnCity6;
    private Button mBtnCity7;
    private Button mBtnCity8;
    private Button mBtnCity9;
    private Button mBtnCity10;
    private Button mBtnCity11;
    private Button mBtnCity12;
    private Button mBtnCity13;
    private Button mBtnCity14;
    private Button mBtnCity15;
    private Button mBtnCity16;
    private Button mBtnCity17;

    private Button mBtnRegiYn;

    private Drawable mCheckP;
    private Drawable mCheckN;

    private boolean mIsChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        mCheckP = getResources().getDrawable(R.drawable.ic_action_tick_p);
        mCheckN = getResources().getDrawable(R.drawable.ic_action_tick);

        mEtRaceDtYear = (Button) findViewById(R.id.filter_btn_race_dt_year);
        mEtRaceDtMonth = (Button) findViewById(R.id.filter_btn_race_dt_month);

        mBtnSearchTp1 = (Button) findViewById(R.id.filter_btn_search_tp1);
        mBtnSearchTp2 = (Button) findViewById(R.id.filter_btn_search_tp2);
        mBtnSearchTp3 = (Button) findViewById(R.id.filter_btn_search_tp3);

        mBtnCity1 = (Button) findViewById(R.id.filter_btn_city1);
        mBtnCity2 = (Button) findViewById(R.id.filter_btn_city2);
        mBtnCity3 = (Button) findViewById(R.id.filter_btn_city3);
        mBtnCity4 = (Button) findViewById(R.id.filter_btn_city4);
        mBtnCity5 = (Button) findViewById(R.id.filter_btn_city5);
        mBtnCity6 = (Button) findViewById(R.id.filter_btn_city6);
        mBtnCity7 = (Button) findViewById(R.id.filter_btn_city7);
        mBtnCity8 = (Button) findViewById(R.id.filter_btn_city8);
        mBtnCity9 = (Button) findViewById(R.id.filter_btn_city9);
        mBtnCity10 = (Button) findViewById(R.id.filter_btn_city10);
        mBtnCity11 = (Button) findViewById(R.id.filter_btn_city11);
        mBtnCity12 = (Button) findViewById(R.id.filter_btn_city12);
        mBtnCity13 = (Button) findViewById(R.id.filter_btn_city13);
        mBtnCity14 = (Button) findViewById(R.id.filter_btn_city14);
        mBtnCity15 = (Button) findViewById(R.id.filter_btn_city15);
        mBtnCity16 = (Button) findViewById(R.id.filter_btn_city16);
        mBtnCity17 = (Button) findViewById(R.id.filter_btn_city17);

        mBtnRegiYn = (Button) findViewById(R.id.filter_btn_regi_yn);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setUserSettingInfo();
    }

    @Override
    public void onBackPressed() {
        if(mIsChanged) {
            setFilterInfo();
            setResult(RESULT_OK);
        }

        super.onBackPressed();
    }

    public void mOnClick(View v) {
        //Log.d(LOG_TAG, "FilterActivity.mOnClick:");
        switch(v.getId()) {
            case R.id.filter_btn_race_dt_year:
                YearDialog yearDialog = new YearDialog(this);
                yearDialog.show();
                break;
            case R.id.filter_btn_race_dt_month:
                MonthDialog monthDialog = new MonthDialog(this);
                monthDialog.show();
                break;
            case R.id.filter_btn_search_tp1:
            case R.id.filter_btn_search_tp2:
            case R.id.filter_btn_search_tp3:
                setSearchTpBtn(v.getId());
                break;
            case R.id.filter_btn_city1:
            case R.id.filter_btn_city2:
            case R.id.filter_btn_city3:
            case R.id.filter_btn_city4:
            case R.id.filter_btn_city5:
            case R.id.filter_btn_city6:
            case R.id.filter_btn_city7:
            case R.id.filter_btn_city8:
            case R.id.filter_btn_city9:
            case R.id.filter_btn_city10:
            case R.id.filter_btn_city11:
            case R.id.filter_btn_city12:
            case R.id.filter_btn_city13:
            case R.id.filter_btn_city14:
            case R.id.filter_btn_city15:
            case R.id.filter_btn_city16:
            case R.id.filter_btn_city17:
                setCityBtn(v);
                break;
            case R.id.filter_btn_regi_yn:
                setRegiYn(v);
                break;
        }
    }

    private void setSearchTpBtn(int id) {
        mBtnSearchTp1.setSelected(false);
        mBtnSearchTp2.setSelected(false);
        mBtnSearchTp3.setSelected(false);
        mBtnSearchTp1.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        mBtnSearchTp2.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        mBtnSearchTp3.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);

        if(id == R.id.filter_btn_search_tp1) {
            mBtnSearchTp1.setSelected(true);
            mBtnSearchTp1.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else if(id == R.id.filter_btn_search_tp2) {
            mBtnSearchTp2.setSelected(true);
            mBtnSearchTp2.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else if(id == R.id.filter_btn_search_tp3) {
            mBtnSearchTp3.setSelected(true);
            mBtnSearchTp3.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        }

        mIsChanged = true;
    }

    private void setCityBtn(View v) {

        if (v.getId() == R.id.filter_btn_city1) {
            if(v.isSelected()) {
                mBtnCity1.setSelected(false);
                mBtnCity1.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity1.setSelected(true);
                mBtnCity1.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city2) {
            if(v.isSelected()) {
                mBtnCity2.setSelected(false);
                mBtnCity2.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity2.setSelected(true);
                mBtnCity2.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city3) {
            if(v.isSelected()) {
                mBtnCity3.setSelected(false);
                mBtnCity3.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity3.setSelected(true);
                mBtnCity3.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city4) {
            if(v.isSelected()) {
                mBtnCity4.setSelected(false);
                mBtnCity4.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity4.setSelected(true);
                mBtnCity4.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city5) {
            if(v.isSelected()) {
                mBtnCity5.setSelected(false);
                mBtnCity5.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity5.setSelected(true);
                mBtnCity5.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city6) {
            if(v.isSelected()) {
                mBtnCity6.setSelected(false);
                mBtnCity6.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity6.setSelected(true);
                mBtnCity6.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city7) {
            if(v.isSelected()) {
                mBtnCity7.setSelected(false);
                mBtnCity7.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity7.setSelected(true);
                mBtnCity7.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city8) {
            if(v.isSelected()) {
                mBtnCity8.setSelected(false);
                mBtnCity8.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity8.setSelected(true);
                mBtnCity8.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city9) {
            if(v.isSelected()) {
                mBtnCity9.setSelected(false);
                mBtnCity9.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity9.setSelected(true);
                mBtnCity9.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city10) {
            if(v.isSelected()) {
                mBtnCity10.setSelected(false);
                mBtnCity10.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity10.setSelected(true);
                mBtnCity10.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city11) {
            if(v.isSelected()) {
                mBtnCity11.setSelected(false);
                mBtnCity11.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity11.setSelected(true);
                mBtnCity11.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city12) {
            if(v.isSelected()) {
                mBtnCity12.setSelected(false);
                mBtnCity12.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity12.setSelected(true);
                mBtnCity12.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city13) {
            if(v.isSelected()) {
                mBtnCity13.setSelected(false);
                mBtnCity13.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity13.setSelected(true);
                mBtnCity13.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city14) {
            if(v.isSelected()) {
                mBtnCity14.setSelected(false);
                mBtnCity14.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity14.setSelected(true);
                mBtnCity14.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city15) {
            if(v.isSelected()) {
                mBtnCity15.setSelected(false);
                mBtnCity15.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity15.setSelected(true);
                mBtnCity15.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city16) {
            if(v.isSelected()) {
                mBtnCity16.setSelected(false);
                mBtnCity16.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity16.setSelected(true);
                mBtnCity16.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        } else if (v.getId() == R.id.filter_btn_city17) {
            if(v.isSelected()) {
                mBtnCity17.setSelected(false);
                mBtnCity17.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            } else {
                mBtnCity17.setSelected(true);
                mBtnCity17.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
            }
        }

        mIsChanged = true;
    }

    private void setRegiYn(View v) {
        if(mBtnRegiYn.isSelected()) {
            mBtnRegiYn.setSelected(false);
            mBtnRegiYn.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        } else {
            mBtnRegiYn.setSelected(true);
            mBtnRegiYn.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        }

        mIsChanged = true;
    }

    private void setFilterInfo() {
        // set Search Tp
        String searchTp = "0";
        if(mBtnSearchTp1.isSelected()) {
            searchTp = "0";
        } else if(mBtnSearchTp2.isSelected()) {
            searchTp = "1";
        } else if(mBtnSearchTp3.isSelected()) {
            searchTp = "2";
        }
        ComFunc.setSearchTp(FilterActivity.this, searchTp);

        // set City Overlay
        int overlay = 0;
        overlay += mBtnCity1.isSelected() ? CITY_1 : 0;
        overlay += mBtnCity2.isSelected() ? CITY_2 : 0;
        overlay += mBtnCity3.isSelected() ? CITY_3 : 0;
        overlay += mBtnCity4.isSelected() ? CITY_4 : 0;
        overlay += mBtnCity5.isSelected() ? CITY_5 : 0;
        overlay += mBtnCity6.isSelected() ? CITY_6 : 0;
        overlay += mBtnCity7.isSelected() ? CITY_7 : 0;
        overlay += mBtnCity8.isSelected() ? CITY_8 : 0;
        overlay += mBtnCity9.isSelected() ? CITY_9 : 0;
        overlay += mBtnCity10.isSelected() ? CITY_10 : 0;
        overlay += mBtnCity11.isSelected() ? CITY_11 : 0;
        overlay += mBtnCity12.isSelected() ? CITY_12 : 0;
        overlay += mBtnCity13.isSelected() ? CITY_13 : 0;
        overlay += mBtnCity14.isSelected() ? CITY_14 : 0;
        overlay += mBtnCity15.isSelected() ? CITY_15 : 0;
        overlay += mBtnCity16.isSelected() ? CITY_16 : 0;
        overlay += mBtnCity17.isSelected() ? CITY_17 : 0;
        ComFunc.setCityOverlay(FilterActivity.this, overlay);

        // set Race Dt
        String raceDtYear = mEtRaceDtYear.getTag(R.string.tag_race_dt_year).toString();
        String raceDtMonth = mEtRaceDtMonth.getTag(R.string.tag_race_dt_month).toString();
        String raceDt = raceDtYear + raceDtMonth;
        if(raceDt.length() == 6) {
            ComFunc.setRaceDt(FilterActivity.this, raceDt);
        }

        // set Regi Yn
        String regiYn = "N";
        if(mBtnRegiYn.isSelected()) {
            regiYn = "Y";
        }
        ComFunc.setRegiYn(FilterActivity.this, regiYn);
    }

    private void setUserSettingInfo() {
        String searchTp = ComFunc.getSearchTp(FilterActivity.this);
        if(searchTp.equals("0")) {
            mBtnSearchTp1.setSelected(true);
            mBtnSearchTp1.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);

            mBtnSearchTp2.setSelected(false);
            mBtnSearchTp3.setSelected(false);
            mBtnSearchTp2.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            mBtnSearchTp3.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        } else if(searchTp.equals("1")) {
            mBtnSearchTp2.setSelected(true);
            mBtnSearchTp2.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);

            mBtnSearchTp1.setSelected(false);
            mBtnSearchTp3.setSelected(false);
            mBtnSearchTp1.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            mBtnSearchTp3.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        } else if(searchTp.equals("2")) {
            mBtnSearchTp3.setSelected(true);
            mBtnSearchTp3.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);

            mBtnSearchTp1.setSelected(false);
            mBtnSearchTp2.setSelected(false);
            mBtnSearchTp1.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
            mBtnSearchTp2.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        String raceDt = ComFunc.getRaceDt(FilterActivity.this);
        if(!raceDt.equals("0")) {
            String strYear = raceDt.substring(0, 4);
            mEtRaceDtYear.setText(strYear + getResources().getString(R.string.year));
            mEtRaceDtYear.setTag(R.string.tag_race_dt_year, strYear);

            String strMonth = raceDt.substring(4, 6);
            if(strMonth.subSequence(0, 1).equals("0")) {
                mEtRaceDtMonth.setText(strMonth.subSequence(1, 2) + getResources().getString(R.string.month));
            } else {
                mEtRaceDtMonth.setText(strMonth + getResources().getString(R.string.month));
            }
            mEtRaceDtMonth.setTag(R.string.tag_race_dt_month, strMonth);
        }

        String regiYn = ComFunc.getRegiYn(FilterActivity.this);
        if(regiYn.equals("Y")) {
            mBtnRegiYn.setSelected(true);
            mBtnRegiYn.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnRegiYn.setSelected(false);
            mBtnRegiYn.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        int overlay = ComFunc.getCityOverlay(FilterActivity.this);
        if ((overlay & CITY_1) == CITY_1) {
            mBtnCity1.setSelected(true);
            mBtnCity1.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity1.setSelected(false);
            mBtnCity1.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_2) == CITY_2) {
            mBtnCity2.setSelected(true);
            mBtnCity2.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity2.setSelected(false);
            mBtnCity2.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_3) == CITY_3) {
            mBtnCity3.setSelected(true);
            mBtnCity3.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity3.setSelected(false);
            mBtnCity3.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_4) == CITY_4) {
            mBtnCity4.setSelected(true);
            mBtnCity4.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity4.setSelected(false);
            mBtnCity4.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_5) == CITY_5) {
            mBtnCity5.setSelected(true);
            mBtnCity5.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity5.setSelected(false);
            mBtnCity5.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_6) == CITY_6) {
            mBtnCity6.setSelected(true);
            mBtnCity6.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity6.setSelected(false);
            mBtnCity6.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_7) == CITY_7) {
            mBtnCity7.setSelected(true);
            mBtnCity7.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity7.setSelected(false);
            mBtnCity7.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_8) == CITY_8) {
            mBtnCity8.setSelected(true);
            mBtnCity8.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity8.setSelected(false);
            mBtnCity8.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_9) == CITY_9) {
            mBtnCity9.setSelected(true);
            mBtnCity9.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity9.setSelected(false);
            mBtnCity9.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_10) == CITY_10) {
            mBtnCity10.setSelected(true);
            mBtnCity10.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity10.setSelected(false);
            mBtnCity10.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_11) == CITY_11) {
            mBtnCity11.setSelected(true);
            mBtnCity11.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity11.setSelected(false);
            mBtnCity11.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_12) == CITY_12) {
            mBtnCity12.setSelected(true);
            mBtnCity12.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity12.setSelected(false);
            mBtnCity12.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_13) == CITY_13) {
            mBtnCity13.setSelected(true);
            mBtnCity13.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity13.setSelected(false);
            mBtnCity13.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_14) == CITY_14) {
            mBtnCity14.setSelected(true);
            mBtnCity14.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity14.setSelected(false);
            mBtnCity14.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_15) == CITY_15) {
            mBtnCity15.setSelected(true);
            mBtnCity15.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity15.setSelected(false);
            mBtnCity15.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_16) == CITY_16) {
            mBtnCity16.setSelected(true);
            mBtnCity16.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity16.setSelected(false);
            mBtnCity16.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }

        if ((overlay & CITY_17) == CITY_17) {
            mBtnCity17.setSelected(true);
            mBtnCity17.setCompoundDrawablesWithIntrinsicBounds(mCheckP, null, null, null);
        } else {
            mBtnCity17.setSelected(false);
            mBtnCity17.setCompoundDrawablesWithIntrinsicBounds(mCheckN, null, null, null);
        }
    }

    public class YearDialog extends Dialog implements OnClickListener {

        public YearDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_year);
            getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setCanceledOnTouchOutside(true);
            setCancelable(true);

            Button btnYear15 = (Button) findViewById(R.id.dialog_year_15);
            Button btnYaer16 = (Button) findViewById(R.id.dialog_year_16);
            Button btnYaer17 = (Button) findViewById(R.id.dialog_year_17);
            Button btnYear18 = (Button) findViewById(R.id.dialog_year_18);

            btnYear15.setOnClickListener(this);
            btnYaer16.setOnClickListener(this);
            btnYaer17.setOnClickListener(this);
            btnYear18.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.dialog_year_15:
                    setYear(String.valueOf(2015));
                    break;
                case R.id.dialog_year_16:
                    setYear(String.valueOf(2016));
                    break;
                case R.id.dialog_year_17:
                    setYear(String.valueOf(2017));
                    break;
                case R.id.dialog_year_18:
                    setYear(String.valueOf(2018));
                    break;
            }
        }

        private void setYear(String year) {
            mEtRaceDtYear.setText(year + getResources().getString(R.string.year));
            mEtRaceDtYear.setTag(R.string.tag_race_dt_year, year);
            mIsChanged = true;
            dismiss();
        }
    }

    public class MonthDialog extends Dialog implements OnClickListener {

        public MonthDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_month);
            getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setCanceledOnTouchOutside(true);
            setCancelable(true);

            Button btnMonth1 = (Button) findViewById(R.id.dialog_month_1);
            Button btnMonth2 = (Button) findViewById(R.id.dialog_month_2);
            Button btnMonth3 = (Button) findViewById(R.id.dialog_month_3);
            Button btnMonth4 = (Button) findViewById(R.id.dialog_month_4);
            Button btnMonth5 = (Button) findViewById(R.id.dialog_month_5);
            Button btnMonth6 = (Button) findViewById(R.id.dialog_month_6);
            Button btnMonth7 = (Button) findViewById(R.id.dialog_month_7);
            Button btnMonth8 = (Button) findViewById(R.id.dialog_month_8);
            Button btnMonth9 = (Button) findViewById(R.id.dialog_month_9);
            Button btnMonth10 = (Button) findViewById(R.id.dialog_month_10);
            Button btnMonth11 = (Button) findViewById(R.id.dialog_month_11);
            Button btnMonth12 = (Button) findViewById(R.id.dialog_month_12);

            btnMonth1.setOnClickListener(this);
            btnMonth2.setOnClickListener(this);
            btnMonth3.setOnClickListener(this);
            btnMonth4.setOnClickListener(this);
            btnMonth5.setOnClickListener(this);
            btnMonth6.setOnClickListener(this);
            btnMonth7.setOnClickListener(this);
            btnMonth8.setOnClickListener(this);
            btnMonth9.setOnClickListener(this);
            btnMonth10.setOnClickListener(this);
            btnMonth11.setOnClickListener(this);
            btnMonth12.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.dialog_month_1:
                    setMonth("01");
                    break;
                case R.id.dialog_month_2:
                    setMonth("02");
                    break;
                case R.id.dialog_month_3:
                    setMonth("03");
                    break;
                case R.id.dialog_month_4:
                    setMonth("04");
                    break;
                case R.id.dialog_month_5:
                    setMonth("05");
                    break;
                case R.id.dialog_month_6:
                    setMonth("06");
                    break;
                case R.id.dialog_month_7:
                    setMonth("07");
                    break;
                case R.id.dialog_month_8:
                    setMonth("08");
                    break;
                case R.id.dialog_month_9:
                    setMonth("09");
                    break;
                case R.id.dialog_month_10:
                    setMonth("10");
                    break;
                case R.id.dialog_month_11:
                    setMonth("11");
                    break;
                case R.id.dialog_month_12:
                    setMonth("12");
                    break;
            }
        }

        private void setMonth(String month) {
            if(month.subSequence(0, 1).equals("0")) {
                mEtRaceDtMonth.setText(month.subSequence(1, 2) + getResources().getString(R.string.month));
            } else {
                mEtRaceDtMonth.setText(month + getResources().getString(R.string.month));
            }
            mEtRaceDtMonth.setTag(R.string.tag_race_dt_month, month);
            mIsChanged = true;
            dismiss();
        }
    }
}
