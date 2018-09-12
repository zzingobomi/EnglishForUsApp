package com.zzingobomi.hellohappy.ranking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zzingobomi.hellohappy.R;
import com.zzingobomi.hellohappy.vo.RankingManyItemVO;

import java.util.List;

public class RankingManyItemRecyclerAdapter extends RecyclerView.Adapter<RankingManyItemRecyclerAdapter.ViewHolder> {

    private RankingManyItemFragment mRankingManyItemFragment;
    private final List<RankingManyItemVO> mDataList;

    public RankingManyItemRecyclerAdapter(List<RankingManyItemVO> mDataList, RankingManyItemFragment fragment) {
        mRankingManyItemFragment = fragment;
        this.mDataList = mDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ranking_many_item_card, parent, false);
        return new RankingManyItemRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankingManyItemVO item = mDataList.get(position);

        if(position == 0) {
            holder.manyitem_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingManyItemFragment.getActivity())
                    .load(R.drawable.medal_1)
                    .into(holder.manyitem_medal_image);
            holder.manyitem_medal_text.setVisibility(View.GONE);
        } else if(position == 1) {
            holder.manyitem_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingManyItemFragment.getActivity())
                    .load(R.drawable.medal_2)
                    .into(holder.manyitem_medal_image);
            holder.manyitem_medal_text.setVisibility(View.GONE);
        } else if(position == 2) {
            holder.manyitem_medal_image.setVisibility(View.VISIBLE);
            Glide.with(mRankingManyItemFragment.getActivity())
                    .load(R.drawable.medal_3)
                    .into(holder.manyitem_medal_image);
            holder.manyitem_medal_text.setVisibility(View.GONE);
        } else {
            holder.manyitem_medal_image.setVisibility(View.GONE);
            holder.manyitem_medal_text.setVisibility(View.VISIBLE);
            holder.manyitem_medal_text.setText(String.valueOf(position + 1));
        }

        RequestOptions options = new RequestOptions();
        options.circleCrop();
        if(item.getRegphotourl() != null) {
            Glide.with(mRankingManyItemFragment.getActivity())
                    .load(item.getRegphotourl())
                    .apply(options)
                    .into(holder.manyitem_user_photo);
        } else {
            Glide.with(mRankingManyItemFragment.getActivity())
                    .load(R.drawable.ic_anonymous_user)
                    .apply(options)
                    .into(holder.manyitem_user_photo);
        }
        holder.manyitem_displayname.setText(item.getRegdisplayname());
        holder.manyitem_count.setText(String.valueOf(item.getCount()) + " 개");
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView manyitem_medal_image;
        TextView manyitem_medal_text;

        ImageView manyitem_user_photo;
        TextView manyitem_displayname;
        TextView manyitem_count;

        public ViewHolder(View itemView) {
            super(itemView);

            manyitem_medal_image = itemView.findViewById(R.id.ranking_manyitem_card_medal_image);
            manyitem_medal_text = itemView.findViewById(R.id.ranking_manyitem_card_medal_text);

            manyitem_user_photo = itemView.findViewById(R.id.ranking_manyitem_card_photo);
            manyitem_displayname = itemView.findViewById(R.id.ranking_manyitem_card_displayname);
            manyitem_count = itemView.findViewById(R.id.ranking_manyitem_card_count);
        }
    }
}
