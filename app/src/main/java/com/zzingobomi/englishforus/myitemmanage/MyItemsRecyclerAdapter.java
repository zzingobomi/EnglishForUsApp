package com.zzingobomi.englishforus.myitemmanage;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.Item;

import java.util.List;

public class MyItemsRecyclerAdapter extends RecyclerView.Adapter<MyItemsRecyclerAdapter.ViewHolder> {

    private final List<Item> mDataList;
    private MyItemsRecyclerViewClickListener mListener;

    public MyItemsRecyclerAdapter(List<Item> dataList) {
        mDataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.myitem_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Item item = mDataList.get(position);

        holder.list_title_ko.setText(item.getTitle_ko());
        holder.list_title_en.setText(item.getTitle_en());

        holder.title_ko.setText(item.getTitle_ko());
        holder.title_en.setText(item.getTitle_en());
        holder.addinfo.setText(item.getAddinfo());

        holder.item_impression.setText(String.valueOf(item.getImpressioncnt()));
        holder.item_ike.setText(String.valueOf(item.getLikecnt()));
        holder.item_bad.setText(String.valueOf(item.getBadcnt()));

        if (mListener != null) {
            // 현재 위치
            final int pos = holder.getAdapterPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.bReadViewState) {
                        // 애니메이션 적용하기..
                        holder.list_layout.setVisibility(View.VISIBLE);
                        holder.read_layout.setVisibility(View.GONE);
                        holder.bReadViewState = false;
                    } else {
                        // 애니메이션 적용하기..
                        holder.list_layout.setVisibility(View.GONE);
                        holder.read_layout.setVisibility(View.VISIBLE);
                        holder.bReadViewState = true;
                    }

                    mListener.onItemClicked(pos);
                }
            });
            holder.modify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onModifyButtonClicked(pos);
                }
            });
            holder.delete_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onDeleteButtonClicked(pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // ListView 에서 보여질 때
        TextView list_title_ko;
        TextView list_title_en;

        // 하나의 아이템 Read 할 때
        TextView title_ko;
        TextView title_en;
        TextView addinfo;

        TextView item_impression;
        TextView item_ike;
        TextView item_bad;

        Button modify_btn;
        Button delete_btn;

        // 레이아웃
        LinearLayout list_layout;
        LinearLayout read_layout;
        Boolean bReadViewState;

        public ViewHolder(View itemView) {
            super(itemView);
            list_title_ko = itemView.findViewById(R.id.myitem_card_list_title_ko);
            list_title_en = itemView.findViewById(R.id.myitem_card_list_title_en);

            title_ko = itemView.findViewById(R.id.myitem_title_ko);
            title_en = itemView.findViewById(R.id.myitem_title_en);
            addinfo = itemView.findViewById(R.id.myitem_addinfo);

            item_impression = itemView.findViewById(R.id.myitem_card_read_impression);
            item_ike = itemView.findViewById(R.id.myitem_card_read_like);
            item_bad = itemView.findViewById(R.id.myitem_card_read_bad);

            modify_btn = itemView.findViewById(R.id.myitemmodify_btn);
            delete_btn = itemView.findViewById(R.id.myitemdelete_btn);

            list_layout = itemView.findViewById(R.id.myitem_card_list);
            read_layout = itemView.findViewById(R.id.myitem_card_read);
            bReadViewState = false;
        }
    }

    @Override
    public long getItemId(int position) {
        return mDataList.get(position).getIdx();
    }

    public void setOnClickListener(MyItemsRecyclerViewClickListener listener) {
        mListener = listener;
    }

    public interface MyItemsRecyclerViewClickListener {
        // 아이템 전체 부분의 클릭
        void onItemClicked(int position);

        // Modify 버튼 클릭
        void onModifyButtonClicked(int position);

        // Delete 버튼 클릭
        void onDeleteButtonClicked(int position);
    }

    public void removeItem(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataList.size());
    }

    public void addItem(int position, Item item) {
        mDataList.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mDataList.size());
    }
}
