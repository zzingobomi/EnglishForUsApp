package com.zzingobomi.englishforus.study;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.zzingobomi.englishforus.auth.FirebaseTokenManager;
import com.zzingobomi.englishforus.vo.Item;
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.ReplyItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
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
public class StudyFragment extends Fragment {

    public interface OnStudyCountPlusListener {
        void onStudyCountPlusListener();
    }
    private OnStudyCountPlusListener mStudyCountListener;

    // 현재 어떤 문장이 나와있는지 상태를 나타낸다.
    enum State {
        NONE,
        SHOW_KO,
        SHOW_EN
    }

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // 속성들
    private int itemIdx;
    private ScrollView mStudyScrollView;
    private LinearLayout mLinearLayout;
    private TextView mTitle_KO;
    private TextView mTitle_EN;
    private TextView mAddInfo;
    private TextView mRegDisplayName;
    private Button mReplyShowBtn;
    private Button mLikeBtn;
    private Button mBadBtn;

    // 레이아웃
    private LinearLayout mKorLinearLayout;
    private LinearLayout mEngLinearLayout;
    private LinearLayout mAddinfoLinearLayout;
    private LinearLayout mButtonsLinearLayout;
    private LinearLayout mReplyLinearLayout;

    // 문장 상태
    private State mCurrentState = State.NONE;

    // 좋아요, 신고하기 상태값
    private Item mCurItem;
    private List<ReplyItem> mCurReplies;

    // 애니메이션
    private Animation mTitle_KO_AlphaAnim;
    private Animation mTitle_EN_AlphaAnim;

    public StudyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mStudyCountListener = (OnStudyCountPlusListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity)context).getLocalClassName() + " 는 OnStudyCountPlusListener를 구현해야 합니다.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_study, container, false);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("onGlobalLayout", "onGlobalLayout");
                resizeContents();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        mStudyScrollView = view.findViewById(R.id.study_fragment_scrollview);
        mLinearLayout = view.findViewById(R.id.study_fragment_layout);
        mTitle_KO = view.findViewById(R.id.title_ko);
        mTitle_EN = view.findViewById(R.id.title_en);
        mAddInfo = view.findViewById(R.id.addinfo);
        mRegDisplayName = view.findViewById(R.id.regdisplayname);

        mReplyShowBtn = view.findViewById(R.id.replyshow_btn);
        mLikeBtn = view.findViewById(R.id.like_btn);
        mBadBtn = view.findViewById(R.id.bad_btn);

        mKorLinearLayout = view.findViewById(R.id.kor_linear_layout);
        mEngLinearLayout = view.findViewById(R.id.eng_linear_layout);
        mAddinfoLinearLayout = view.findViewById(R.id.addinfo_linear_layout);
        mButtonsLinearLayout = view.findViewById(R.id.buttons_linear_layout);
        mReplyLinearLayout = view.findViewById(R.id.reply_linear_layout);

        mKorLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickmLinearLayout(v);
            }
        });
        mEngLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickmLinearLayout(v);
            }
        });
        mAddinfoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickmLinearLayout(v);
            }
        });


        mReplyLinearLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d("addOnLayoutChange", "addOnLayoutChange");

                int x = mButtonsLinearLayout.getLeft();
                int y = mButtonsLinearLayout.getTop();

                try {
                    ObjectAnimator animator = ObjectAnimator.ofInt(mStudyScrollView, "scrollY", y);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(500);
                    animator.start();

                    // AccelerateDecelerateInterpolator, AccelerateInterpolator, BounceInterpolator, DecelerateInterpolator
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mReplyShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickmReplyShowBtn(v);
            }
        });

        mLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickmLikeBtn(v);
            }
        });

        mBadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickmBadBtn(v);
            }
        });

        new HttpOneItemAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/onerandomitem");

        mTitle_KO_AlphaAnim = AnimationUtils.loadAnimation(getContext(), R.anim.study_alpha_anim);
        mTitle_KO_AlphaAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitle_KO.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTitle_EN_AlphaAnim = AnimationUtils.loadAnimation(getContext(), R.anim.study_alpha_anim);
        mTitle_EN_AlphaAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitle_EN.setVisibility(View.VISIBLE);
                mAddInfo.setVisibility(View.VISIBLE);
                mRegDisplayName.setVisibility((View.VISIBLE));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        return view;
    }

    private void onClickmLinearLayout(View v) {
        if(mCurrentState == State.SHOW_KO) {
            mCurrentState = State.SHOW_EN;

            mTitle_EN.startAnimation(mTitle_EN_AlphaAnim);
            mAddInfo.startAnimation(mTitle_EN_AlphaAnim);
            mRegDisplayName.startAnimation(mTitle_EN_AlphaAnim);
        } else {
            mTitle_KO.setVisibility(View.INVISIBLE);
            mTitle_EN.setVisibility(View.INVISIBLE);
            mAddInfo.setVisibility(View.INVISIBLE);
            mRegDisplayName.setVisibility((View.INVISIBLE));

            mReplyLinearLayout.removeAllViewsInLayout();

            ObjectAnimator animator = ObjectAnimator.ofInt(mStudyScrollView, "scrollY", 0);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(500);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mReplyLinearLayout.requestLayout();     // 늘어난 스크롤 재조정
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            new HttpOneItemAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/onerandomitem");
        }
    }

    private void onClickmReplyShowBtn(View v) {

        mReplyLinearLayout.removeAllViewsInLayout();
        String reqUrl = "http://englishforus.zzingobomi.synology.me/replies/all/" + itemIdx;
        new HttpReplyAsyncTask(this).execute(reqUrl);
    }

    private void onClickmLikeBtn(View v) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null) {
            String reqUrl = "http://englishforus.zzingobomi.synology.me/itemapi/like/" + itemIdx;
            String regIdEmail = firebaseUser.getEmail();

            new HttpItemLikeAsyncTask(this).execute(reqUrl, regIdEmail, String.valueOf(mCurItem.isLikestate()));
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.common_need_login), Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickmBadBtn(View v) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null) {
            String reqUrl = "http://englishforus.zzingobomi.synology.me/itemapi/bad/" + itemIdx;
            String regIdEmail = firebaseUser.getEmail();

            new HttpItemBadAsyncTask(this).execute(reqUrl, regIdEmail, String.valueOf(mCurItem.isBadstate()));
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.common_need_login), Toast.LENGTH_SHORT).show();
        }
    }

    // 1/3, 1/3, 1/3, 0.1 의 크기로 각 콘텐츠들의 크기를 다시 맞춘다.
    private void resizeContents() {
        int scrollHeight = mStudyScrollView.getHeight();

        LinearLayout.LayoutParams korParams = (LinearLayout.LayoutParams) mKorLinearLayout.getLayoutParams();
        korParams.height = (int)(scrollHeight * 0.3);
        mKorLinearLayout.setLayoutParams(korParams);

        LinearLayout.LayoutParams engParams = (LinearLayout.LayoutParams) mEngLinearLayout.getLayoutParams();
        engParams.height = (int)(scrollHeight * 0.3);
        mEngLinearLayout.setLayoutParams(engParams);

        LinearLayout.LayoutParams addinfoParams = (LinearLayout.LayoutParams) mAddinfoLinearLayout.getLayoutParams();
        addinfoParams.height = (int)(scrollHeight * 0.3);
        mAddinfoLinearLayout.setLayoutParams(addinfoParams);

        LinearLayout.LayoutParams buttonsParams = (LinearLayout.LayoutParams) mButtonsLinearLayout.getLayoutParams();
        buttonsParams.height = (int)(scrollHeight * 0.1);
        mButtonsLinearLayout.setLayoutParams(buttonsParams);
    }

    //region 네트워크 영역

    ///
    /// 문장 받아오기
    ///
    private static class HttpOneItemAsyncTask extends AsyncTask<String, Void, Item> {

        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpOneItemAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Item doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            Item item = new Item();
            String strUrl = params[0];
            try {
                JsonObject json = new JsonObject();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null) {
                    json.addProperty("requestidemail", firebaseUser.getEmail());
                } else {
                    json.addProperty("requestidemail", "");
                }
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(strUrl)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<Item>() {}.getType();
                item = gson.fromJson(response.body().string(), listType);

                Log.d(TAG, "onCreate: " + item.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return item;
        }

        @Override
        protected void onPostExecute(Item item) {
            super.onPostExecute(item);
            waitDialog.dismiss();
            if(item != null) {
                Log.d("HttpOneItemAsyncTask", item.toString());

                StudyFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mCurrentState = State.SHOW_KO;
                fragment.itemIdx = item.getIdx();
                fragment.mCurItem = item;

                TextView title_ko = fragment.getView().findViewById(R.id.title_ko);
                title_ko.setText(item.getTitle_ko());
                TextView title_en = fragment.getView().findViewById(R.id.title_en);
                title_en.setText(item.getTitle_en());
                TextView addinfo = fragment.getView().findViewById(R.id.addinfo);
                addinfo.setText(item.getAddinfo());
                TextView displayname = fragment.getView().findViewById(R.id.regdisplayname);
                displayname.setText("Made By " + item.getRegdisplayname());

                Button replybtn = fragment.getView().findViewById(R.id.replyshow_btn);
                replybtn.setText(fragment.getActivity().getString(R.string.study_replyshowbtn_text) + " [" + item.getReplycnt() + "]");

                Button likebtn = fragment.getView().findViewById(R.id.like_btn);
                if(item.isLikestate()) {
                    likebtn.setText(fragment.getActivity().getString(R.string.common_likecancelbtn_text) + " [" + item.getLikecnt() + "]");
                } else {
                    likebtn.setText(fragment.getActivity().getString(R.string.common_likebtn_text) + " [" + item.getLikecnt() + "]");
                }
                Button badbtn = fragment.getView().findViewById(R.id.bad_btn);
                if(item.isBadstate()) {
                    badbtn.setText(fragment.getActivity().getString(R.string.common_badcancelbtn_text) + " [" + item.getBadcnt() + "]");
                } else {
                    badbtn.setText(fragment.getActivity().getString(R.string.common_badbtn_text) + " [" + item.getBadcnt() + "]");
                }

                title_en.setVisibility(View.INVISIBLE);
                addinfo.setVisibility(View.INVISIBLE);
                displayname.setVisibility((View.INVISIBLE));

                Animation animation = fragment.mTitle_KO_AlphaAnim;
                title_ko.startAnimation(animation);

                // 공부하기 한번 할때마다 공부 횟수 늘리기
                if(fragment.mStudyCountListener != null) {
                    fragment.mStudyCountListener.onStudyCountPlusListener();
                }
            }
        }
    }

    ///
    /// 좋아요
    ///
    private static class HttpItemLikeAsyncTask extends AsyncTask<String, Void, Item> {

        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpItemLikeAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Item doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return null;

            Item item = new Item();
            String strUrl = params[0];
            String strRegIdEmail = params[1];
            Boolean bLike = Boolean.valueOf(params[2]);

            try {
                JsonObject json = new JsonObject();
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                json.addProperty("regidemail", strRegIdEmail);
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = null;

                if(!bLike) {
                    // 좋아요
                    request = new Request.Builder()
                            .url(strUrl)
                            .post(body)
                            .build();
                } else {
                    // 좋아요 취소
                    request = new Request.Builder()
                            .url(strUrl)
                            .delete(body)
                            .build();
                }

                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpItemLike", "UNAUTHORIZED");
                    return null;
                }

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<Item>() {}.getType();
                item = gson.fromJson(response.body().string(), listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return item;
        }

        @Override
        protected void onPostExecute(Item item) {
            super.onPostExecute(item);
            waitDialog.dismiss();
            if(item != null) {
                Log.d("HttpItemLike : ", item.toString());

                StudyFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mCurItem = item;

                Button likeBtn = fragment.getView().findViewById(R.id.like_btn);
                if(item.isLikestate()) {
                    likeBtn.setText(fragment.getActivity().getString(R.string.common_likecancelbtn_text) + " [" + item.getLikecnt() + "]");
                } else {
                    likeBtn.setText(fragment.getActivity().getString(R.string.common_likebtn_text) + " [" + item.getLikecnt() + "]");
                }
            }
        }
    }

    ///
    /// 신고하기
    ///
    private static class HttpItemBadAsyncTask extends AsyncTask<String, Void, Item> {

        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpItemBadAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Item doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return null;

            Item item = new Item();
            String strUrl = params[0];
            String strRegIdEmail = params[1];
            Boolean bBad = Boolean.valueOf(params[2]);

            Log.d("HttpItemBad 1: ", bBad.toString());

            try {
                JsonObject json = new JsonObject();
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                json.addProperty("regidemail", strRegIdEmail);
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = null;

                if(!bBad) {
                    // 신고하기
                    request = new Request.Builder()
                            .url(strUrl)
                            .post(body)
                            .build();
                } else {
                    // 신고하기 취소
                    request = new Request.Builder()
                            .url(strUrl)
                            .delete(body)
                            .build();
                }

                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpItemLike", "UNAUTHORIZED");
                    return null;
                }

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<Item>() {}.getType();
                item = gson.fromJson(response.body().string(), listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return item;
        }

        @Override
        protected void onPostExecute(Item item) {
            super.onPostExecute(item);
            waitDialog.dismiss();
            if(item != null) {
                Log.d("HttpItemBad : ", item.toString());

                StudyFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mCurItem = item;

                Button badBtn = fragment.getView().findViewById(R.id.bad_btn);
                if(item.isBadstate()) {
                    badBtn.setText(fragment.getActivity().getString(R.string.common_badcancelbtn_text) + " [" + item.getBadcnt() + "]");
                } else {
                    badBtn.setText(fragment.getActivity().getString(R.string.common_badbtn_text) + " [" + item.getBadcnt() + "]");
                }
            }
        }
    }


    ///
    /// 댓글 목록 받아오기
    ///
    private static class HttpReplyAsyncTask extends AsyncTask<String, Void, List<ReplyItem>> {

        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpReplyAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected List<ReplyItem> doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            List<ReplyItem> replies = new ArrayList<>();
            String strUrl = params[0];
            try {
                JsonObject json = new JsonObject();
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null) {
                    json.addProperty("requestidemail", firebaseUser.getEmail());
                } else {
                    json.addProperty("requestidemail", "");
                }
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(strUrl)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<List<ReplyItem>>() {}.getType();
                replies = gson.fromJson(response.body().string(), listType);

                Log.d(TAG, "onCreate: " + replies.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return replies;
        }

        @Override
        protected void onPostExecute(List<ReplyItem> replies) {
            super.onPostExecute(replies);
            waitDialog.dismiss();
            if(replies != null) {
                Log.d("HttpReplyAsyncTask", replies.toString());

                final StudyFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mCurReplies = replies;

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
                        replyLike.setText(fragment.getActivity().getString(R.string.common_likecancelbtn_text) + " [" + replyItem.getLikecnt() + "]");
                    } else {
                        replyLike.setText(fragment.getActivity().getString(R.string.common_likebtn_text) + " [" + replyItem.getLikecnt() + "]");
                    }
                    if(replyItem.isBadstate()) {
                        replyBad.setText(fragment.getActivity().getString(R.string.common_badcancelbtn_text) + " [" + replyItem.getBadcnt() + "]");
                    } else {
                        replyBad.setText(fragment.getActivity().getString(R.string.common_badbtn_text) + " [" + replyItem.getBadcnt() + "]");
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
                            if(replyModify.getText().toString().equals(fragment.getResources().getString(R.string.common_modify_save_text))) {
                                String reqUrl = "http://englishforus.zzingobomi.synology.me/replies/" + replyIdx.getText().toString();
                                new HttpReplyModifyAsyncTask(fragment).execute(reqUrl, replyContentsEdit.getText().toString());
                            } else {
                                replyContents.setVisibility(View.GONE);
                                replyContentsEdit.setVisibility(View.VISIBLE);

                                replyModify.setText(fragment.getResources().getString(R.string.common_modify_save_text));
                                replyModifyCancel.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    replyModifyCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            replyContents.setVisibility(View.VISIBLE);
                            replyContentsEdit.setVisibility(View.GONE);

                            replyModify.setText(fragment.getResources().getString(R.string.common_modify_text));
                            replyModifyCancel.setVisibility(View.GONE);
                        }
                    });
                    replyDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(fragment.getContext())
                                    .title(R.string.study_reply_delete_title)
                                    .content(R.string.study_reply_delete_content)
                                    .positiveText(R.string.common_agree)
                                    .negativeText(R.string.common_disagree)
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
                                Toast.makeText(fragment.getContext(), fragment.getContext().getString(R.string.common_need_login), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(fragment.getContext(), fragment.getContext().getString(R.string.common_need_login), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    replyLayout.addView(replyItemLayout);
                }
            }
        }
    }

    ///
    /// 댓글 좋아요
    ///
    private static class HttpReplyLikeAsyncTask extends AsyncTask<String, Void, ReplyItem> {

        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpReplyLikeAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected ReplyItem doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return null;

            ReplyItem replyItem = new ReplyItem();
            String strUrl = params[0];
            String strRegIdEmail = params[1];
            Boolean bLike = Boolean.valueOf(params[2]);

            try {
                JsonObject json = new JsonObject();
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                json.addProperty("regidemail", strRegIdEmail);
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = null;

                if(!bLike) {
                    // 좋아요
                    request = new Request.Builder()
                            .url(strUrl)
                            .post(body)
                            .build();
                } else {
                    // 좋아요 취소
                    request = new Request.Builder()
                            .url(strUrl)
                            .delete(body)
                            .build();
                }

                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpItemLike", "UNAUTHORIZED");
                    return null;
                }

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<ReplyItem>() {}.getType();
                replyItem = gson.fromJson(response.body().string(), listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return replyItem;
        }

        @Override
        protected void onPostExecute(ReplyItem replyItem) {
            super.onPostExecute(replyItem);
            waitDialog.dismiss();
            if(replyItem != null) {
                Log.d("HttpReplyLike : ", replyItem.toString());

                StudyFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                // mCurReplies 정보 변경
                for(int i = 0; i < fragment.mCurReplies.size(); i++) {
                    if(fragment.mCurReplies.get(i).getIdx() == replyItem.getIdx()) {
                        fragment.mCurReplies.set(i, replyItem);
                        break;
                    }
                }

                // 실제 UI Element 변경
                for(int i = 0; i < fragment.mReplyLinearLayout.getChildCount(); i++) {
                    TextView replyIdx = fragment.mReplyLinearLayout.getChildAt(i).findViewById(R.id.reply_idx);
                    if(replyIdx != null && replyIdx.getText().toString().equals(String.valueOf(replyItem.getIdx()))) {
                        TextView replyLike = fragment.mReplyLinearLayout.getChildAt(i).findViewById(R.id.like_reply);
                        if(replyItem.isLikestate()) {
                            replyLike.setText(fragment.getActivity().getString(R.string.common_likecancelbtn_text) + " [" + replyItem.getLikecnt() + "]");
                        } else {
                            replyLike.setText(fragment.getActivity().getString(R.string.common_likebtn_text) + " [" + replyItem.getLikecnt() + "]");
                        }
                        break;
                    }
                }
            }
        }
    }

    ///
    /// 댓글 신고하기
    ///
    private static class HttpReplyBadAsyncTask extends AsyncTask<String, Void, ReplyItem> {

        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpReplyBadAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();

        }

        @Override
        protected ReplyItem doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return null;

            ReplyItem replyItem = new ReplyItem();
            String strUrl = params[0];
            String strRegIdEmail = params[1];
            Boolean bBad = Boolean.valueOf(params[2]);

            try {
                JsonObject json = new JsonObject();
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                json.addProperty("regidemail", strRegIdEmail);
                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = null;

                if(!bBad) {
                    // 신고하기
                    request = new Request.Builder()
                            .url(strUrl)
                            .post(body)
                            .build();
                } else {
                    // 신고하기 취소
                    request = new Request.Builder()
                            .url(strUrl)
                            .delete(body)
                            .build();
                }

                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpItemLike", "UNAUTHORIZED");
                    return null;
                }

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<ReplyItem>() {}.getType();
                replyItem = gson.fromJson(response.body().string(), listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return replyItem;
        }

        @Override
        protected void onPostExecute(ReplyItem replyItem) {
            super.onPostExecute(replyItem);
            waitDialog.dismiss();
            if(replyItem != null) {
                Log.d("HttpReplyBad : ", replyItem.toString());

                StudyFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                // mCurReplies 정보 변경
                for(int i = 0; i < fragment.mCurReplies.size(); i++) {
                    if(fragment.mCurReplies.get(i).getIdx() == replyItem.getIdx()) {
                        fragment.mCurReplies.set(i, replyItem);
                        break;
                    }
                }

                // 실제 UI Element 변경
                for(int i = 0; i < fragment.mReplyLinearLayout.getChildCount(); i++) {
                    TextView replyIdx = fragment.mReplyLinearLayout.getChildAt(i).findViewById(R.id.reply_idx);
                    if(replyIdx != null && replyIdx.getText().toString().equals(String.valueOf(replyItem.getIdx()))) {
                        TextView replyBad = fragment.mReplyLinearLayout.getChildAt(i).findViewById(R.id.bad_reply);
                        if(replyItem.isBadstate()) {
                            replyBad.setText(fragment.getActivity().getString(R.string.common_badcancelbtn_text) + " [" + replyItem.getBadcnt() + "]");
                        } else {
                            replyBad.setText(fragment.getActivity().getString(R.string.common_badbtn_text) + " [" + replyItem.getBadcnt() + "]");
                        }
                        break;
                    }
                }
            }
        }
    }

    ///
    /// 댓글 추가하기
    ///
    private static class HttpReplyCreateAsyncTask extends AsyncTask<String, Void, String> {
        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpReplyCreateAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return "E_FAIL";

            String result = null;
            String strUrl = params[0];

            int itemIdx = fragment.itemIdx;
            String replyText = params[1];
            String regidEmail = "";
            String regDisplayMame = "";
            String regPhotoUrl = "";

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) {
                regidEmail = firebaseUser.getEmail();
                regDisplayMame = firebaseUser.getDisplayName();
                if(firebaseUser.getPhotoUrl() != null) {
                    regPhotoUrl = firebaseUser.getPhotoUrl().toString();
                }
            }

            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("itemidx", String.valueOf(itemIdx));
                jsonObject.addProperty("replytext", replyText);
                jsonObject.addProperty("regidemail", regidEmail);
                jsonObject.addProperty("regdisplayname", regDisplayMame);
                jsonObject.addProperty("regphotourl", regPhotoUrl);

                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url(strUrl)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            waitDialog.dismiss();
            if(result != null) {
                Log.d("HttpReplyCreate : ", result);

                if(result.equals("SUCCESS")) {
                    StudyFragment fragment = fragmentWeakReference.get();
                    if(fragment == null || fragment.isDetached()) return;

                    fragment.onClickmReplyShowBtn(null);
                }
            }
        }
    }

    ///
    /// 댓글 수정하기
    ///
    private static class HttpReplyModifyAsyncTask extends AsyncTask<String, Void, String> {
        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpReplyModifyAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return "E_FAIL";

            String result = null;
            String strUrl = params[0];

            int itemIdx = fragment.itemIdx;
            String replyText = params[1];

            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("replytext", replyText);

                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Request request = new Request.Builder()
                        .url(strUrl)
                        .put(body)
                        .build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            waitDialog.dismiss();
            if(result != null) {
                Log.d("HttpReplyModify : ", result);

                if(result.equals("SUCCESS")) {
                    StudyFragment fragment = fragmentWeakReference.get();
                    if(fragment == null || fragment.isDetached()) return;

                    fragment.onClickmReplyShowBtn(null);
                }
            }
        }
    }

    ///
    /// 댓글 삭제하기
    ///
    private static class HttpReplyDeleteAsyncTask extends AsyncTask<String, Void, String> {
        private WeakReference<StudyFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpReplyDeleteAsyncTask(StudyFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            final StudyFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return "E_FAIL";

            String result = null;
            String strUrl = params[0];

            try {
                Request request = new Request.Builder()
                        .url(strUrl)
                        .delete()
                        .build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            waitDialog.dismiss();
            if(result != null) {
                Log.d("HttpReplyDelete : ", result);

                if(result.equals("SUCCESS")) {
                    StudyFragment fragment = fragmentWeakReference.get();
                    if(fragment == null || fragment.isDetached()) return;

                    fragment.onClickmReplyShowBtn(null);
                }
            }
        }
    }

    //endregion
}
