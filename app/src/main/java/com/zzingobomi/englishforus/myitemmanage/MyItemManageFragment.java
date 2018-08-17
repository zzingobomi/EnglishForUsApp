package com.zzingobomi.englishforus.myitemmanage;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
public class MyItemManageFragment extends Fragment implements MyItemsRecyclerAdapter.MyItemsRecyclerViewClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Firebase 인증
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    private Button mAddItemButton;

    private List<Item> mCurItems;
    private RecyclerView mRecyclerView;
    private MyItemsRecyclerAdapter mAdapter;


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

        mAddItemButton = view.findViewById(R.id.addmyitem_btn);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 내 문장 추가 화면으로 전환
                AddItemFragment addItemFragment = new AddItemFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contnet_fragment_layout, addItemFragment)
                        .addToBackStack(null)
                        .commit();
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

        // ItemDecoration
        DividerItemDecoration decoration = new DividerItemDecoration(this.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(decoration);

        new HttpMyItemsAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/myitems");

        return view;
    }

    @Override
    public void onItemClicked(int position) {
        Log.d(TAG, "onItemClicked: " + position);
    }

    @Override
    public void onModifyButtonClicked(int position) {
        Log.d(TAG, "onModifyButtonClicked: " + position);
    }

    @Override
    public void onDeleteButtonClicked(int position) {
        Log.d(TAG, "onDeleteButtonClicked: " + position);
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

                // 어댑터 설정
                fragment.mAdapter = new MyItemsRecyclerAdapter(items);
                fragment.mAdapter.setOnClickListener(fragment);
                fragment.mRecyclerView.setAdapter(fragment.mAdapter);
            }
        }
    }


    //endregion

}
