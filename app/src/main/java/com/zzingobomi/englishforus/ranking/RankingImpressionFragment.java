package com.zzingobomi.englishforus.ranking;


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
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.Item;

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
public class RankingImpressionFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private RecyclerView mRecyclerView;
    private RankingImpressionRecyclerAdapter mAdapter;


    public RankingImpressionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking_impression, container, false);

        mRecyclerView = view.findViewById(R.id.ranking_impression_recycler_view);
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

        new HttpRankAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/rankingapi/rank", "impressioncnt", "5");

        return view;
    }



    //region 네트워크 영역

    ///
    /// column 이름으로 ranking 순위 받아오기
    ///
    private static class HttpRankAsyncTask extends AsyncTask<String, Void, List<Item>> {

        private WeakReference<RankingImpressionFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpRankAsyncTask(RankingImpressionFragment fragment) {
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
            String columnName = params[1];
            int rank = Integer.parseInt(params[2]);
            try {
                JsonObject json = new JsonObject();
                json.addProperty("column", columnName);
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
                Type listType = new TypeToken<List<Item>>() {}.getType();
                items = gson.fromJson(response.body().string(), listType);

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
                Log.d("HttpRankAsyncTask", items.toString());

                final RankingImpressionFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mAdapter = new RankingImpressionRecyclerAdapter(items, fragment);
                //fragment.mAdapter.setOnClickListener(fragment);
                fragment.mRecyclerView.setAdapter(fragment.mAdapter);
            }
        }
    }

    //endregion

}
