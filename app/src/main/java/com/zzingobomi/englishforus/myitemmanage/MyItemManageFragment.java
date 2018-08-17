package com.zzingobomi.englishforus.myitemmanage;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.study.StudyFragment;
import com.zzingobomi.englishforus.vo.Item;
import com.zzingobomi.englishforus.vo.ReplyItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyItemManageFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Firebase 인증
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    private List<Item> mCurItems;


    public MyItemManageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser == null) {
            // 로그인 안되어 있음.. 다시 돌아가기
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_my_item_manage, container, false);

        // 레이아웃들..


        new HttpMyItemsAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/myitems");

        return view;
    }





    //region 네트워크 영역

    ///
    /// 내 문장들 목록 받아오기
    ///
    private static class HttpMyItemsAsyncTask extends AsyncTask<String, Void, List<Item>> {

        private WeakReference<MyItemManageFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpMyItemsAsyncTask(MyItemManageFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.wait_progress_title)
                    .content(R.string.wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected List<Item> doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            List<Item> items = new ArrayList<>();
            String strUrl = params[0];
            try {
                JsonObject json = new JsonObject();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null) {
                    json.addProperty("regidemail", firebaseUser.getEmail());
                } else {
                    json.addProperty("regidemail", "");
                }
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(strUrl)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();

                // 현재 Item 에는 시간이 없지만 시간순으로 뿌려주기 위해 보내줘야 할듯..
                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<List<Item>>() {}.getType();
                items = gson.fromJson(response.body().string(), listType);

                Log.d(TAG, "onCreate: " + items.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            super.onPostExecute(items);
            waitDialog.dismiss();
            if(items != null) {
                Log.d("HttpMyItemsAsyncTask", items.toString());

                final MyItemManageFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mCurItems = items;

                for(int i = 0; i < items.size(); i++) {
                    Log.d("ITEM", items.get(i).getTitle_ko());
                }











                /*
                // 댓글보기 최상위 Layout
                LinearLayout replyLayout = fragment.getView().findViewById(R.id.reply_linear_layout);

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                LinearLayout replyCreateLayout = (LinearLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.reply_create, null);
                LinearLayout loginLayout = replyCreateLayout.findViewById(R.id.reply_login_layout);
                LinearLayout logoutLayout = replyCreateLayout.findViewById(R.id.reply_logout_layout);
                if(firebaseUser != null) {
                    // 로그인한 유저만 댓글 달기 폼 보여주기
                    loginLayout.setVisibility(View.VISIBLE);
                    logoutLayout.setVisibility(View.GONE);

                    final TextView replyCreateText = replyCreateLayout.findViewById(R.id.reply_create_text);
                    TextView replyCreateBtn = replyCreateLayout.findViewById(R.id.reply_create_btn);

                    replyCreateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String reqUrl = "http://englishforus.zzingobomi.synology.me/replies";
                            new HttpReplyCreateAsyncTask(fragment).execute(reqUrl, replyCreateText.getText().toString());
                        }
                    });

                } else {
                    // 로그인 안한 유저는 로그인 유도창 보여주기
                    loginLayout.setVisibility(View.GONE);
                    logoutLayout.setVisibility(View.VISIBLE);
                }

                replyLayout.addView(replyCreateLayout);

                // 댓글들 목록 보여주기
                for(int i = 0; i < replies.size(); i++) {
                    LinearLayout replyItemLayout = (LinearLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.item_reply_card, null);

                    final TextView replyIdx = replyItemLayout.findViewById(R.id.reply_idx);
                    TextView replyDisplayName = replyItemLayout.findViewById(R.id.reply_regdisplayname);
                    final TextView replyContents = replyItemLayout.findViewById(R.id.reply_contents);
                    final EditText replyContentsEdit = replyItemLayout.findViewById(R.id.reply_contents_edit);
                    TextView replyRegDate = replyItemLayout.findViewById(R.id.reply_regdate);
                    ImageView replyRegPhoto = replyItemLayout.findViewById(R.id.reply_account_photo);

                    final TextView replyModify = replyItemLayout.findViewById(R.id.reply_modify);
                    final TextView replyModifyCancel = replyItemLayout.findViewById(R.id.reply_modify_cancel);
                    TextView replyDelete = replyItemLayout.findViewById(R.id.reply_delete);
                    TextView replyBad = replyItemLayout.findViewById(R.id.bad_reply);
                    TextView replyLike = replyItemLayout.findViewById(R.id.like_reply);

                    final ReplyItem replyItem = replies.get(i);

                    // 각 댓글들의 정보 셋팅
                    replyIdx.setText(String.valueOf(replyItem.getIdx()));
                    replyDisplayName.setText(replyItem.getRegdisplayname() + " | ");
                    replyContents.setText(replyItem.getReplytext());
                    replyContentsEdit.setText(replyItem.getReplytext());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    replyRegDate.setText(simpleDateFormat.format(replyItem.getRegdate()));

                    RequestOptions options = new RequestOptions();
                    options.circleCrop();
                    if(replyItem.getRegphotourl() != null) {
                        Glide.with(fragment.getActivity())
                                .load(replyItem.getRegphotourl())
                                .apply(options)
                                .into(replyRegPhoto);
                    } else {
                        Glide.with(fragment.getActivity())
                                .load(R.drawable.ic_anonymous_user)
                                .apply(options)
                                .into(replyRegPhoto);
                    }

                    // 받아온 정보에 따라 다르게 버튼 셋팅
                    if(replyItem.isLikestate()) {
                        replyLike.setText(fragment.getActivity().getString(R.string.likecancelbtn_text) + " [" + replyItem.getLikecnt() + "]");
                    } else {
                        replyLike.setText(fragment.getActivity().getString(R.string.likebtn_text) + " [" + replyItem.getLikecnt() + "]");
                    }
                    if(replyItem.isBadstate()) {
                        replyBad.setText(fragment.getActivity().getString(R.string.badcancelbtn_text) + " [" + replyItem.getBadcnt() + "]");
                    } else {
                        replyBad.setText(fragment.getActivity().getString(R.string.badbtn_text) + " [" + replyItem.getBadcnt() + "]");
                    }

                    // 각 댓글들의 버튼 셋팅
                    if(firebaseUser != null) {
                        if(firebaseUser.getEmail().equals(replyItem.getRegidemail())) {
                            replyModify.setVisibility(View.VISIBLE);
                            replyDelete.setVisibility(View.VISIBLE);
                            replyBad.setVisibility(View.VISIBLE);
                            replyLike.setVisibility(View.VISIBLE);
                        } else {
                            replyModify.setVisibility(View.GONE);
                            replyDelete.setVisibility(View.GONE);
                            replyBad.setVisibility(View.VISIBLE);
                            replyLike.setVisibility(View.VISIBLE);
                        }
                    } else {
                        replyModify.setVisibility(View.GONE);
                        replyDelete.setVisibility(View.GONE);
                        replyBad.setVisibility(View.VISIBLE);
                        replyLike.setVisibility(View.VISIBLE);
                    }

                    replyModify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(replyModify.getText().toString().equals("저장")) {
                                String reqUrl = "http://englishforus.zzingobomi.synology.me/replies/" + replyIdx.getText().toString();
                                new HttpReplyModifyAsyncTask(fragment).execute(reqUrl, replyContentsEdit.getText().toString());
                            } else {
                                replyContents.setVisibility(View.GONE);
                                replyContentsEdit.setVisibility(View.VISIBLE);

                                replyModify.setText("저장");
                                replyModifyCancel.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    replyModifyCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            replyContents.setVisibility(View.VISIBLE);
                            replyContentsEdit.setVisibility(View.GONE);

                            replyModify.setText("수정");
                            replyModifyCancel.setVisibility(View.GONE);
                        }
                    });
                    replyDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(fragment.getContext())
                                    .title(R.string.reply_delete_title)
                                    .content(R.string.reply_delete_content)
                                    .positiveText(R.string.agree)
                                    .negativeText(R.string.disagree)
                                    .positiveColor(Color.BLACK)
                                    .negativeColor(Color.BLACK)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String reqUrl = "http://englishforus.zzingobomi.synology.me/replies/" + String.valueOf(replyItem.getIdx());
                                            new HttpReplyDeleteAsyncTask(fragment).execute(reqUrl);
                                        }
                                    })
                                    .show();
                        }
                    });
                    replyLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(firebaseUser != null) {
                                String reqUrl = "http://englishforus.zzingobomi.synology.me/replies/like/" + String.valueOf(replyItem.getIdx());
                                for(int i = 0; i < fragment.mCurReplies.size(); i++) {
                                    if(fragment.mCurReplies.get(i).getIdx() == replyItem.getIdx()) {
                                        new HttpReplyLikeAsyncTask(fragment).execute(reqUrl, firebaseUser.getEmail(), String.valueOf(fragment.mCurReplies.get(i).isLikestate()));
                                        break;
                                    }
                                }
                            } else {
                                Toast.makeText(fragment.getContext(), fragment.getContext().getString(R.string.need_login), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    replyBad.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(firebaseUser != null) {
                                String reqUrl = "http://englishforus.zzingobomi.synology.me/replies/bad/" + String.valueOf(replyItem.getIdx());
                                for(int i = 0; i < fragment.mCurReplies.size(); i++) {
                                    if(fragment.mCurReplies.get(i).getIdx() == replyItem.getIdx()) {
                                        new HttpReplyBadAsyncTask(fragment).execute(reqUrl, firebaseUser.getEmail(), String.valueOf(fragment.mCurReplies.get(i).isBadstate()));
                                        break;
                                    }
                                }
                            } else {
                                Toast.makeText(fragment.getContext(), fragment.getContext().getString(R.string.need_login), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    replyLayout.addView(replyItemLayout);
                }
                */
            }
        }
    }


    //endregion

}
