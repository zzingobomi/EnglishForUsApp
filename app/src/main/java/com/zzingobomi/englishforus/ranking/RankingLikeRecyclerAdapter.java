package com.zzingobomi.englishforus.ranking;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.Item;
import com.zzingobomi.englishforus.vo.RankingManyItemVO;

import java.util.List;

public class RankingLikeRecyclerAdapter extends RecyclerView.Adapter<RankingLikeRecyclerAdapter.ViewHolder> {

    private final List<Item> mDataList;

    public RankingLikeRecyclerAdapter(List<Item> mDataList) {
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

        holder.ranking_like_kor.setText(item.getTitle_ko());
        holder.ranking_like_eng.setText(item.getTitle_en());
        holder.ranking_like_count.setText(String.valueOf(item.getLikecnt()));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ranking_like_kor;
        TextView ranking_like_eng;
        TextView ranking_like_count;


        public ViewHolder(View itemView) {
            super(itemView);

            ranking_like_kor = itemView.findViewById(R.id.ranking_like_card_kor);
            ranking_like_eng = itemView.findViewById(R.id.ranking_like_card_eng);
            ranking_like_count = itemView.findViewById(R.id.ranking_like_card_like);
        }
    }
}
