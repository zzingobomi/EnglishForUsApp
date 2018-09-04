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
    RankingPagerAdapter mRankingPagerAdapter;


    public RankingShowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking_show, container, false);

        mRankingViewPager = view.findViewById(R.id.ranking_pager);
        mRankingPagerAdapter = new RankingPagerAdapter(getActivity().getSupportFragmentManager(), getContext());
        mRankingViewPager.setAdapter(mRankingPagerAdapter);

        mRankingTabLayout = view.findViewById(R.id.ranking_tab);
        mRankingTabLayout.setupWithViewPager(mRankingViewPager);

        return view;
    }
}
