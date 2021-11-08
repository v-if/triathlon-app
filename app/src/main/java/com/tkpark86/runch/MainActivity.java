package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.GetRaceList_In;
import com.tkpark86.runch.model.GetRaceList_Out;
import com.tkpark86.runch.model.GetRaceList_Res;
import com.tkpark86.runch.model.LikeRace_In;
import com.tkpark86.runch.model.LikeRace_Res;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Goodruns";

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

    private static final int ACTIVITY_RACE_DETAIL = 1;
    private static final int ACTIVITY_FILTER = 2;
    private static final int ACTIVITY_MEMBER = 3;

    private RaceCardAdapter mAdapter;

    private boolean mEOF = false;
    private boolean loading = true;
    private static final int VISIBLE_THRESHOLD = 1;
    private int mPreviousTotal = 0;
    private int listCnt = 0;

    // samsung NT500
    // E5:4D:53:52:FC:50:28:5C:D3:08:19:A6:EF:7B:C0:71:28:B2:41:35

    // new lenovo
    // F4:B9:97:71:76:E9:34:71:27:40:EB:80:0A:97:1F:C7:EE:53:41:8F


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        navigationView.addHeaderView(header);
        navigationView.setCheckedItem(getMenuId(ComFunc.getRaceTp(this)));
        setMemberInfo(header);

        RecyclerView listView = (RecyclerView) findViewById(R.id.racelist_list);
        //http://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
        //http://www.kmshack.kr/2014/12/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-listview%EC%97%90%EC%84%9C-recyclerview%EB%A1%9C-%EB%A7%88%EC%9D%B4%EA%B7%B8%EB%A0%88%EC%9D%B4%EC%85%98-%ED%95%98%EA%B8%B0/
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > mPreviousTotal) {
                        loading = false;
                        mPreviousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                    loading = true;
                    if (!mEOF) {
                        String conKey = mAdapter.getLastKey(listCnt - 1);
                        getRaceList(conKey);
                    }
                }
            }
        });

        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

        mAdapter = new RaceCardAdapter();
        listView.setAdapter(mAdapter);

        getRaceList("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_FILTER) {
            if(resultCode == RESULT_OK) {
                init();
                getRaceList("");
            }
        } else if(requestCode == ACTIVITY_RACE_DETAIL) {
            if(resultCode == RESULT_OK) {
                if(ComFunc.getRaceTp(this).equals("5")) {
                    init();
                    getRaceList("");
                } else {
                    String position = data.getStringExtra("position");
                    String likeCnt = data.getStringExtra("likeCnt");
                    String myLike = data.getStringExtra("myLike");
                    String commentCnt = data.getStringExtra("commentCnt");
                    String myComment = data.getStringExtra("myComment");
                    String myRace = data.getStringExtra("myRace");

                    GetRaceList_Out item = new GetRaceList_Out();
                    item.like_race_cnt = likeCnt;
                    item.my_like_race = myLike;
                    item.comment_cnt = commentCnt;
                    item.my_comment = myComment;
                    item.my_race = myRace;
                    mAdapter.updateData(Integer.parseInt(position), item);
                }
            }
        } else if(requestCode == ACTIVITY_MEMBER) {
            if(resultCode == RESULT_OK) {
                String logout = data.getStringExtra("logout");
                if(logout.equals("Y")) {
                    Intent intent = new Intent(this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            startActivityForResult(intent, ACTIVITY_FILTER);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.code_race_tp_1) {
            // set RaceTp
            ComFunc.setRaceTp(this, "1");
            init();
            getRaceList("");
        } else if (id == R.id.code_race_tp_2) {
            // set RaceTp
            ComFunc.setRaceTp(this, "2");
            init();
            getRaceList("");
        } else if (id == R.id.code_race_tp_3) {
            // set RaceTp
            ComFunc.setRaceTp(this, "3");
            init();
            getRaceList("");
        } else if (id == R.id.code_race_tp_4) {
            // set RaceTp
            ComFunc.setRaceTp(this, "4");
            init();
            getRaceList("");
        } else if (id == R.id.code_race_tp_5) {
            // set RaceTp
            ComFunc.setRaceTp(this, "5");
            init();
            getRaceList("");
        } else if (id == R.id.nav_user) {
            Intent intent = new Intent(MainActivity.this, MemberActivity.class);
            intent.putExtra("memberId", "");
            startActivityForResult(intent, ACTIVITY_MEMBER);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void init() {
        mPreviousTotal = 0;
        loading = true;
        mEOF = false;
        listCnt = 0;
        mAdapter.clear();
    }

    private void setMemberInfo(View v) {
        String imgUrl = ComFunc.getMemberProfileImgUrl(this);
        ImageView profileImg = (ImageView) v.findViewById(R.id.menu_profile_img);
        if(imgUrl.equals("")) {
            profileImg.setImageResource(R.drawable.default_profile_round);
        } else {
            Picasso.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_empty)
                    .error(R.drawable.ic_error)
                    .into(profileImg);
        }

        String nickname = ComFunc.getMemberNickname(this);
        TextView tvNickname = (TextView) v.findViewById(R.id.menu_nickname);
        tvNickname.setText(nickname);

        String email = ComFunc.getMemberEmail(this);
        TextView tvEmail = (TextView) v.findViewById(R.id.menu_email);
        tvEmail.setText(email);
    }

    private int getMenuId(String raceTp) {
        int id = 0;
        if(raceTp.equals("1")) {
            id = R.id.code_race_tp_1;
        } else if(raceTp.equals("2")) {
            id = R.id.code_race_tp_2;
        } else if(raceTp.equals("3")) {
            id = R.id.code_race_tp_3;
        } else if(raceTp.equals("4")) {
            id = R.id.code_race_tp_4;
        } else if(raceTp.equals("5")) {
            id = R.id.code_race_tp_5;
        }
        return id;
    }

    private List<String> getCityList(int overlay) {
        List<String> cityList = new ArrayList<>();
        if ((overlay & CITY_1) == CITY_1) cityList.add("01");
        if ((overlay & CITY_2) == CITY_2) cityList.add("02");
        if ((overlay & CITY_3) == CITY_3) cityList.add("03");
        if ((overlay & CITY_4) == CITY_4) cityList.add("04");
        if ((overlay & CITY_5) == CITY_5) cityList.add("05");
        if ((overlay & CITY_6) == CITY_6) cityList.add("06");
        if ((overlay & CITY_7) == CITY_7) cityList.add("07");
        if ((overlay & CITY_8) == CITY_8) cityList.add("08");
        if ((overlay & CITY_9) == CITY_9) cityList.add("09");
        if ((overlay & CITY_10) == CITY_10) cityList.add("10");
        if ((overlay & CITY_11) == CITY_11) cityList.add("11");
        if ((overlay & CITY_12) == CITY_12) cityList.add("12");
        if ((overlay & CITY_13) == CITY_13) cityList.add("13");
        if ((overlay & CITY_14) == CITY_14) cityList.add("14");
        if ((overlay & CITY_15) == CITY_15) cityList.add("15");
        if ((overlay & CITY_16) == CITY_16) cityList.add("16");
        if ((overlay & CITY_17) == CITY_17) cityList.add("17");
        return cityList;
    }

    private String getCityName(String city) {
        String cityName = "";
        switch(city) {
            case "01":
                cityName = getResources().getString(R.string.code_city_01);
                break;
            case "02":
                cityName = getResources().getString(R.string.code_city_02);
                break;
            case "03":
                cityName = getResources().getString(R.string.code_city_03);
                break;
            case "04":
                cityName = getResources().getString(R.string.code_city_04);
                break;
            case "05":
                cityName = getResources().getString(R.string.code_city_05);
                break;
            case "06":
                cityName = getResources().getString(R.string.code_city_06);
                break;
            case "07":
                cityName = getResources().getString(R.string.code_city_07);
                break;
            case "08":
                cityName = getResources().getString(R.string.code_city_08);
                break;
            case "09":
                cityName = getResources().getString(R.string.code_city_09);
                break;
            case "10":
                cityName = getResources().getString(R.string.code_city_10);
                break;
            case "11":
                cityName = getResources().getString(R.string.code_city_11);
                break;
            case "12":
                cityName = getResources().getString(R.string.code_city_12);
                break;
            case "13":
                cityName = getResources().getString(R.string.code_city_13);
                break;
            case "14":
                cityName = getResources().getString(R.string.code_city_14);
                break;
            case "15":
                cityName = getResources().getString(R.string.code_city_15);
                break;
            case "16":
                cityName = getResources().getString(R.string.code_city_16);
                break;
            case "17":
                cityName = getResources().getString(R.string.code_city_17);
                break;
        }

        return cityName;
    }

    private void likeRace(final String raceId, final View v) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        LikeRace_In input = new LikeRace_In();
        input.race_id = raceId;
        input.member_id = ComFunc.getMemberId(this);

        String url = ComFunc.getServiceURL("likeRace");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(LOG_TAG, "RaceDetailActivity.likeRace: e.getMessage()="+e.getMessage());
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(MainActivity.this, Request.Method.POST, url, json,
            new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                LikeRace_Res res = gson.fromJson(response.toString(), LikeRace_Res.class);

                String code = res.returnCode; // 101.등록, 102.취소

                ImageView ivLike = (ImageView) v.findViewById(R.id.racelist_item_iv_like_race);
                TextView tvLikeCnt = (TextView) v.findViewById(R.id.racelist_item_like_race_cnt);
                int likeCnt = Integer.parseInt(tvLikeCnt.getText().toString());
                if(code.equals("101")) {
                    likeCnt += 1;
                    tvLikeCnt.setText(String.valueOf(likeCnt));
                    ivLike.setImageResource(R.drawable.ic_action_heart_p);
                    tvLikeCnt.setTag(R.string.tag_my_like_race, "Y");
                    if(likeCnt > 0) {
                        tvLikeCnt.setVisibility(View.VISIBLE);
                    } else {
                        tvLikeCnt.setVisibility(View.INVISIBLE);
                    }
                } else if(code.equals("102")) {
                    likeCnt -= 1;
                    tvLikeCnt.setText(String.valueOf(likeCnt));
                    ivLike.setImageResource(R.drawable.ic_action_heart);
                    tvLikeCnt.setTag(R.string.tag_my_like_race, "N");
                    if(likeCnt > 0) {
                        tvLikeCnt.setVisibility(View.VISIBLE);
                    } else {
                        tvLikeCnt.setVisibility(View.INVISIBLE);
                    }
                } else {
                    // Show Popup
                    new AlertDialogWrapper.Builder(MainActivity.this)
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
                // Show Popup
                new AlertDialogWrapper.Builder(MainActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                likeRace(raceId, v);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: cancel");
                            }
                        })
                        .show();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    private void getRaceList(final String conKey) {
        Log.d(TAG, "getRaceList: ");
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        GetRaceList_In input = new GetRaceList_In();
        input.conKey = conKey;
        input.qryCount = 10;
        input.member_id = ComFunc.getMemberId(this);
        input.race_tp = ComFunc.getRaceTp(this);
        input.search_tp = ComFunc.getSearchTp(this);
        input.race_dt = ComFunc.getRaceDt(this);
        input.regi_yn = ComFunc.getRegiYn(this);
        input.city = ComFunc.getCityOverlay(this);
        input.city_list = getCityList(ComFunc.getCityOverlay(this));

        String url = ComFunc.getServiceURL("getRaceList");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(TAG, "getRaceList: JSONException e="+e.getMessage());
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, json,
            new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                GetRaceList_Res res = gson.fromJson(response.toString(), GetRaceList_Res.class);

                listCnt += res.resultCount;
                if(listCnt == res.totalCount) {
                    mEOF = true;
                }

                if(res.returnCode.equals("000")) {
                    mAdapter.attach(res.output);
                } else {
                    // Show Popup
                    new AlertDialogWrapper.Builder(MainActivity.this)
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
                // Show Popup
                new AlertDialogWrapper.Builder(MainActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                getRaceList(conKey);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: cancel");
                            }
                        })
                        .show();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    class RaceCardAdapter extends RecyclerView.Adapter<RaceCardAdapter.ViewHolder> {
        private List<GetRaceList_Out> list;

        public RaceCardAdapter() {
            list = new ArrayList<>();
        }

        public void attach(List<GetRaceList_Out> _list) {
            list.addAll(_list);
            mAdapter.notifyDataSetChanged();
        }

        public void clear() {
            list.clear();
            mAdapter.notifyDataSetChanged();
        }

        public void updateData(int position, GetRaceList_Out item) {
            list.get(position).my_like_race = item.my_like_race;
            list.get(position).like_race_cnt = item.like_race_cnt;
            list.get(position).my_comment = item.my_comment;
            list.get(position).comment_cnt = item.comment_cnt;
            list.get(position).my_race = item.my_race;
            mAdapter.notifyDataSetChanged();
        }

        public String getLastKey(int position) {
            return list.get(position).conKey;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tvRaceNm = (TextView) v.findViewById(R.id.racelist_item_race_nm);
                    TextView tvRaceDetail = (TextView) v.findViewById(R.id.racelist_item_race_detail);
                    TextView tvRaceDt = (TextView) v.findViewById(R.id.racelist_item_race_dt);
                    TextView tvCity = (TextView) v.findViewById(R.id.racelist_item_city);
                    TextView tvDday = (TextView) v.findViewById(R.id.racelist_item_dday);
                    TextView tvRegiYn = (TextView) v.findViewById(R.id.racelist_item_iv_regi);
                    TextView tvLikeCnt = (TextView) v.findViewById(R.id.racelist_item_like_race_cnt);
                    TextView tvCommentCnt = (TextView) v.findViewById(R.id.racelist_item_comment_cnt);

                    Intent intent = new Intent(MainActivity.this, ScrollingActivity.class);
                    intent.putExtra("position", String.valueOf(position));
                    intent.putExtra("raceTp", v.getTag(R.string.tag_race_tp).toString());
                    intent.putExtra("raceId", v.getTag(R.string.tag_race_id).toString());
                    intent.putExtra("raceNm", tvRaceNm.getText().toString());
                    intent.putExtra("raceDetail", tvRaceDetail.getText().toString());
                    intent.putExtra("raceDt", tvRaceDt.getText().toString());
                    intent.putExtra("city", tvCity.getText().toString());
                    intent.putExtra("dday", tvDday.getText().toString());
                    intent.putExtra("regiYn", tvRegiYn.getTag(R.string.tag_regi_yn).toString());
                    intent.putExtra("likeCnt", tvLikeCnt.getText().toString());
                    intent.putExtra("commentCnt", tvCommentCnt.getText().toString());
                    intent.putExtra("myLike", tvLikeCnt.getTag(R.string.tag_my_like_race).toString());
                    intent.putExtra("myComment", tvCommentCnt.getTag(R.string.tag_my_comment).toString());
                    intent.putExtra("myRace", v.getTag(R.string.tag_my_race).toString());
                    startActivityForResult(intent, ACTIVITY_RACE_DETAIL);
                }
            });
            holder.llLikeForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //SnackBar snackbar = new SnackBar(MainActivity.this, "like");
                    //snackbar.show();
                    String raceId = v.getTag(R.string.tag_race_id).toString();
                    likeRace(raceId, v);
                }
            });
            holder.llCommentForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SnackBar snackbar = new SnackBar(MainActivity.this, "comment");
                    snackbar.show();
                }
            });

            GetRaceList_Out item = list.get(position);
            holder.cardView.setTag(R.string.tag_race_tp, item.race_tp);
            holder.cardView.setTag(R.string.tag_race_id, item.race_id);
            holder.cardView.setTag(R.string.tag_my_race, item.my_race);

            holder.tvRaceNm.setText(item.race_nm);
            holder.tvRaceDetail.setText(item.race_detail);
            holder.tvRaceDt.setText(item.race_dt);
            holder.tvCity.setText(getCityName(list.get(position).city));
            holder.tvRegiYn.setTag(R.string.tag_regi_yn, item.regi_yn);

            // set Tag
            holder.llLikeForm.setTag(R.string.tag_race_id, item.race_id);
            holder.tvLikeRaceCnt.setTag(R.string.tag_my_like_race, item.my_like_race);
            holder.tvCommentCnt.setTag(R.string.tag_my_comment, item.my_comment);

            if(item.my_like_race.equals("Y")) {
                holder.ivLikeRace.setImageResource(R.drawable.ic_action_heart_p);
            } else {
                holder.ivLikeRace.setImageResource(R.drawable.ic_action_heart);
            }

            if(item.my_comment.equals("Y")) {
                holder.ivComment.setImageResource(R.drawable.ic_action_monolog_p);
            } else {
                holder.ivComment.setImageResource(R.drawable.ic_action_monolog);
            }

            if(item.d_day.length() > 0) {
                holder.tvDday.setVisibility(View.VISIBLE);
                holder.tvDday.setText(item.d_day);
            } else {
                holder.tvDday.setVisibility(View.GONE);
            }

            if (item.regi_yn.equals("Y")) {
                holder.tvRegiYn.setVisibility(View.VISIBLE);
                holder.tvRegiYn.setText(R.string.code_regi_yn);
            } else {
                holder.tvRegiYn.setVisibility(View.GONE);
            }

            holder.tvLikeRaceCnt.setText(item.like_race_cnt);
            if(item.like_race_cnt.equals("0")) {
                holder.tvLikeRaceCnt.setVisibility(View.INVISIBLE);
            } else {
                holder.tvLikeRaceCnt.setVisibility(View.VISIBLE);
            }

            holder.tvCommentCnt.setText(item.comment_cnt);
            if(item.comment_cnt.equals("0")) {
                holder.tvCommentCnt.setVisibility(View.INVISIBLE);
            } else {
                holder.tvCommentCnt.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_racelist_item, viewGroup, false);

            return new ViewHolder(itemView);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            protected CardView cardView;
            protected TextView tvRaceNm;
            protected TextView tvRaceDetail;
            protected TextView tvRaceDt;
            protected TextView tvLikeRaceCnt;
            protected TextView tvCommentCnt;
            protected ImageView ivLikeRace;
            protected ImageView ivComment;
            protected TextView tvCity;
            protected TextView tvDday;
            protected TextView tvRegiYn;
            protected LinearLayout llLikeForm;
            protected LinearLayout llCommentForm;

            public ViewHolder(View v) {
                super(v);
                cardView = (CardView) v.findViewById(R.id.card_view);
                tvRaceNm = (TextView) v.findViewById(R.id.racelist_item_race_nm);
                tvRaceDetail = (TextView) v.findViewById(R.id.racelist_item_race_detail);
                tvRaceDt = (TextView) v.findViewById(R.id.racelist_item_race_dt);
                tvLikeRaceCnt = (TextView) v.findViewById(R.id.racelist_item_like_race_cnt);
                tvCommentCnt = (TextView) v.findViewById(R.id.racelist_item_comment_cnt);
                ivLikeRace = (ImageView) v.findViewById(R.id.racelist_item_iv_like_race);
                ivComment = (ImageView) v.findViewById(R.id.racelist_item_iv_like_comment);
                tvCity = (TextView) v.findViewById(R.id.racelist_item_city);
                tvDday = (TextView) v.findViewById(R.id.racelist_item_dday);
                tvRegiYn = (TextView) v.findViewById(R.id.racelist_item_iv_regi);
                llLikeForm = (LinearLayout) v.findViewById(R.id.racelist_item_like_from);
                llCommentForm = (LinearLayout) v.findViewById(R.id.racelist_item_comment_from);
            }
        }
    }
}
