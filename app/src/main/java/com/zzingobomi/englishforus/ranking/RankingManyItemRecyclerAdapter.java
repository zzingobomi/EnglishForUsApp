package com.zzingobomi.englishforus.ranking;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.RankingManyItemVO;

import java.util.List;

public class RankingManyItemRecyclerAdapter extends RecyclerView.Adapter<RankingManyItemRecyclerAdapter.ViewHolder> {

    private final List<RankingManyItemVO> mDataList;

    public RankingManyItemRecyclerAdapter(List<RankingManyItemVO> mDataList) {
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

        holder.manyitem_displayname.setText(item.getRegdisplayname());
        holder.manyitem_count.setText(String.valueOf(item.getCount()));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView manyitem_displayname;
        TextView manyitem_count;


        public ViewHolder(View itemView) {
            super(itemView);

            manyitem_displayname = itemView.findViewById(R.id.ranking_manyitem_card_displayname);
            manyitem_count = itemView.findViewById(R.id.ranking_manyitem_card_count);
        }
    }
}
