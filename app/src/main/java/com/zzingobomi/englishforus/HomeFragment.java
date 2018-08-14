package com.zzingobomi.englishforus;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private AdView mAdViewHomeBanner;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAdViewHomeBanner = view.findViewById(R.id.adview_home_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewHomeBanner.loadAd(adRequest);

        return view;
    }

}
