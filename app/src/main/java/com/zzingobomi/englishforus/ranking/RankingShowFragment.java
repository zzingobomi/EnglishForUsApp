package com.zzingobomi.englishforus.ranking;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzingobomi.englishforus.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankingShowFragment extends Fragment {


    public RankingShowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ranking_show, container, false);
    }

}