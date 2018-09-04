package com.zzingobomi.englishforus.myitemmanage;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
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
import com.zzingobomi.englishforus.auth.EmailCreateAccountFragment;
import com.zzingobomi.englishforus.auth.FirebaseTokenManager;
import com.zzingobomi.englishforus.study.StudyFragment;
import com.zzingobomi.englishforus.vo.Item;
import com.zzingobomi.englishforus.vo.ReplyItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
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
public class MyItemManageFragment extends Fragment implements MyItemsRecyclerAdapter.MyItemsRecyclerViewClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Firebase 인증
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    private Button mAddItemButton;

    private RecyclerView mRecyclerView;
    private MyItemsRecyclerAdapter mAdapter;

    private int mCurSelectPosition = -1;


    public MyItemManageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final MyItemManageFragment fragment = this;

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser == null) {
            // 로그인 안되어 있음.. 다시 돌아가기
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_my_item_manage, container, false);

        mAddItemButton = view.findViewById(R.id.addmyitem_btn);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AddItemFragment addItemFragment = new AddItemFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction
                        .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                        .add(R.id.contnet_fragment_layout, addItemFragment)
                        .addToBackStack(null)
                        .commit();

                fragmentTransaction.hide(fragment);
            }
        });

        mRecyclerView = view.findViewById(R.id.myitems_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        // ItemAnimator
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(1000);
        animator.setRemoveDuration(1000);
        animator.setMoveDuration(1000);
        animator.setChangeDuration(1000);
        mRecyclerView.setItemAnimator(animator);

        // ItemDecoration 다른 속성들 있나..?
        //DividerItemDecoration decoration = new DividerItemDecoration(this.getContext(), layoutManager.getOrientation());
        //mRecyclerView.addItemDecoration(decoration);

        new HttpMyItemsAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/myitems");

        return view;
    }

    public void addNewItem(Item item) {
        Log.d(TAG, "New Item : " + item.getIdx() + " / " + item.getTitle_ko());
        mAdapter.addItem(0, item);
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onItemClicked(int position) {
        Log.d(TAG, "onItemClicked: " + position);
    }

    @Override
    public void onModifyButtonClicked(int position, String titleKor, String titleEng, String addInfo) {
        mCurSelectPosition = position;
        Item modifyItem = mAdapter.getItem(position);
        new HttpModifyItemAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/myitem/" + modifyItem.getIdx()
                ,titleKor
                ,titleEng
                ,addInfo
                ,modifyItem.getRegidemail()
        );
    }

    @Override
    public void onDeleteButtonClicked(int position) {
        final MyItemManageFragment fragment = this;
        final int pos = position;
        mCurSelectPosition = position;
        new MaterialDialog.Builder(getContext())
                .title("문장 삭제")
                .content("문장을 정말로 삭제하시겠습니까?")
                .positiveText(R.string.common_agree)
                .negativeText(R.string.common_disagree)
                .positiveColor(Color.BLACK)
                .negativeColor(Color.BLACK)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, "Position : " + pos + " / " + "Idx : " + mAdapter.getItemId(pos));
                        String reqUrl = "http://englishforus.zzingobomi.synology.me/itemapi/myitem/" + mAdapter.getItemId(pos);
                        new HttpMyItemDeleteAsyncTask(fragment).execute(reqUrl);
                    }
                })
                .show();
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
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
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
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
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

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpMyItemsAsyncTask", "UNAUTHORIZED");
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

                // 어댑터 설정
                fragment.mAdapter = new MyItemsRecyclerAdapter(items, fragment.getContext());
                fragment.mAdapter.setOnClickListener(fragment);
                fragment.mRecyclerView.setAdapter(fragment.mAdapter);
            }
        }
    }

    ///
    /// 내 문장 수정하기
    ///
    private static class HttpModifyItemAsyncTask extends AsyncTask<String, Void, Item> {

        private WeakReference<MyItemManageFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpModifyItemAsyncTask(MyItemManageFragment fragment) {
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

            String strUrl = params[0];
            String strTitleKo = params[1];
            String strTitleEn = params[2];
            String strAddInfo = params[3];
            String strRegIdEmail = params[4];
            Item resultItem = null;
            //Item item = new Item(strTitleKo, strTitleEn, strAddInfo, strRegIdEmail, strRegDisplayName);

            try {
                JsonObject json = new JsonObject();
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                json.addProperty("title_ko", strTitleKo);
                json.addProperty("title_en", strTitleEn);
                json.addProperty("addinfo", strAddInfo);
                json.addProperty("regidemail", strRegIdEmail);
                RequestBody requestBody = RequestBody.create(JSON, json.toString());

                /*
                // 다른 좋은 방법 있기 전까지... 그냥 값 집어넣어서 보내기 ItemVO 에 맵핑하는 방법이 있을텐데..
                Gson gson = new Gson();
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8")
                        ,gson.toJson(item));
                        */

                Request request = new Request.Builder()
                        .url(strUrl)
                        .put(requestBody)
                        .build();
                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpModifyItemAsyncTask", "UNAUTHORIZED");
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
                resultItem = gson.fromJson(response.body().string(), listType);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultItem;
        }

        @Override
        protected void onPostExecute(Item resultItem) {
            super.onPostExecute(resultItem);
            waitDialog.dismiss();
            if(resultItem != null) {
                Log.d(TAG, "Response:SUCCESS");

                final MyItemManageFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                if(fragment.mCurSelectPosition != -1) {
                    fragment.mAdapter.modifyItem(fragment.mCurSelectPosition, resultItem);
                    fragment.mCurSelectPosition = -1;
                }
            }
        }
    }

    ///
    /// 내 문장 삭제하기
    ///
    private static class HttpMyItemDeleteAsyncTask extends AsyncTask<String, Void, String> {
        private WeakReference<MyItemManageFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpMyItemDeleteAsyncTask(MyItemManageFragment fragment) {
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

            final MyItemManageFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isDetached()) return "E_FAIL";

            String result = null;
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
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                RequestBody body = RequestBody.create(JSON, json.toString());

                Request request = new Request.Builder()
                        .url(strUrl)
                        .delete(body)
                        .build();

                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("UNAUTHORIZED", "HttpMyItemDeleteAsyncTask");
                    return null;
                }

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
                Log.d("MyItemDelete : ", result);

                if(result.equals("SUCCESS")) {
                    MyItemManageFragment fragment = fragmentWeakReference.get();
                    if(fragment == null || fragment.isDetached()) return;

                    if(fragment.mCurSelectPosition != -1) {
                        fragment.mAdapter.removeItem(fragment.mCurSelectPosition);
                        fragment.mCurSelectPosition = -1;
                    }
                }
            }
        }
    }


    //endregion

}
