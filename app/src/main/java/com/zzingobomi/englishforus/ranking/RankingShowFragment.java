package com.zzingobomi.englishforus.ranking;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

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
import com.zzingobomi.englishforus.myitemmanage.MyItemManageFragment;
import com.zzingobomi.englishforus.myitemmanage.MyItemsRecyclerAdapter;
import com.zzingobomi.englishforus.study.StudyFragment;
import com.zzingobomi.englishforus.vo.Item;

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
public class RankingShowFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    ViewPager mRankingViewPager;
    TabLayout mRankingTabLayout;


    ///
    /// Column 이름으로 순위 결과 받아오는 Wrapper 클래스
    ///
    static class WrapperColumnRankResult {
        public List<Item> resultItems;
        public String columnName;
    }

    ///
    /// MayItem 순위 결과 받아오는 Wrapper 클래스
    ///
    static class WrapperManyItemRankResult {
        public String regdisplayname;
        public String regphotourl;
        public Integer count;
    }


    public RankingShowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking_show, container, false);

        mRankingViewPager = view.findViewById(R.id.ranking_pager);
        RankingPagerAdapter adapter = new RankingPagerAdapter(getActivity().getSupportFragmentManager(), getContext());
        mRankingViewPager.setAdapter(adapter);

        mRankingTabLayout = view.findViewById(R.id.ranking_tab);
        mRankingTabLayout.setupWithViewPager(mRankingViewPager);



        //new HttpRankAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/rankingapi/rank", "impressioncnt", "3");
        //new HttpRankAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/rankingapi/rank", "likecnt", "3");
        //new HttpRankManyItemsAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/rankingapi/topregistuser", "3");

        return view;
    }

    //region 네트워크 영역

    ///
    /// column 이름으로 ranking 순위 받아오기
    ///
    private static class HttpRankAsyncTask extends AsyncTask<String, Void, WrapperColumnRankResult> {

        private WeakReference<RankingShowFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpRankAsyncTask(RankingShowFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected WrapperColumnRankResult doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            WrapperColumnRankResult wrapperRankResultList = new WrapperColumnRankResult();
            List<Item> items;
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

                wrapperRankResultList.resultItems = items;
                wrapperRankResultList.columnName = columnName;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return wrapperRankResultList;
        }

        @Override
        protected void onPostExecute(WrapperColumnRankResult columnRankResult) {
            super.onPostExecute(columnRankResult);
            waitDialog.dismiss();
            if(columnRankResult != null) {
                Log.d("HttpRankAsyncTask", "");

                final RankingShowFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                /*
                if(columnRankResult.columnName.equals("impressioncnt")) {
                    for(int i = 0; i < 3; i++) {
                        String resultText = columnRankResult.resultItems.get(i).getTitle_ko() + " / " +
                                columnRankResult.resultItems.get(i).getTitle_en() + " / " +
                                columnRankResult.resultItems.get(i).getImpressioncnt();
                        fragment.impressionRank[i].setText(resultText);
                    }
                } else if(columnRankResult.columnName.equals("likecnt")) {
                    for(int i = 0; i < 3; i++) {
                        String resultText = columnRankResult.resultItems.get(i).getTitle_ko() + " / " +
                                columnRankResult.resultItems.get(i).getTitle_en() + " / " +
                                columnRankResult.resultItems.get(i).getLikecnt();
                        fragment.likeRank[i].setText(resultText);
                    }
                }
                */
            }
        }
    }

    ///
    /// ManyItem 순위 받아오기(가장 많이 아이템을 등록한 유저)
    ///
    private static class HttpRankManyItemsAsyncTask extends AsyncTask<String, Void, List<WrapperManyItemRankResult>> {

        private WeakReference<RankingShowFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpRankManyItemsAsyncTask(RankingShowFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected List<WrapperManyItemRankResult> doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            List<WrapperManyItemRankResult> manyItemRankResult = new ArrayList<>();
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
                Type listType = new TypeToken<List<WrapperManyItemRankResult>>() {}.getType();
                manyItemRankResult = gson.fromJson(response.body().string(), listType);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return manyItemRankResult;
        }

        @Override
        protected void onPostExecute(List<WrapperManyItemRankResult> manyItemRankResult) {
            super.onPostExecute(manyItemRankResult);
            waitDialog.dismiss();
            if(manyItemRankResult != null) {
                Log.d("RankManyItemsAsyncTask", "");

                final RankingShowFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                for(WrapperManyItemRankResult itemRankResult : manyItemRankResult) {
                    Log.d("ITEM", itemRankResult.regdisplayname + " / " + itemRankResult.count);
                }
            }
        }
    }

    //endregion

}
