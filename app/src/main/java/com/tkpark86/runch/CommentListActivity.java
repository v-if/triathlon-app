package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.CommentLike_In;
import com.tkpark86.runch.model.GetCommentList_In;
import com.tkpark86.runch.model.GetCommentList_Out;
import com.tkpark86.runch.model.GetCommentList_Res;
import com.tkpark86.runch.model.LikeRace_Res;
import com.tkpark86.runch.model.RequestBadComment_In;
import com.tkpark86.runch.model.RequestBadComment_Res;
import com.tkpark86.runch.model.RequestComment_In;
import com.tkpark86.runch.model.RequestComment_Res;

public class CommentListActivity extends AppCompatActivity {

    private static final String TAG = "Goodruns";

    private static final int ACTIVITY_COMMENT = 1;
    private static final int ACTIVITY_COMMENT_LIST = 2;
    private static final int ACTIVITY_COMMENT_REPLY = 3;
    private static final int ACTIVITY_COMMENT_MODIFY = 4;

    private CommentCardAdapter mAdapter;

    private boolean mEOF = false;
    private boolean loading = true;
    private static final int VISIBLE_THRESHOLD = 1;
    private int mPreviousTotal = 0;
    private int listCnt = 0;

    private String mRaceId;
    private String mRaceNm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);

        // received data
        Bundle bundle = getIntent().getExtras();
        mRaceId = bundle.getString("raceId");
        mRaceNm = bundle.getString("raceNm");
        String commentCnt = bundle.getString("commentCnt");

        // Title
        setTitle(getResources().getString(R.string.comment) + "(" + commentCnt + ")");

        RecyclerView listView = (RecyclerView) findViewById(R.id.comment_list);
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
                        getCommentList(mRaceId, conKey);
                    }
                }
            }
        });

        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

        mAdapter = new CommentCardAdapter();
        listView.setAdapter(mAdapter);

        getCommentList(mRaceId, "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comment_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_comment) {
            Intent intent = new Intent(CommentListActivity.this, CommentActivity.class);
            intent.putExtra("procTp", "1"); // 1.등록, 2.수정, 3.삭제, 4.조회
            intent.putExtra("raceId", mRaceId);
            intent.putExtra("raceNm", mRaceNm);
            intent.putExtra("commentId", "");
            startActivityForResult(intent, ACTIVITY_COMMENT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_COMMENT) {
            if(resultCode == RESULT_OK) {
                init();
                String raceId = data.getStringExtra("raceId");
                getCommentList(raceId, "");

                Intent intent = new Intent();
                intent.putExtra("raceId", raceId);
                setResult(RESULT_OK, intent);
            }
        } else if(requestCode == ACTIVITY_COMMENT_MODIFY) {
            if(resultCode == RESULT_OK) {
                String raceId = data.getStringExtra("raceId");
                String content = data.getStringExtra("content");
                int position = data.getIntExtra("position", 0);
                mAdapter.updateData(position, content);

                Intent intent = new Intent();
                intent.putExtra("raceId", raceId);
                setResult(RESULT_OK, intent);
            }
        } else if(requestCode == ACTIVITY_COMMENT_REPLY) {
            if(resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", 0);
                String replyCnt = data.getStringExtra("replyCnt");
                mAdapter.updateReplyCnt(position, replyCnt);

                Intent intent = new Intent();
                intent.putExtra("raceId", mRaceId);
                setResult(RESULT_OK, intent);
            }
        }
    }

    private void init() {
        mPreviousTotal = 0;
        loading = true;
        mEOF = false;
        listCnt = 0;
        mAdapter.clear();
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

    private void commentLike(final String commentId, final View v, final int position) {
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

        RaceJsonObjectRequest jsObjRequest = new RaceJsonObjectRequest(CommentListActivity.this, Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        LikeRace_Res res = gson.fromJson(response.toString(), LikeRace_Res.class);

                        String code = res.returnCode; // 101.등록, 102.취소

                        Intent intent = new Intent();
                        intent.putExtra("raceId", mRaceId);
                        setResult(RESULT_OK, intent);

                        //ImageView ivLike = (ImageView) v.findViewById(R.id.cardview_1_iv_like_race);
                        //TextView tvLikeCnt = (TextView) v.findViewById(R.id.cardview_1_like_race_cnt);
                        //ImageView ivLike = (ImageView) v.getTag(R.string.tag_like_race);
                        TextView tvLikeCnt = (TextView) v.getTag(R.string.tag_like_race_cnt);
                        int likeCnt = Integer.parseInt(tvLikeCnt.getText().toString());
                        if(code.equals("101")) {
                            likeCnt += 1;
                            mAdapter.updateLikeCnt(position, String.valueOf(likeCnt), "Y");
//                            tvLikeCnt.setText(String.valueOf(likeCnt));
//                            //ivLike.setImageResource(R.drawable.ic_action_heart_p);
//                            tvLikeCnt.setTag(R.string.tag_my_like_race, "Y");
//                            if(likeCnt > 0) {
//                                tvLikeCnt.setVisibility(View.VISIBLE);
//                            } else {
//                                tvLikeCnt.setVisibility(View.INVISIBLE);
//                            }
                        } else if(code.equals("102")) {
                            likeCnt -= 1;
                            mAdapter.updateLikeCnt(position, String.valueOf(likeCnt), "N");
//                            tvLikeCnt.setText(String.valueOf(likeCnt));
//                            //ivLike.setImageResource(R.drawable.ic_action_heart);
//                            tvLikeCnt.setTag(R.string.tag_my_like_race, "N");
//                            if(likeCnt > 0) {
//                                tvLikeCnt.setVisibility(View.VISIBLE);
//                            } else {
//                                tvLikeCnt.setVisibility(View.INVISIBLE);
//                            }
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                commentLike(commentId, v, position);
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
        Intent intent = new Intent(CommentListActivity.this, CommentActivity.class);
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
                            mAdapter.deleteData(position);

                            Intent intent = new Intent();
                            intent.putExtra("raceId", raceId);
                            setResult(RESULT_OK, intent);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentListActivity.this)
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

    private void likeList(String id, String likeCnt) {
        Intent intent = new Intent(CommentListActivity.this, LikeListActivity.class);
        intent.putExtra("procTp", "2"); // 1.대회, 2.코멘트
        intent.putExtra("id", id);
        intent.putExtra("likeCnt", likeCnt);
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
                        new AlertDialogWrapper.Builder(CommentListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentListActivity.this)
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

    private void getCommentList(final String raceId, final String conKey) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        GetCommentList_In input = new GetCommentList_In();
        input.conKey = conKey;
        input.qryCount = 10;
        input.race_id = raceId;
        input.member_id = ComFunc.getMemberId(this);

        String url = ComFunc.getServiceURL("getCommentList");

        Gson gson = new Gson();
        JSONObject json = new JSONObject();
        try {
            json = new JSONObject(gson.toJson(input));
        } catch (JSONException e) {
            //Log.d(TAG, "getRaceDetail: JSONException e");
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        GetCommentList_Res res = gson.fromJson(response.toString(), GetCommentList_Res.class);

                        listCnt += res.resultCount;
                        if(listCnt == res.totalCount) {
                            mEOF = true;
                        }

                        if(res.returnCode.equals("000")) {
                            mAdapter.attach(res);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: ok");
                                getCommentList(raceId, conKey);
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

    class CommentCardAdapter extends RecyclerView.Adapter<CommentCardAdapter.ViewHolder> {
        private List<GetCommentList_Out> list;
        private int totalCount;

        public CommentCardAdapter() {
            list = new ArrayList<>();
        }

        public void attach(GetCommentList_Res res) {
            totalCount = res.totalCount;
            list.addAll(res.output);
            mAdapter.notifyDataSetChanged();

            setTitle(getResources().getString(R.string.comment) + "(" + totalCount + ")");
        }

        public void clear() {
            list.clear();
            mAdapter.notifyDataSetChanged();
        }

        public void updateLikeCnt(int position, String likeCnt, String myCommentLike) {
            list.get(position).like_cnt = likeCnt;
            list.get(position).my_comment_like = myCommentLike;
            mAdapter.notifyDataSetChanged();
        }

        public void updateData(int position, String content) {
            list.get(position).content = content;
            mAdapter.notifyDataSetChanged();
        }

        public void updateReplyCnt(int position, String replyCny) {
            list.get(position).reply_cnt = replyCny;
            mAdapter.notifyDataSetChanged();
        }

        public void deleteData(int position) {
            list.remove(position);
            mAdapter.notifyDataSetChanged();

            totalCount -= 1;
            setTitle(getResources().getString(R.string.comment) + "(" + totalCount + ")");
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

            final GetCommentList_Out item = list.get(position);

            holder.tvNickname.setText(item.nickname);
            holder.tvCmtDt2.setText(item.comment_dt2);
            holder.tvCmtDt2Nm.setText(getCommentDt2Nm(item.comment_dt2_cd));
            holder.tvContent.setText(item.content);
            if(item.profile_img_url != null) {
                Picasso.with(CommentListActivity.this)
                        .load(item.profile_img_url)
                        .placeholder(R.drawable.ic_empty)
                        .error(R.drawable.ic_error)
                        .into(holder.ivProfile);
            } else {
                holder.ivProfile.setImageResource(R.drawable.default_profile_round);
            }

//            if(item.my_comment_like.equals("Y")) {
//                holder.ivLikeRace.setImageResource(R.drawable.ic_action_heart_p);
//            } else {
//                holder.ivLikeRace.setImageResource(R.drawable.ic_action_heart);
//            }

            holder.tvRaceCnt.setText(item.like_cnt);
            if(item.like_cnt.equals("0")) {
                holder.tvRaceCnt.setVisibility(View.INVISIBLE);
            } else {
                holder.tvRaceCnt.setVisibility(View.VISIBLE);
            }

            holder.tvCommentCnt.setText(item.reply_cnt);
            if(item.reply_cnt.equals("0")) {
                holder.tvCommentCnt.setVisibility(View.INVISIBLE);
            } else {
                holder.tvCommentCnt.setVisibility(View.VISIBLE);
            }

            holder.llLikeForm.setTag(R.string.tag_like_race, holder.ivLikeRace);
            holder.llLikeForm.setTag(R.string.tag_like_race_cnt, holder.tvRaceCnt);
            holder.llLikeForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commentLike(item.comment_id, v, position);
                }
            });
            holder.llCommentForm.setTag(R.string.tag_comment_id, item.comment_id);
            holder.llCommentForm.setTag(R.string.tag_reply_cnt, item.reply_cnt);
            holder.llCommentForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String commentId = v.getTag(R.string.tag_comment_id).toString();
                    String replyCnt = v.getTag(R.string.tag_reply_cnt).toString();

                    Intent intent = new Intent(CommentListActivity.this, CommentReplyListActivity.class);
                    intent.putExtra("commentId", commentId);
                    intent.putExtra("position", position);
                    intent.putExtra("replyCnt", replyCnt);
                    startActivityForResult(intent, ACTIVITY_COMMENT_REPLY);
                }
            });
            holder.llEtcForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.member_id.equals(ComFunc.getMemberId(CommentListActivity.this))) {
                        new MaterialDialog.Builder(CommentListActivity.this)
                                .items(R.array.my_etc_list)
                                .title(item.nickname + "'s " + getResources().getString(R.string.comment))
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        if (which == 0) {
                                            modifyComment(item.race_id, item.race_nm, item.comment_id, item.content, position);
                                        } else if (which == 1) {
                                            // Show Popup
                                            new AlertDialogWrapper.Builder(CommentListActivity.this)
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
                                            likeList(item.comment_id, item.like_cnt);
                                        }
                                    }
                                }).show();
                    } else {
                        new MaterialDialog.Builder(CommentListActivity.this)
                                .items(R.array.etc_list)
                                .title(item.nickname + "'s " + getResources().getString(R.string.comment))
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        if (which == 0) {
                                            likeList(item.comment_id, item.like_cnt);
                                        } else if (which == 1) {
                                            // Show Popup
                                            new AlertDialogWrapper.Builder(CommentListActivity.this)
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
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_commentlist_item, viewGroup, false);

            return new ViewHolder(itemView);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            //protected CardView cardView;
            protected ImageView ivProfile;
            protected TextView tvNickname;
            protected TextView tvCmtDt2;
            protected TextView tvCmtDt2Nm;
            protected TextView tvContent;
            protected ImageView ivLikeRace;
            protected TextView tvRaceCnt;
            protected ImageView ivLikeComment;
            protected TextView tvCommentCnt;
            protected LinearLayout llLikeForm;
            protected LinearLayout llCommentForm;
            protected LinearLayout llEtcForm;

            public ViewHolder(View v) {
                super(v);
                //cardView = (CardView) v.findViewById(R.id.card_view);
                ivProfile = (ImageView) v.findViewById(R.id.commentlist_iv_profile);
                tvNickname = (TextView) v.findViewById(R.id.commentlist_tv_nickname);
                tvCmtDt2 = (TextView) v.findViewById(R.id.commentlist_tv_comment_dt2);
                tvCmtDt2Nm = (TextView) v.findViewById(R.id.commentlist_tv_comment_dt2_nm);
                tvContent = (TextView) v.findViewById(R.id.commentlist_tv_content);
                ivLikeRace = (ImageView) v.findViewById(R.id.commentlist_iv_like_race);
                tvRaceCnt = (TextView) v.findViewById(R.id.commentlist_like_race_cnt);
                ivLikeComment = (ImageView) v.findViewById(R.id.commentlist_iv_like_comment);
                tvCommentCnt = (TextView) v.findViewById(R.id.commentlist_comment_cnt);
                llLikeForm = (LinearLayout) v.findViewById(R.id.commentlist_like_from);
                llCommentForm = (LinearLayout) v.findViewById(R.id.commentlist_comment_from);
                llEtcForm = (LinearLayout) v.findViewById(R.id.commentlist_etc_from);
            }
        }
    }
}
