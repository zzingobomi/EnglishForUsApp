package com.zzingobomi.hellohappy.ranking;


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

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.zzingobomi.hellohappy.MainActivity;
import com.zzingobomi.hellohappy.R;
import com.zzingobomi.hellohappy.vo.RankingManyItemVO;

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
public class RankingManyItemFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private RecyclerView mRecyclerView;
    private RankingManyItemRecyclerAdapter mAdapter;

    public RankingManyItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking_many_item, container, false);

        mRecyclerView = view.findViewById(R.id.ranking_manyitem_recycler_view);
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
        DividerItemDecoration decoration = new DividerItemDecoration(this.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(decoration);

        new HttpRankManyItemsAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/rankingapi/topregistuser", "5");

        return view;
    }



    //region 네트워크 영역

    ///
    /// ManyItem 순위 받아오기(가장 많이 아이템을 등록한 유저)
    ///
    private static class HttpRankManyItemsAsyncTask extends AsyncTask<String, Void, List<RankingManyItemVO>> {

        private WeakReference<RankingManyItemFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpRankManyItemsAsyncTask(RankingManyItemFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected List<RankingManyItemVO> doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            List<RankingManyItemVO> manyItemRankResult = new ArrayList<>();
            String strUrl = params[0];
            int rank = Integer.parseInt(params[1]);
            try {
                JsonObject json = new JsonObject();
                json.addProperty("rank", rank);

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
                Type listType = new TypeToken<List<RankingManyItemVO>>() {}.getType();
                manyItemRankResult = gson.fromJson(response.body().string(), listType);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return manyItemRankResult;
        }

        @Override
        protected void onPostExecute(List<RankingManyItemVO> manyItemRankResult) {
            super.onPostExecute(manyItemRankResult);
            waitDialog.dismiss();
            if(manyItemRankResult != null) {
                Log.d("RankManyItemsAsyncTask", "");

                final RankingManyItemFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mAdapter = new RankingManyItemRecyclerAdapter(manyItemRankResult, fragment);
                //fragment.mAdapter.setOnClickListener(fragment);
                fragment.mRecyclerView.setAdapter(fragment.mAdapter);
            }
        }
    }

    //endregion

}
