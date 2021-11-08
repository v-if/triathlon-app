package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.MapStringRequest;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.CommentLike_In;
import com.tkpark86.runch.model.GetCommentList_Out;
import com.tkpark86.runch.model.GetRaceDetail_In;
import com.tkpark86.runch.model.GetRaceDetail_Out;
import com.tkpark86.runch.model.GetRaceDetail_Res;
import com.tkpark86.runch.model.LikeRace_In;
import com.tkpark86.runch.model.LikeRace_Res;
import com.tkpark86.runch.model.MapData;
import com.tkpark86.runch.model.MyRace_In;
import com.tkpark86.runch.model.MyRace_Res;
import com.tkpark86.runch.model.RaceInfo;
import com.tkpark86.runch.model.RequestBadComment_In;
import com.tkpark86.runch.model.RequestBadComment_Res;
import com.tkpark86.runch.model.RequestComment_In;
import com.tkpark86.runch.model.RequestComment_Res;
import com.tkpark86.runch.model.TrackPoint;
import com.tkpark86.runch.model.WayPoint;

public class ScrollingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "Goodruns";

    private static final int ACTIVITY_COMMENT = 1;
    private static final int ACTIVITY_COMMENT_LIST = 2;
    private static final int ACTIVITY_COMMENT_REPLY = 3;
    private static final int ACTIVITY_COMMENT_MODIFY = 4;

    private boolean mIsChanged;

    private GoogleMap mMap;
    //private MapData mMapData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // received data
        Bundle bundle = getIntent().getExtras();
        String position = bundle.getString("position");
        String raceTp = bundle.getString("raceTp");
        final String raceId = bundle.getString("raceId");
        final String raceNm = bundle.getString("raceNm");
        String raceDetail = bundle.getString("raceDetail");
        String raceDt = bundle.getString("raceDt");
        String city = bundle.getString("city");
        String dday = bundle.getString("dday");
        String regiYn = bundle.getString("regiYn");
        String likeCnt = bundle.getString("likeCnt");
        final String commentCnt = bundle.getString("commentCnt");
        String myLike = bundle.getString("myLike");
        String myComment = bundle.getString("myComment");
        String myRace = bundle.getString("myRace");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRace(raceId, view);
            }
        });

        // Title
        setTitle(raceNm);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ScrollingActivity.this);

        getGpxData();

        TextView tvRaceNm = (TextView) findViewById(R.id.cardview_1_race_nm);
        tvRaceNm.setText(raceNm);

        TextView tvRaceDetail = (TextView) findViewById(R.id.cardview_1_race_detail);
        tvRaceDetail.setText(raceDetail);

        TextView tvRaceDt = (TextView) findViewById(R.id.cardview_1_race_dt);
        tvRaceDt.setText(raceDt);

        TextView tvCity = (TextView) findViewById(R.id.cardview_1_city);
        tvCity.setText(city);

        TextView tvDday = (TextView) findViewById(R.id.cardview_1_dday);
        tvDday.setText(dday);

        TextView tvRegiYn = (TextView) findViewById(R.id.cardview_1_iv_regi);

        TextView tvLikeCnt = (TextView) findViewById(R.id.cardview_1_like_race_cnt);
        tvLikeCnt.setTag(R.string.tag_position, position);
        tvLikeCnt.setTag(R.string.tag_my_like_race, myLike);
        tvLikeCnt.setText(likeCnt);

        TextView tvCommentCnt = (TextView) findViewById(R.id.cardview_1_comment_cnt);
        tvCommentCnt.setTag(R.string.tag_my_comment, myComment);
        tvCommentCnt.setText(commentCnt);

        ImageView ivLikeRace = (ImageView) findViewById(R.id.cardview_1_iv_like_race);
        if(myLike.equals("Y")) {
            ivLikeRace.setImageResource(R.drawable.ic_action_heart_p);
        } else {
            ivLikeRace.setImageResource(R.drawable.ic_action_heart);
        }

        ImageView ivComment = (ImageView) findViewById(R.id.cardview_1_iv_like_comment);
        if(myComment.equals("Y")) {
            ivComment.setImageResource(R.drawable.ic_action_monolog_p);
        } else {
            ivComment.setImageResource(R.drawable.ic_action_monolog);
        }

        if(myRace.equals("Y")) {
            fab.setTag(R.string.tag_my_race, "Y");
            fab.setImageResource(R.drawable.ic_grade_yellow_48dp);
        } else {
            fab.setTag(R.string.tag_my_race, "N");
            fab.setImageResource(R.drawable.ic_grade_white_48dp);
        }

        if(dday.length() > 0) {
            tvDday.setVisibility(View.VISIBLE);
            tvDday.setText(dday);
        } else {
            tvDday.setVisibility(View.GONE);
        }

        if (regiYn.equals("Y")) {
            tvRegiYn.setVisibility(View.VISIBLE);
            tvRegiYn.setText(R.string.code_regi_yn);
        } else {
            tvRegiYn.setVisibility(View.GONE);
        }

        if(likeCnt.equals("0")) {
            tvLikeCnt.setVisibility(View.INVISIBLE);
        } else {
            tvLikeCnt.setVisibility(View.VISIBLE);
        }

        if(commentCnt.equals("0")) {
            tvCommentCnt.setVisibility(View.INVISIBLE);
        } else {
            tvCommentCnt.setVisibility(View.VISIBLE);
        }

        LinearLayout llLikeForm = (LinearLayout) findViewById(R.id.cardview_1_like_from);
        llLikeForm.setTag(R.string.tag_like_race, ivLikeRace);
        llLikeForm.setTag(R.string.tag_like_race_cnt, tvLikeCnt);
        llLikeForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeRace(raceId, v);
            }
        });

        LinearLayout llCommentForm = (LinearLayout) findViewById(R.id.cardview_1_comment_from);
        llCommentForm.setTag(R.string.tag_like_comment, ivComment);
        llCommentForm.setTag(R.string.tag_like_comment_cnt, tvCommentCnt);
        llCommentForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tvCommentCnt = (TextView) v.getTag(R.string.tag_like_comment_cnt);
                String commentCnt = tvCommentCnt.getText().toString();
                if (commentCnt.equals("0")) {
                    Intent intent = new Intent(ScrollingActivity.this, CommentActivity.class);
                    intent.putExtra("procTp", "1"); // 1.등록, 2.수정, 3.삭제, 4.조회
                    intent.putExtra("raceId", raceId);
                    intent.putExtra("raceNm", raceNm);
                    intent.putExtra("commentId", "");
                    startActivityForResult(intent, ACTIVITY_COMMENT);
                } else {
                    Intent intent = new Intent(ScrollingActivity.this, CommentListActivity.class);
                    intent.putExtra("raceId", raceId);
                    intent.putExtra("raceNm", raceNm);
                    intent.putExtra("commentCnt", commentCnt);
                    startActivityForResult(intent, ACTIVITY_COMMENT_LIST);
                }
            }
        });


        ImageView ivProcTp = (ImageView) findViewById(R.id.cardview_2_iv_race_tp);
        if(raceTp.equals("1")) {
            ivProcTp.setImageResource(R.drawable.ic_directions_tri_black_24dp);
        } else if(raceTp.equals("2")) {
            ivProcTp.setImageResource(R.drawable.ic_pool_black_24dp);
        } else if(raceTp.equals("3")) {
            ivProcTp.setImageResource(R.drawable.ic_directions_bike_black_24dp);
        } else if(raceTp.equals("4")) {
            ivProcTp.setImageResource(R.drawable.ic_directions_run_black_24dp);
        }


        getRaceDetail(raceId);
    }

    @Override
    public void onBackPressed() {
        if(mIsChanged) {
            TextView tvLikeCnt = (TextView) findViewById(R.id.cardview_1_like_race_cnt);
            TextView tvCommentCnt = (TextView) findViewById(R.id.cardview_1_comment_cnt);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            String position = tvLikeCnt.getTag(R.string.tag_position).toString();
            String likeCnt = tvLikeCnt.getText().toString();
            String myLike = tvLikeCnt.getTag(R.string.tag_my_like_race).toString();
            String commentCnt = tvCommentCnt.getText().toString();
            String myComment = tvCommentCnt.getTag(R.string.tag_my_comment).toString();
            String myRace = fab.getTag(R.string.tag_my_race).toString();

            Intent intent = new Intent();
            intent.putExtra("position",position);
            intent.putExtra("likeCnt",likeCnt);
            intent.putExtra("myLike",myLike);
            intent.putExtra("commentCnt",commentCnt);
            intent.putExtra("myComment",myComment);
            intent.putExtra("myRace",myRace);

            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_COMMENT) {
            if(resultCode == RESULT_OK) {
                String raceId = data.getStringExtra("raceId");
                getRaceDetail(raceId);
                mIsChanged = true;
            }
        } else if(requestCode == ACTIVITY_COMMENT_LIST) {
            if(resultCode == RESULT_OK) {
                String raceId = data.getStringExtra("raceId");
                getRaceDetail(raceId);
                mIsChanged = true;
            }
        } else if(requestCode == ACTIVITY_COMMENT_REPLY) {
            if(resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", 0);
                String replyCnt = data.getStringExtra("replyCnt");

                TextView tvCmtCnt = null;
                if(position == 0) {
                    tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_1);
                } else if(position == 1) {
                    tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_2);
                } else if(position == 2) {
                    tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_3);
                }

                tvCmtCnt.setText(replyCnt);
                if(replyCnt.equals("0")) {
                    tvCmtCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvCmtCnt.setVisibility(View.VISIBLE);
                }
            }
        } else if(requestCode == ACTIVITY_COMMENT_MODIFY) {
            if(resultCode == RESULT_OK) {
                String raceId = data.getStringExtra("raceId");
                String content = data.getStringExtra("content");
                int position = data.getIntExtra("position", 0);

                TextView tvContent = null;
                if(position == 0) {
                    tvContent = (TextView) findViewById(R.id.cardview_3_tv_content_comment_1);
                } else if(position == 1) {
                    tvContent = (TextView) findViewById(R.id.cardview_3_tv_content_comment_2);
                } else if(position == 2) {
                    tvContent = (TextView) findViewById(R.id.cardview_3_tv_content_comment_3);
                }

                tvContent.setText(content);
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map){
            Log.d(TAG, "TestMapDemoActivity.onMapReady: ");
        mMap = map;

        mMap.getUiSettings().setAllGesturesEnabled(false);

        map.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Intent intent = new Intent(ScrollingActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private String getRaceTpNm(String raceTp) {
        String raceTpNm = "";
        if(raceTp.equals("1")) {
            raceTpNm = getResources().getString(R.string.code_race_tp_1);
        } else if(raceTp.equals("2")) {
            raceTpNm = getResources().getString(R.string.code_race_tp_2);
        } else if(raceTp.equals("3")) {
            raceTpNm = getResources().getString(R.string.code_race_tp_3);
        } else if(raceTp.equals("4")) {
            raceTpNm = getResources().getString(R.string.code_race_tp_4);
        }
        return raceTpNm;
    }

    private String getCommentDt2Nm(String code) {
        String name = "";
        if(code.equals("1")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_1);
        } else if(code.equals("2")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_2);
        } else if(code.equals("3")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_3);
        } else if(code.equals("4")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_4);
        } else if(code.equals("5")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_5);
        } else if(code.equals("6")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_6);
        } else if(code.equals("7")) {
            name = getResources().getString(R.string.code_comment_dt2_cd_7);
        }
        return name;
    }

    private void openCommentReplyListActivity(String commentId, int position) {

        TextView tvCmtCnt = null;
        if(position == 0) {
            tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_1);
        } else if(position == 1) {
            tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_2);
        } else if(position == 2) {
            tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_3);
        }

        Intent intent = new Intent(ScrollingActivity.this, CommentReplyListActivity.class);
        intent.putExtra("commentId", commentId);
        intent.putExtra("position", position);
        intent.putExtra("replyCnt", tvCmtCnt.getText().toString());
        startActivityForResult(intent, ACTIVITY_COMMENT_REPLY);
    }

    private void openEtcDialog(final GetCommentList_Out item, final int position) {
        if(item.member_id.equals(ComFunc.getMemberId(this))) {
            new MaterialDialog.Builder(ScrollingActivity.this)
                    .items(R.array.my_etc_list)
                    .title(item.nickname + "'s " + getResources().getString(R.string.comment))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {
                                modifyComment(item.race_id, item.race_nm, item.comment_id, item.content, position);
                            } else if (which == 1) {
                                // Show Popup
                                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                                        .setTitle(R.string.warning)
                                        .setMessage(R.string.msg_delete_comment_warring)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteComment(item.race_id, item.comment_id, position);
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            } else if (which == 2) {
                                likeList(item.comment_id, position);
                            }
                        }
                    }).show();
        } else {
            new MaterialDialog.Builder(ScrollingActivity.this)
                    .items(R.array.etc_list)
                    .title(item.nickname + "'s " + getResources().getString(R.string.comment))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {
                                likeList(item.comment_id, position);
                            } else if (which == 1) {
                                // Show Popup
                                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                                        .setTitle(R.string.warning)
                                        .setMessage(R.string.msg_bad_comment_warring)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                badComment(item.comment_id);
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            }
                        }
                    }).show();
        }
    }

    private void setDetailData(final GetRaceDetail_Out output) {
        // CardView 1
        TextView tvCommentCnt = (TextView) findViewById(R.id.cardview_1_comment_cnt);
        tvCommentCnt.setTag(R.string.tag_my_comment, output.my_comment);
        tvCommentCnt.setText(output.comment_cnt);
        if(output.comment_cnt.equals("0")) {
            tvCommentCnt.setVisibility(View.INVISIBLE);
        } else {
            tvCommentCnt.setVisibility(View.VISIBLE);
        }

        ImageView ivComment = (ImageView) findViewById(R.id.cardview_1_iv_like_comment);
        if(output.my_comment.equals("Y")) {
            ivComment.setImageResource(R.drawable.ic_action_monolog_p);
        } else {
            ivComment.setImageResource(R.drawable.ic_action_monolog);
        }


        // CardView 2
        TextView tvRaceTp = (TextView) findViewById(R.id.cardview_2_tv_race_tp);
        tvRaceTp.setText(getRaceTpNm(output.race_tp));

        TextView tvRaceDetail = (TextView) findViewById(R.id.cardview_2_tv_race_detail);
        tvRaceDetail.setText(output.race_detail);

        TextView tvRegister = (TextView) findViewById(R.id.cardview_2_tv_register);
        tvRegister.setText(output.regi_start_dt + "~" + output.regi_end_dt);

        TextView tvHomepage = (TextView) findViewById(R.id.cardview_2_tv_homepage);
        tvHomepage.setText(output.homepage);


        // CardView 3
        LinearLayout llMoreBtnForm = (LinearLayout) findViewById(R.id.cardview_3_btn_form);
        llMoreBtnForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScrollingActivity.this, CommentListActivity.class);
                intent.putExtra("raceId", output.race_id);
                intent.putExtra("raceNm", output.race_nm);
                intent.putExtra("commentCnt", output.comment_cnt);
                startActivityForResult(intent, ACTIVITY_COMMENT_LIST);
            }
        });



        LinearLayout llForm1 = (LinearLayout) findViewById(R.id.cardview_3_from_comment_1);
        llForm1.setVisibility(View.INVISIBLE);
        LinearLayout llForm2 = (LinearLayout) findViewById(R.id.cardview_3_from_comment_2);
        llForm2.setVisibility(View.GONE);
        LinearLayout llForm3 = (LinearLayout) findViewById(R.id.cardview_3_from_comment_3);
        llForm3.setVisibility(View.GONE);
        llMoreBtnForm.setVisibility(View.GONE);

        for(int i=0; i<output.comments.size(); i++) {
            if(i == 0) {
                llMoreBtnForm.setVisibility(View.VISIBLE);
                llForm1.setVisibility(View.VISIBLE);
                TextView tvNickname = (TextView) findViewById(R.id.cardview_3_tv_nickname_comment_1);
                tvNickname.setText(output.comments.get(0).nickname);
                TextView tvCmtDt2 = (TextView) findViewById(R.id.cardview_3_tv_comment_dt2_comment_1);
                tvCmtDt2.setText(output.comments.get(0).comment_dt2);
                TextView tvCmtDt2Nm = (TextView) findViewById(R.id.cardview_3_tv_comment_dt2_nm_comment_1);
                tvCmtDt2Nm.setText(getCommentDt2Nm(output.comments.get(0).comment_dt2_cd));
                TextView tvContent = (TextView) findViewById(R.id.cardview_3_tv_content_comment_1);
                tvContent.setText(output.comments.get(0).content);
                ImageView ivProfile = (ImageView) findViewById(R.id.cardview_3_iv_profile_comment_1);
                if(output.comments.get(0).profile_img_url != null) {
                    Picasso.with(this)
                            .load(output.comments.get(0).profile_img_url)
                            .placeholder(R.drawable.ic_empty)
                            .error(R.drawable.ic_error)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.default_profile_round);
                }

                ImageView ivLikeRace = (ImageView) findViewById(R.id.cardview_3_iv_like_race_comment_1);
//                if(output.comments.get(0).my_comment_like.equals("Y")) {
//                    ivLikeRace.setImageResource(R.drawable.ic_action_heart_p);
//                } else {
//                    ivLikeRace.setImageResource(R.drawable.ic_action_heart);
//                }

                TextView tvRaceCnt = (TextView) findViewById(R.id.cardview_3_like_race_cnt_comment_1);
                tvRaceCnt.setText(output.comments.get(0).like_cnt);
                if(output.comments.get(0).like_cnt.equals("0")) {
                    tvRaceCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvRaceCnt.setVisibility(View.VISIBLE);
                }

                TextView tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_1);
                tvCmtCnt.setText(output.comments.get(0).reply_cnt);
                if(output.comments.get(0).reply_cnt.equals("0")) {
                    tvCmtCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvCmtCnt.setVisibility(View.VISIBLE);
                }

                LinearLayout llLikeForm = (LinearLayout) findViewById(R.id.cardview_3_like_from_comment_1);
                llLikeForm.setTag(R.string.tag_like_race, ivLikeRace);
                llLikeForm.setTag(R.string.tag_like_race_cnt, tvRaceCnt);
                llLikeForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        commentLike(output.comments.get(0).comment_id, v);
                    }
                });
                LinearLayout llCommentForm = (LinearLayout) findViewById(R.id.cardview_3_comment_from_comment_1);
                llCommentForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openCommentReplyListActivity(output.comments.get(0).comment_id, 0);
                    }
                });
                LinearLayout llEtcForm = (LinearLayout) findViewById(R.id.cardview_3_etc_from_comment_1);
                llEtcForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEtcDialog(output.comments.get(0), 0);
                    }
                });
            } else if(i == 1) {
                llForm2.setVisibility(View.VISIBLE);
                TextView tvNickname = (TextView) findViewById(R.id.cardview_3_tv_nickname_comment_2);
                tvNickname.setText(output.comments.get(1).nickname);
                TextView tvCmtDt2 = (TextView) findViewById(R.id.cardview_3_tv_comment_dt2_comment_2);
                tvCmtDt2.setText(output.comments.get(1).comment_dt2);
                TextView tvCmtDt2Nm = (TextView) findViewById(R.id.cardview_3_tv_comment_dt2_nm_comment_2);
                tvCmtDt2Nm.setText(getCommentDt2Nm(output.comments.get(1).comment_dt2_cd));
                TextView tvContent = (TextView) findViewById(R.id.cardview_3_tv_content_comment_2);
                tvContent.setText(output.comments.get(1).content);
                ImageView ivProfile = (ImageView) findViewById(R.id.cardview_3_iv_profile_comment_2);
                if(output.comments.get(1).profile_img_url != null) {
                    Picasso.with(this)
                            .load(output.comments.get(1).profile_img_url)
                            .placeholder(R.drawable.ic_empty)
                            .error(R.drawable.ic_error)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.default_profile_round);
                }

                ImageView ivLikeRace = (ImageView) findViewById(R.id.cardview_3_iv_like_race_comment_2);
//                if(output.comments.get(1).my_comment_like.equals("Y")) {
//                    ivLikeRace.setImageResource(R.drawable.ic_action_heart_p);
//                } else {
//                    ivLikeRace.setImageResource(R.drawable.ic_action_heart);
//                }

                TextView tvRaceCnt = (TextView) findViewById(R.id.cardview_3_like_race_cnt_comment_2);
                tvRaceCnt.setText(output.comments.get(1).like_cnt);
                if(output.comments.get(1).like_cnt.equals("0")) {
                    tvRaceCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvRaceCnt.setVisibility(View.VISIBLE);
                }

                TextView tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_2);
                tvCmtCnt.setText(output.comments.get(1).reply_cnt);
                if(output.comments.get(1).reply_cnt.equals("0")) {
                    tvCmtCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvCmtCnt.setVisibility(View.VISIBLE);
                }

                LinearLayout llLikeForm = (LinearLayout) findViewById(R.id.cardview_3_like_from_comment_2);
                llLikeForm.setTag(R.string.tag_like_race, ivLikeRace);
                llLikeForm.setTag(R.string.tag_like_race_cnt, tvRaceCnt);
                llLikeForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        commentLike(output.comments.get(1).comment_id, v);
                    }
                });
                LinearLayout llCommentForm = (LinearLayout) findViewById(R.id.cardview_3_comment_from_comment_2);
                llCommentForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openCommentReplyListActivity(output.comments.get(1).comment_id, 1);
                    }
                });
                LinearLayout llEtcForm = (LinearLayout) findViewById(R.id.cardview_3_etc_from_comment_2);
                llEtcForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEtcDialog(output.comments.get(1), 1);
                    }
                });
            } else if(i == 2) {
                llForm3.setVisibility(View.VISIBLE);
                TextView tvNickname = (TextView) findViewById(R.id.cardview_3_tv_nickname_comment_3);
                tvNickname.setText(output.comments.get(2).nickname);
                TextView tvCmtDt2 = (TextView) findViewById(R.id.cardview_3_tv_comment_dt2_comment_3);
                tvCmtDt2.setText(output.comments.get(2).comment_dt2);
                TextView tvCmtDt2Nm = (TextView) findViewById(R.id.cardview_3_tv_comment_dt2_nm_comment_3);
                tvCmtDt2Nm.setText(getCommentDt2Nm(output.comments.get(2).comment_dt2_cd));
                TextView tvContent = (TextView) findViewById(R.id.cardview_3_tv_content_comment_3);
                tvContent.setText(output.comments.get(2).content);
                ImageView ivProfile = (ImageView) findViewById(R.id.cardview_3_iv_profile_comment_3);
                if(output.comments.get(2).profile_img_url != null) {
                    Picasso.with(this)
                            .load(output.comments.get(2).profile_img_url)
                            .placeholder(R.drawable.ic_empty)
                            .error(R.drawable.ic_error)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.default_profile_round);
                }

                ImageView ivLikeRace = (ImageView) findViewById(R.id.cardview_3_iv_like_race_comment_3);
//                if(output.comments.get(2).my_comment_like.equals("Y")) {
//                    ivLikeRace.setImageResource(R.drawable.ic_action_heart_p);
//                } else {
//                    ivLikeRace.setImageResource(R.drawable.ic_action_heart);
//                }

                TextView tvRaceCnt = (TextView) findViewById(R.id.cardview_3_like_race_cnt_comment_3);
                tvRaceCnt.setText(output.comments.get(2).like_cnt);
                if(output.comments.get(2).like_cnt.equals("0")) {
                    tvRaceCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvRaceCnt.setVisibility(View.VISIBLE);
                }

                TextView tvCmtCnt = (TextView) findViewById(R.id.cardview_3_comment_cnt_comment_3);
                tvCmtCnt.setText(output.comments.get(2).reply_cnt);
                if(output.comments.get(2).reply_cnt.equals("0")) {
                    tvCmtCnt.setVisibility(View.INVISIBLE);
                } else {
                    tvCmtCnt.setVisibility(View.VISIBLE);
                }

                LinearLayout llLikeForm = (LinearLayout) findViewById(R.id.cardview_3_like_from_comment_3);
                llLikeForm.setTag(R.string.tag_like_race, ivLikeRace);
                llLikeForm.setTag(R.string.tag_like_race_cnt, tvRaceCnt);
                llLikeForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        commentLike(output.comments.get(2).comment_id, v);
                    }
                });
                LinearLayout llCommentForm = (LinearLayout) findViewById(R.id.cardview_3_comment_from_comment_3);
                llCommentForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openCommentReplyListActivity(output.comments.get(2).comment_id, 2);
                    }
                });
                LinearLayout llEtcForm = (LinearLayout) findViewById(R.id.cardview_3_etc_from_comment_3);
                llEtcForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEtcDialog(output.comments.get(2), 2);
                    }
                });
            }
        }


        // CardView 4
        List<String> thumbnailUrl = new ArrayList<>();
        final List<String> imgUrl = new ArrayList<>();
        if(output.thumbnail_img_url_1 != null) {
            thumbnailUrl.add(output.thumbnail_img_url_1);
            imgUrl.add(output.img_url_1);
        }
        if(output.thumbnail_img_url_2 != null) {
            thumbnailUrl.add(output.thumbnail_img_url_2);
            imgUrl.add(output.img_url_2);
        }
        if(output.thumbnail_img_url_3 != null) {
            thumbnailUrl.add(output.thumbnail_img_url_3);
            imgUrl.add(output.img_url_3);
        }
        if(output.thumbnail_img_url_4 != null) {
            thumbnailUrl.add(output.thumbnail_img_url_4);
            imgUrl.add(output.img_url_4);
        }
        if(output.thumbnail_img_url_5 != null) {
            thumbnailUrl.add(output.thumbnail_img_url_5);
            imgUrl.add(output.img_url_5);
        }

        ImageView ivGallery = null;
        for(int i=0; i<thumbnailUrl.size(); i++) {
            if(i == 0) {
                ivGallery = (ImageView) findViewById(R.id.cardview_4_iv_gallery_1);
            } else if(i == 1) {
                ivGallery = (ImageView) findViewById(R.id.cardview_4_iv_gallery_2);
            } else if(i == 2) {
                ivGallery = (ImageView) findViewById(R.id.cardview_4_iv_gallery_3);
            } else if(i == 3) {
                ivGallery = (ImageView) findViewById(R.id.cardview_4_iv_gallery_4);
            } else if(i == 4) {
                ivGallery = (ImageView) findViewById(R.id.cardview_4_iv_gallery_5);
            }

            ivGallery.setVisibility(View.VISIBLE);
            ivGallery.setTag(R.string.tag_position, i);

            ivGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ScrollingActivity.this, PhotoActivity.class);
                    String position = v.getTag(R.string.tag_position).toString();
                    intent.putExtra("position", position);
                    int cnt = 0;
                    for (int i = 0; i < imgUrl.size(); i++) {
                        if (i == 0) {
                            intent.putExtra("url_1", imgUrl.get(i));
                        } else if (i == 1) {
                            intent.putExtra("url_2", imgUrl.get(i));
                        } else if (i == 2) {
                            intent.putExtra("url_3", imgUrl.get(i));
                        } else if (i == 3) {
                            intent.putExtra("url_4", imgUrl.get(i));
                        } else if (i == 4) {
                            intent.putExtra("url_5", imgUrl.get(i));
                        }
                        cnt++;
                    }
                    intent.putExtra("cnt", cnt);

                    startActivity(intent);
                }
            });

            Picasso.with(this)
                    .load(thumbnailUrl.get(i))
                    .placeholder(R.drawable.ic_empty)
                    .error(R.drawable.ic_error)
                    .into(ivGallery);
        }
    }

    private void drawPolyline(Builder builder, RaceInfo raceInfo) {
        PolylineOptions options = new PolylineOptions();
        NumberFormat nf4Km = new DecimalFormat("#,##0.#km");
        NumberFormat nf4m = new DecimalFormat("#,##0m");
        LatLng tempPoint = null;
        double totlaDistance = 0.0d;
        for (TrackPoint tp : raceInfo.list) {
            LatLng mapPoint = new LatLng(tp.latitude, tp.longitude);
            builder.include(mapPoint);
            options.add(mapPoint);

            if(tempPoint != null) {
                totlaDistance += SphericalUtil.computeDistanceBetween(tempPoint, mapPoint);
                if(totlaDistance > 1000.0d) {
                    tp.distance = nf4Km.format(totlaDistance/1000);
                } else {
                    tp.distance = nf4m.format(totlaDistance);
                }
            }
            tempPoint = mapPoint;
        }
        raceInfo.polyline = mMap.addPolyline(options
                .color(raceInfo.lineColor)
                .width(raceInfo.lineWidth));

        raceInfo.totalDistance = nf4Km.format(totlaDistance/1000);
    }


    private void drawPolygon(Builder builder, RaceInfo raceInfo) {
        PolygonOptions options = new PolygonOptions();
        for (TrackPoint tp : raceInfo.list) {
            LatLng mapPoint = new LatLng(tp.latitude, tp.longitude);
            builder.include(mapPoint);
            options.add(mapPoint);
        }

        Polygon polygon = mMap.addPolygon(options
                .strokeWidth(raceInfo.lineWidth)
                .strokeColor(raceInfo.lineColor)
                .fillColor(raceInfo.lineColor));

        int prevColor = polygon.getFillColor();
        polygon.setStrokeColor(Color.argb(100, Color.red(prevColor), Color.green(prevColor), Color.blue(prevColor)));
        polygon.setFillColor(Color.argb(100, Color.red(prevColor), Color.green(prevColor), Color.blue(prevColor)));
    }


    private void drawRoute(List<RaceInfo> raceInfoList) {
        Builder builder = new Builder();

        for(RaceInfo raceInfo : raceInfoList) {
            if(raceInfo.type.equals("polyline")) {
                drawPolyline(builder, raceInfo);

                // Chart
                //drawChart(raceInfo);

                // set Legend
                //drawLegend(raceInfo);
            } if(raceInfo.type.equals("polygon")) {
                drawPolygon(builder, raceInfo);
            }
        }

        // zoom
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        //
//        float scale = getResources().getDisplayMetrics().density;
//        int dpAsPixels = (int) (180 * scale + 0.5f);
//        mMap.setPadding(0, 0, 0, dpAsPixels);
        //

        //mMap.moveCamera(cu);
        mMap.animateCamera(cu);
    }


    private void drawWayPoint(ArrayList<WayPoint> wptList) {
        for(WayPoint wpt : wptList) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(wpt.latitude, wpt.longitude))
                    .title(wpt.name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                    .infoWindowAnchor(0.5f, 0.5f));
        }
    }


    // http://theopentutorials.com/tutorials/android/xml/android-simple-xmlpullparser-tutorial/
    private MapData xmlParse(InputStream is) {
        MapData mapData = new MapData();

        try {
            RaceInfo raceInfo = null;
            TrackPoint tp = null;
            WayPoint wpt = null;
            String text = "";

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(is, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("trkseg")) {
                            raceInfo = new RaceInfo();
                            raceInfo.type = parser.getAttributeValue(null, "type");
                            raceInfo.legend = parser.getAttributeValue(null, "legend");
                            raceInfo.lineColor = Color.parseColor(parser.getAttributeValue(null, "lineColor"));
                            raceInfo.lineWidth = Float.valueOf(parser.getAttributeValue(null, "lineWidth"));
                        } else if (tagname.equalsIgnoreCase("trkpt")) {
                            tp = new TrackPoint();
                            tp.latitude = Double.valueOf(parser.getAttributeValue(null, "lat"));
                            tp.longitude = Double.valueOf(parser.getAttributeValue(null, "lon"));
                        } else if (tagname.equalsIgnoreCase("wpt")) {
                            wpt = new WayPoint();
                            wpt.latitude = Double.valueOf(parser.getAttributeValue(null, "lat"));
                            wpt.longitude = Double.valueOf(parser.getAttributeValue(null, "lon"));
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("trkseg")) {
                            mapData.raceInfoList.add(raceInfo);
                        } else if (tagname.equalsIgnoreCase("trkpt")) {
                            raceInfo.list.add(tp);
                        } else if (tagname.equalsIgnoreCase("wpt")) {
                            mapData.wptList.add(wpt);
                        } else if (tagname.equalsIgnoreCase("ele")) {
                            tp.elevation = Float.valueOf(text);
                        } else if (tagname.equalsIgnoreCase("name")) {
                            wpt.name = text;
                        }
                        break;

                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            Log.d(TAG, "xmlParse: XmlPullParserException e=" + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "xmlParse: IOException e="+e.getMessage());
            e.printStackTrace();
        }

        //drawWayPoint(wptList);

        return mapData;
    }

    private void getGpxData() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String gpxFileName = "gpx_000114_20151215172000.xml";
        String url = "http://vveb5u.cafe24.com/gpx/"+gpxFileName;

        MapStringRequest stringRequest = new MapStringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        MapData mapData = xmlParse(new ByteArrayInputStream(response.getBytes()));
//                        List<RaceInfo> raceInfoList = mapData.raceInfoList;
//                        drawRoute(raceInfoList);
//
//                        GoodrunsApplication app = (GoodrunsApplication) getApplicationContext();
//                        app.setMapData(mapData);

                        String[] params = new String[]{ response };

                        XmlParseTask task = new XmlParseTask();
                        task.execute(params);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: VolleyError error"+error.getMessage());
            }
        });

        requestQueue.add(stringRequest);
    }

    private void getRaceDetail(final String raceId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        GetRaceDetail_In input = new GetRaceDetail_In();
        input.race_id = raceId;
        input.member_id = ComFunc.getMemberId(this);

        String url = ComFunc.getServiceURL("getRaceDetail");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            Log.d(TAG, "getRaceDetail: JSONException e");
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        GetRaceDetail_Res res = gson.fromJson(response.toString(), GetRaceDetail_Res.class);

                        if(res.returnCode.equals("000")) {
                            setDetailData(res.output);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(ScrollingActivity.this)
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
                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                getRaceDetail(raceId);
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

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(ScrollingActivity.this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        LikeRace_Res res = gson.fromJson(response.toString(), LikeRace_Res.class);
                        mIsChanged = true;

                        String code = res.returnCode; // 101.등록, 102.취소

                        //ImageView ivLike = (ImageView) v.findViewById(R.id.cardview_1_iv_like_race);
                        //TextView tvLikeCnt = (TextView) v.findViewById(R.id.cardview_1_like_race_cnt);
                        ImageView ivLike = (ImageView) v.getTag(R.string.tag_like_race);
                        TextView tvLikeCnt = (TextView) v.getTag(R.string.tag_like_race_cnt);
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
                            new AlertDialogWrapper.Builder(ScrollingActivity.this)
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
                new AlertDialogWrapper.Builder(ScrollingActivity.this)
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

    private void commentLike(final String commentId, final View v) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        CommentLike_In input = new CommentLike_In();
        input.comment_id = commentId;
        input.member_id = ComFunc.getMemberId(this);

        String url = ComFunc.getServiceURL("commentLike");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(LOG_TAG, "RaceDetailActivity.likeRace: e.getMessage()="+e.getMessage());
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(ScrollingActivity.this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        LikeRace_Res res = gson.fromJson(response.toString(), LikeRace_Res.class);

                        String code = res.returnCode; // 101.등록, 102.취소

                        //ImageView ivLike = (ImageView) v.findViewById(R.id.cardview_1_iv_like_race);
                        //TextView tvLikeCnt = (TextView) v.findViewById(R.id.cardview_1_like_race_cnt);
                        //ImageView ivLike = (ImageView) v.getTag(R.string.tag_like_race);
                        TextView tvLikeCnt = (TextView) v.getTag(R.string.tag_like_race_cnt);
                        int likeCnt = Integer.parseInt(tvLikeCnt.getText().toString());
                        if(code.equals("101")) {
                            likeCnt += 1;
                            tvLikeCnt.setText(String.valueOf(likeCnt));
                            //ivLike.setImageResource(R.drawable.ic_action_heart_p);
                            tvLikeCnt.setTag(R.string.tag_my_like_race, "Y");
                            if(likeCnt > 0) {
                                tvLikeCnt.setVisibility(View.VISIBLE);
                            } else {
                                tvLikeCnt.setVisibility(View.INVISIBLE);
                            }
                        } else if(code.equals("102")) {
                            likeCnt -= 1;
                            tvLikeCnt.setText(String.valueOf(likeCnt));
                            //ivLike.setImageResource(R.drawable.ic_action_heart);
                            tvLikeCnt.setTag(R.string.tag_my_like_race, "N");
                            if(likeCnt > 0) {
                                tvLikeCnt.setVisibility(View.VISIBLE);
                            } else {
                                tvLikeCnt.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(ScrollingActivity.this)
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
                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                commentLike(commentId, v);
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

    private void myRace(final String raceId, final View v) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        MyRace_In input = new MyRace_In();
        input.member_id = ComFunc.getMemberId(this);
        input.race_id = raceId;

        String url = ComFunc.getServiceURL("myRace");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(LOG_TAG, "RaceDetailActivity.likeRace: e.getMessage()="+e.getMessage());
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(ScrollingActivity.this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        MyRace_Res res = gson.fromJson(response.toString(), MyRace_Res.class);
                        mIsChanged = true;

                        String code = res.returnCode; // 101.등록, 102.취소

                        if(code.equals("101")) {
                            ((FloatingActionButton) v).setTag(R.string.tag_my_race, "Y");
                            ((FloatingActionButton) v).setImageResource(R.drawable.ic_grade_yellow_48dp);
                        } else if(code.equals("102")) {
                            ((FloatingActionButton) v).setTag(R.string.tag_my_race, "N");
                            ((FloatingActionButton) v).setImageResource(R.drawable.ic_grade_white_48dp);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(ScrollingActivity.this)
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
                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                myRace(raceId, v);
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

    private void modifyComment(String raceId, String raceNm, String commentId, String content, int position) {
        Intent intent = new Intent(ScrollingActivity.this, CommentActivity.class);
        intent.putExtra("procTp", "2"); // 1.등록, 2.수정, 3.삭제, 4.조회
        intent.putExtra("raceId", raceId);
        intent.putExtra("raceNm", raceNm);
        intent.putExtra("commentId", commentId);
        intent.putExtra("content", content);
        intent.putExtra("position", position);
        startActivityForResult(intent, ACTIVITY_COMMENT_MODIFY);
    }

    private void deleteComment(final String raceId, final String commentId, final int position) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        RequestComment_In input = new RequestComment_In();
        input.proc_tp = "3"; // 1.입력, 2.수정, 3.삭제, 4.조회
        input.comment_id = commentId;

        String url = ComFunc.getServiceURL("requestComment");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            Log.d(TAG, "deleteComment: JSONException e");
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        RequestComment_Res res = gson.fromJson(response.toString(), RequestComment_Res.class);

                        if(res.returnCode.equals("104")) {
                            getRaceDetail(raceId);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(ScrollingActivity.this)
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
                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                deleteComment(raceId, commentId, position);
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

    private void likeList(String id, int position) {
        TextView tvLikeCnt = null;
        if(position == 0) {
            tvLikeCnt = (TextView) findViewById(R.id.cardview_3_like_race_cnt_comment_1);
        } else if(position == 1) {
            tvLikeCnt = (TextView) findViewById(R.id.cardview_3_like_race_cnt_comment_2);
        } else if(position == 2) {
            tvLikeCnt = (TextView) findViewById(R.id.cardview_3_like_race_cnt_comment_3);
        }

        Intent intent = new Intent(ScrollingActivity.this, LikeListActivity.class);
        intent.putExtra("procTp", "2"); // 1.대회, 2.코멘트
        intent.putExtra("id", id);
        intent.putExtra("likeCnt", tvLikeCnt.getText().toString());
        startActivity(intent);
    }

    private void badComment(final String commentId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        RequestBadComment_In input = new RequestBadComment_In();
        input.proc_tp = "1"; // 1.입력, 2.수정, 3.삭제
        input.member_id = ComFunc.getMemberId(this);
        input.comment_tp = "1"; // 1.코멘트, 2.댓글
        input.comment_id = commentId;
        input.content = "";

        String url = ComFunc.getServiceURL("requestBadComment");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(TAG, "getRaceDetail: JSONException e");
        }

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        RequestBadComment_Res res = gson.fromJson(response.toString(), RequestBadComment_Res.class);

                        // Show Popup
                        new AlertDialogWrapper.Builder(ScrollingActivity.this)
                                .setTitle(R.string.notice)
                                .setMessage(res.returnMessage)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Log.d(TAG, "onClick: ok");
                                    }
                                }).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show Popup
                new AlertDialogWrapper.Builder(ScrollingActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                badComment(commentId);
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

    class XmlParseTask extends AsyncTask<String, Void, MapData>{
        @Override
        protected MapData doInBackground(String... params) {
            String response = params[0];
            MapData mapData = xmlParse(new ByteArrayInputStream(response.getBytes()));
            return mapData;
        }

        @Override
        protected void onPostExecute(MapData mapData) {
            List<RaceInfo> raceInfoList = mapData.raceInfoList;
            drawRoute(raceInfoList);

            GoodrunsApplication app = (GoodrunsApplication) getApplicationContext();
            app.setMapData(mapData);
        }
    }
}
