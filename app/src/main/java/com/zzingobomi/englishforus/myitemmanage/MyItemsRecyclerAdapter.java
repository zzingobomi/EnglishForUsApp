package com.zzingobomi.englishforus.myitemmanage;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = mDataList.get(position);

        holder.title_ko.setText(item.getTitle_ko());
        holder.title_en.setText(item.getTitle_en());
        holder.addinfo.setText(item.getAddinfo());

        if (mListener != null) {
            // 현재 위치
            final int pos = holder.getAdapterPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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

        TextView title_ko;
        TextView title_en;
        TextView addinfo;

        Button modify_btn;
        Button delete_btn;

        public ViewHolder(View itemView) {
            super(itemView);
            title_ko = itemView.findViewById(R.id.myitem_title_ko);
            title_en = itemView.findViewById(R.id.myitem_title_en);
            addinfo = itemView.findViewById(R.id.myitem_addinfo);

            modify_btn = itemView.findViewById(R.id.myitemmodify_btn);
            delete_btn = itemView.findViewById(R.id.myitemdelete_btn);
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
