package com.zzingobomi.englishforus.ranking;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zzingobomi.englishforus.R;

import java.util.ArrayList;

public class RankingPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private ArrayList<Fragment> mData;

    public RankingPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;

        mData = new ArrayList<>();
        mData.add(new RankingManyItemFragment());
        mData.add(new RankingLikeFragment());
        mData.add(new RankingImpressionFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return mContext.getResources().getString(R.string.rank_manyitem_title);
        } else if(position == 1) {
            return mContext.getResources().getString(R.string.rank_like_title);
        } else if(position == 2) {
            return mContext.getResources().getString(R.string.rank_impression_title);
        }

        return position + " 번째";
    }
}
