package com.zzingobomi.hellohappy.ranking;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zzingobomi.hellohappy.R;
import com.zzingobomi.hellohappy.vo.Item;
import com.zzingobomi.hellohappy.vo.RankingManyItemVO;

import java.util.List;

public class RankingLikeRecyclerAdapter extends RecyclerView.Adapter<RankingLikeRecyclerAdapter.ViewHolder> {

    private RankingLikeFragment mRankingLikeFragment;
    private final List<Item> mDataList;

    public RankingLikeRecyclerAdapter(List<Item> mDataList, RankingLikeFragment fragment) {
        mRankingLikeFragment = fragment;
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ranking_like_card, parent, false);
        return new RankingLikeRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = mDataList.get(position);

        if(position == 0) {
            holder.like_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingLikeFragment.getActivity())
                    .load(R.drawable.medal_1)
                    .into(holder.like_medal_image);
            holder.like_medal_text.setVisibility(View.GONE);
        } else if(position == 1) {
            holder.like_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingLikeFragment.getActivity())
                    .load(R.drawable.medal_2)
                    .into(holder.like_medal_image);
            holder.like_medal_text.setVisibility(View.GONE);
        } else if(position == 2) {
            holder.like_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingLikeFragment.getActivity())
                    .load(R.drawable.medal_3)
                    .into(holder.like_medal_image);
            holder.like_medal_text.setVisibility(View.GONE);
        } else {
            holder.like_medal_image.setVisibility(View.GONE);
            holder.like_medal_text.setVisibility(View.VISIBLE);
            holder.like_medal_text.setText(String.valueOf(position + 1));
        }

        holder.ranking_like_kor.setText(item.getTitle_ko());
        holder.ranking_like_eng.setText(item.getTitle_en());
        holder.ranking_like_displayname.setText(item.getRegdisplayname());
        holder.ranking_like_count.setText(String.valueOf(item.getLikecnt()));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView like_medal_image;
        TextView like_medal_text;

        TextView ranking_like_kor;
        TextView ranking_like_eng;
        TextView ranking_like_count;
        TextView ranking_like_displayname;


        public ViewHolder(View itemView) {
            super(itemView);

            like_medal_image = itemView.findViewById(R.id.ranking_like_card_medal_image);
            like_medal_text = itemView.findViewById(R.id.ranking_like_card_medal_text);

            ranking_like_kor = itemView.findViewById(R.id.ranking_like_card_kor);
            ranking_like_eng = itemView.findViewById(R.id.ranking_like_card_eng);
            ranking_like_count = itemView.findViewById(R.id.ranking_like_card_like);
            ranking_like_displayname = itemView.findViewById(R.id.ranking_like_card_displayname);
        }
    }
}
