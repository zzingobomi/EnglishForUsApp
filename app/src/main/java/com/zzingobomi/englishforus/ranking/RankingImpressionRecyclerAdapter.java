package com.zzingobomi.englishforus.ranking;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.Item;

import java.util.List;

public class RankingImpressionRecyclerAdapter extends RecyclerView.Adapter<RankingImpressionRecyclerAdapter.ViewHolder> {

    private final List<Item> mDataList;

    public RankingImpressionRecyclerAdapter(List<Item> mDataList) {
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

        holder.ranking_impression_kor.setText(item.getTitle_ko());
        holder.ranking_impression_eng.setText(item.getTitle_en());
        holder.ranking_impression_count.setText(String.valueOf(item.getImpressioncnt()));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ranking_impression_kor;
        TextView ranking_impression_eng;
        TextView ranking_impression_count;


        public ViewHolder(View itemView) {
            super(itemView);

            ranking_impression_kor = itemView.findViewById(R.id.ranking_impression_card_kor);
            ranking_impression_eng = itemView.findViewById(R.id.ranking_impression_card_eng);
            ranking_impression_count = itemView.findViewById(R.id.ranking_impression_card_impression);
        }
    }
}
