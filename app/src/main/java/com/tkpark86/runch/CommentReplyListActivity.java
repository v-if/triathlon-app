package com.tkpark86.runch;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.tkpark86.runch.common.ComFunc;
import com.tkpark86.runch.common.RaceJsonObjectRequest;
import com.tkpark86.runch.model.GetCommentReplyList_In;
import com.tkpark86.runch.model.GetCommentReplyList_Out;
import com.tkpark86.runch.model.GetCommentReplyList_Res;
import com.tkpark86.runch.model.RequestBadComment_In;
import com.tkpark86.runch.model.RequestBadComment_Res;
import com.tkpark86.runch.model.RequestCommentReply_In;
import com.tkpark86.runch.model.RequestCommentReply_Out;
import com.tkpark86.runch.model.RequestCommentReply_Res;

public class CommentReplyListActivity extends AppCompatActivity {

    private CommentReplyCardAdapter mAdapter;

    private boolean mEOF = false;
    private boolean loading = true;
    private static final int VISIBLE_THRESHOLD = 1;
    private int mPreviousTotal = 0;
    private int listCnt = 0;

    private RecyclerView mListView;
    private boolean mIsChanged;

    private int mPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_reply_list);

        // received data
        Bundle bundle = getIntent().getExtras();
        final String commentId = bundle.getString("commentId");
        String replyCnt = bundle.getString("replyCnt");
        mPosition = bundle.getInt("position");

        // Title
        setTitle(getResources().getString(R.string.reply) + "(" + replyCnt + ")");

        mListView = (RecyclerView) findViewById(R.id.comment_reply_list);
        //http://stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
        //http://www.kmshack.kr/2014/12/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-listview%EC%97%90%EC%84%9C-recyclerview%EB%A1%9C-%EB%A7%88%EC%9D%B4%EA%B7%B8%EB%A0%88%EC%9D%B4%EC%85%98-%ED%95%98%EA%B8%B0/
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        getCommentReplyList(commentId, conKey);
                    }
                }
            }
        });

        mListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        mListView.setLayoutManager(llm);

        mAdapter = new CommentReplyCardAdapter();
        mListView.setAdapter(mAdapter);

        getCommentReplyList(commentId, "");

        ButtonRectangle btnSend = (ButtonRectangle) findViewById(R.id.btn_reply_send);
        btnSend.setRippleSpeed(36f);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCommentReply(commentId);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mIsChanged) {
            Intent intent = new Intent();
            intent.putExtra("position", mPosition);
            intent.putExtra("replyCnt", String.valueOf(mAdapter.getTotalCount()));
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
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

    private void getCommentReplyList(final String commentId, final String conKey) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        GetCommentReplyList_In input = new GetCommentReplyList_In();
        input.conKey = conKey;
        input.qryCount = 10;
        input.comment_id = commentId;

        String url = ComFunc.getServiceURL("getCommentReplyList");

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
                        GetCommentReplyList_Res res = gson.fromJson(response.toString(), GetCommentReplyList_Res.class);

                        if(res.returnCode.equals("000")) {
                            mAdapter.attach(res);
                            if(listCnt == 0) {
                                mListView.scrollToPosition(0);
                            } else {
                                mListView.scrollToPosition(listCnt-1);
                            }

                            listCnt += res.resultCount;
                            if(listCnt == res.totalCount) {
                                mEOF = true;
                            }

                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                getCommentReplyList(commentId, conKey);
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

    private void requestCommentReply(final String commentId) {
        final EditText etContent = (EditText) findViewById(R.id.et_reply_content);

        if(etContent.getText().toString().length() == 0) {
            SnackBar snackbar = new SnackBar(CommentReplyListActivity.this, getResources().getString(R.string.msg_content));
            snackbar.show();
            etContent.requestFocus();
            return;
        }

        if(etContent.getText().toString().length() > 100) {
            SnackBar snackbar = new SnackBar(CommentReplyListActivity.this, getResources().getString(R.string.comment_hint));
            snackbar.show();
            etContent.requestFocus();
            return;
        }


        RequestQueue requestQueue = Volley.newRequestQueue(this);

        RequestCommentReply_In input = new RequestCommentReply_In();
        input.proc_tp = "1"; // 1.입력, 2.수정, 3.삭제
        input.comment_id = commentId;
        input.member_id = ComFunc.getMemberId(this);
        input.content = etContent.getText().toString();

        String url = ComFunc.getServiceURL("requestCommentReply");

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
                        RequestCommentReply_Res res = gson.fromJson(response.toString(), RequestCommentReply_Res.class);

                        if(res.returnCode.equals("101")) {
                            mIsChanged = true;

                            mAdapter.addItem(res.output);
                            etContent.setText("");
                            mListView.scrollToPosition(0);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                requestCommentReply(commentId);
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

    private void deleteCommentReply(final int position, final String commentReplyId, final String commentId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        RequestCommentReply_In input = new RequestCommentReply_In();
        input.proc_tp = "3"; // 1.입력, 2.수정, 3.삭제
        input.comment_reply_id = commentReplyId;
        input.comment_id = commentId;

        String url = ComFunc.getServiceURL("requestCommentReply");

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
                        RequestCommentReply_Res res = gson.fromJson(response.toString(), RequestCommentReply_Res.class);

                        if(res.returnCode.equals("104")) {
                            mIsChanged = true;

                            mAdapter.deleteData(position);
                        } else {
                            // Show Popup
                            new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                deleteCommentReply(position, commentReplyId, commentId);
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

    private void badComment(final String commentReplyId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        RequestBadComment_In input = new RequestBadComment_In();
        input.proc_tp = "1"; // 1.입력, 2.수정, 3.삭제
        input.member_id = ComFunc.getMemberId(this);
        input.comment_tp = "2"; // 1.코멘트, 2.댓글
        input.comment_id = commentReplyId;
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
                        new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
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
                new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
                        .setTitle(R.string.network_error_title)
                        .setMessage(R.string.network_error_msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Log.d(TAG, "onClick: ok");
                                badComment(commentReplyId);
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

    class CommentReplyCardAdapter extends RecyclerView.Adapter<CommentReplyCardAdapter.ViewHolder> {
        private List<GetCommentReplyList_Out> list;
        private int totalCount;

        public CommentReplyCardAdapter() {
            list = new ArrayList<>();
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void attach(GetCommentReplyList_Res res) {
            totalCount = res.totalCount;
            list.addAll(res.output);
            mAdapter.notifyDataSetChanged();

            setTitle(getResources().getString(R.string.reply) + "(" + totalCount + ")");
        }

        public void addItem(RequestCommentReply_Out item) {
            GetCommentReplyList_Out newItem = new GetCommentReplyList_Out();
            newItem.comment_reply_id = item.comment_reply_id;
            newItem.comment_id = item.comment_id;
            newItem.member_id = item.member_id;
            newItem.nickname = item.nickname;
            newItem.profile_img_url = item.profile_img_url;
            newItem.content = item.content;
            newItem.comment_reply_dt = item.comment_reply_dt;
            newItem.comment_reply_dt2 = item.comment_reply_dt2;
            newItem.comment_reply_dt2_cd = item.comment_reply_dt2_cd;

            list.add(0, newItem);
            listCnt += 1;
            mAdapter.notifyDataSetChanged();

            totalCount += 1;
            setTitle(getResources().getString(R.string.reply) + "(" + totalCount + ")");
        }

        public void clear() {
            list.clear();
            mAdapter.notifyDataSetChanged();
        }

        public void deleteData(int position) {
            list.remove(position);
            listCnt -= 1;
            mAdapter.notifyDataSetChanged();

            totalCount -= 1;
            setTitle(getResources().getString(R.string.reply) + "(" + totalCount + ")");
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

            final GetCommentReplyList_Out item = list.get(position);

            holder.tvNickname.setText(item.nickname);
            holder.tvCmtDt2.setText(item.comment_reply_dt2);
            holder.tvCmtDt2Nm.setText(getCommentDt2Nm(item.comment_reply_dt2_cd));
            holder.tvContent.setText(item.content);
            if(item.profile_img_url != null) {
                Picasso.with(CommentReplyListActivity.this)
                        .load(item.profile_img_url)
                        .placeholder(R.drawable.ic_empty)
                        .error(R.drawable.ic_error)
                        .into(holder.ivProfile);
            } else {
                holder.ivProfile.setImageResource(R.drawable.default_profile_round);
            }

            holder.llEtcForm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.member_id.equals(ComFunc.getMemberId(CommentReplyListActivity.this))) {
                        new MaterialDialog.Builder(CommentReplyListActivity.this)
                                .items(R.array.reply_my_etc_list)
                                .title(item.nickname + "'s " + getResources().getString(R.string.reply))
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        if (which == 0) {
                                            // Show Popup
                                            new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
                                                    .setTitle(R.string.warning)
                                                    .setMessage(R.string.msg_delete_comment_warring)
                                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            deleteCommentReply(position, item.comment_reply_id, item.comment_id);
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
                    } else {
                        new MaterialDialog.Builder(CommentReplyListActivity.this)
                                .items(R.array.reply_etc_list)
                                .title(item.nickname + "'s " + getResources().getString(R.string.reply))
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        if (which == 0) {
                                            // Show Popup
                                            new AlertDialogWrapper.Builder(CommentReplyListActivity.this)
                                                    .setTitle(R.string.warning)
                                                    .setMessage(R.string.msg_bad_comment_warring)
                                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            badComment(item.comment_reply_id);
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
                    inflate(R.layout.card_commentreplylist_item, viewGroup, false);

            return new ViewHolder(itemView);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            //protected CardView cardView;
            protected ImageView ivProfile;
            protected TextView tvNickname;
            protected TextView tvCmtDt2;
            protected TextView tvCmtDt2Nm;
            protected TextView tvContent;
            protected LinearLayout llEtcForm;

            public ViewHolder(View v) {
                super(v);
                //cardView = (CardView) v.findViewById(R.id.card_view);
                ivProfile = (ImageView) v.findViewById(R.id.commentreplylist_iv_profile);
                tvNickname = (TextView) v.findViewById(R.id.commentreplylist_tv_nickname);
                tvCmtDt2 = (TextView) v.findViewById(R.id.commentreplylist_tv_comment_dt2);
                tvCmtDt2Nm = (TextView) v.findViewById(R.id.commentreplylist_tv_comment_dt2_nm);
                tvContent = (TextView) v.findViewById(R.id.commentreplylist_tv_content);
                llEtcForm = (LinearLayout) v.findViewById(R.id.commentreplylist_etc_from);
            }
        }
    }
}
