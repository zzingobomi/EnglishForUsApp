package com.zzingobomi.englishforus.ranking;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.Item;

import java.util.List;

public class RankingImpressionRecyclerAdapter extends RecyclerView.Adapter<RankingImpressionRecyclerAdapter.ViewHolder> {

    private RankingImpressionFragment mRankingImpressionFragment;
    private final List<Item> mDataList;

    public RankingImpressionRecyclerAdapter(List<Item> mDataList, RankingImpressionFragment fragment) {
        mRankingImpressionFragment = fragment;
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ranking_impression_card, parent, false);
        return new RankingImpressionRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = mDataList.get(position);

        if(position == 0) {
            holder.impression_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingImpressionFragment.getActivity())
                    .load(R.drawable.medal_1)
                    .into(holder.impression_medal_image);
            holder.impression_medal_text.setVisibility(View.GONE);
        } else if(position == 1) {
            holder.impression_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingImpressionFragment.getActivity())
                    .load(R.drawable.medal_2)
                    .into(holder.impression_medal_image);
            holder.impression_medal_text.setVisibility(View.GONE);
        } else if(position == 2) {
            holder.impression_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingImpressionFragment.getActivity())
                    .load(R.drawable.medal_3)
                    .into(holder.impression_medal_image);
            holder.impression_medal_text.setVisibility(View.GONE);
        } else {
            holder.impression_medal_image.setVisibility(View.GONE);
            holder.impression_medal_text.setVisibility(View.VISIBLE);
            holder.impression_medal_text.setText(String.valueOf(position + 1));
        }

        holder.ranking_impression_kor.setText(item.getTitle_ko());
        holder.ranking_impression_eng.setText(item.getTitle_en());
        holder.ranking_impression_displayname.setText(item.getRegdisplayname());
        holder.ranking_impression_count.setText(String.valueOf(item.getImpressioncnt()));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView impression_medal_image;
        TextView impression_medal_text;

        TextView ranking_impression_kor;
        TextView ranking_impression_eng;
        TextView ranking_impression_count;
        TextView ranking_impression_displayname;


        public ViewHolder(View itemView) {
            super(itemView);

            impression_medal_image = itemView.findViewById(R.id.ranking_impression_card_medal_image);
            impression_medal_text = itemView.findViewById(R.id.ranking_impression_card_medal_text);

            ranking_impression_kor = itemView.findViewById(R.id.ranking_impression_card_kor);
            ranking_impression_eng = itemView.findViewById(R.id.ranking_impression_card_eng);
            ranking_impression_count = itemView.findViewById(R.id.ranking_impression_card_impression);
            ranking_impression_displayname = itemView.findViewById(R.id.ranking_impression_card_displayname);
        }
    }
}
