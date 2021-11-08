package com.tkpark86.runch;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
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
import com.tkpark86.runch.model.LikeList_In;
import com.tkpark86.runch.model.LikeList_Out;
import com.tkpark86.runch.model.LikeList_Res;

public class LikeListActivity extends AppCompatActivity {

    private LikeCardAdapter mAdapter;

    private boolean mEOF = false;
    private boolean loading = true;
    private static final int VISIBLE_THRESHOLD = 1;
    private int mPreviousTotal = 0;
    private int listCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);

        // received data
        Bundle bundle = getIntent().getExtras();
        final String procTp = bundle.getString("procTp");
        final String id = bundle.getString("id");
        String likeCnt = bundle.getString("likeCnt");

        setTitle(getResources().getString(R.string.like) + "(" + likeCnt + ")");

        RecyclerView listView = (RecyclerView) findViewById(R.id.like_list);
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
                        likeList(procTp, id, conKey);
                    }
                }
            }
        });

        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

        mAdapter = new LikeCardAdapter();
        listView.setAdapter(mAdapter);

        likeList(procTp, id, "");
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

    private void likeList(final String procTp, final String id, final String conKey) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        LikeList_In input = new LikeList_In();
        input.conKey = conKey;
        input.qryCount = 10;
        input.proc_tp = procTp;			// 1.대회, 2.코멘트
        if(procTp.equals("1")) {
            input.race_id = id;
        } else if(procTp.equals("2")) {
            input.comment_id = id;
        }

        String url = ComFunc.getServiceURL("likeList");

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
                        LikeList_Res res = gson.fromJson(response.toString(), LikeList_Res.class);

                        listCnt += res.resultCount;
                        if(listCnt == res.totalCount) {
                            mEOF = true;
                        }

                        if(res.returnCode.equals("000")) {
                            mAdapter.attach(res);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(LikeListActivity.this)
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
                new AlertDialogWrapper.Builder(LikeListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                likeList(procTp, id, conKey);
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

    class LikeCardAdapter extends RecyclerView.Adapter<LikeCardAdapter.ViewHolder> {
        private List<LikeList_Out> list;
        private int totalCount;

        public LikeCardAdapter() {
            list = new ArrayList<>();
        }

        public void attach(LikeList_Res res) {
            totalCount = res.totalCount;
            list.addAll(res.output);
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

            final LikeList_Out item = list.get(position);

            holder.tvNickname.setText(item.nickname);
            holder.tvCmtDt2.setText(item.like_dt2);
            holder.tvCmtDt2Nm.setText(getCommentDt2Nm(item.like_dt2_cd));
            if(item.profile_img_url != null) {
                Picasso.with(LikeListActivity.this)
                        .load(item.profile_img_url)
                        .placeholder(R.drawable.ic_empty)
                        .error(R.drawable.ic_error)
                        .into(holder.ivProfile);
            } else {
                holder.ivProfile.setImageResource(R.drawable.default_profile_round);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_likelist_item, viewGroup, false);

            return new ViewHolder(itemView);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            protected ImageView ivProfile;
            protected TextView tvNickname;
            protected TextView tvCmtDt2;
            protected TextView tvCmtDt2Nm;

            public ViewHolder(View v) {
                super(v);
                ivProfile = (ImageView) v.findViewById(R.id.likelist_iv_profile);
                tvNickname = (TextView) v.findViewById(R.id.likelist_tv_nickname);
                tvCmtDt2 = (TextView) v.findViewById(R.id.likelist_tv_comment_dt2);
                tvCmtDt2Nm = (TextView) v.findViewById(R.id.likelist_tv_comment_dt2_nm);
            }
        }
    }
}
