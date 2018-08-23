package com.zzingobomi.englishforus.myitemmanage;

import android.animation.LayoutTransition;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzingobomi.englishforus.R;
import com.zzingobomi.englishforus.vo.Item;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyItemsRecyclerAdapter extends RecyclerView.Adapter<MyItemsRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private final List<Item> mDataList;
    private MyItemsRecyclerViewClickListener mListener;

    public MyItemsRecyclerAdapter(List<Item> dataList, Context context) {
        mContext = context;
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

        holder.myitem_title_ko.setText(item.getTitle_ko());
        holder.myitem_title_ko_edit.setText(item.getTitle_ko());
        holder.myitem_title_en.setText(item.getTitle_en());
        holder.myitem_title_en_edit.setText(item.getTitle_en());
        holder.myitem_addinfo.setText(item.getAddinfo());
        holder.myitem_addinfo_edit.setText(item.getAddinfo());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.myitem_regdate.setText(simpleDateFormat.format(item.getRegdate()));

        holder.item_impression.setText(String.valueOf(item.getImpressioncnt()));
        holder.item_like.setText(String.valueOf(item.getLikecnt()));
        holder.item_bad.setText(String.valueOf(item.getBadcnt()));

        holder.list_impression.setText(String.valueOf(item.getImpressioncnt()));
        holder.list_like.setText(String.valueOf(item.getLikecnt()));
        holder.list_bad.setText(String.valueOf(item.getBadcnt()));

        if (mListener != null) {
            // 현재 위치
            final int pos = holder.getAdapterPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.bReadViewState) {
                        // 리스트 형태로 만든다
                        holder.myitem_title_ko_literal.setVisibility(View.GONE);
                        holder.myitem_title_en_literal.setVisibility(View.GONE);
                        holder.myitem_addinfo_layout.setVisibility(View.GONE);
                        holder.myitem_social_info_layout.setVisibility(View.GONE);
                        holder.myitem_button_layout.setVisibility(View.GONE);


                        Animation visibleAni = new AlphaAnimation(0, 1);
                        visibleAni.setDuration(500);
                        holder.myitem_list_bottom_layout.setAnimation(visibleAni);
                        holder.myitem_list_bottom_layout.setVisibility(View.VISIBLE);

                        holder.bReadViewState = false;
                    } else {
                        // Read 형태로 만든다
                        Animation visibleAni = new AlphaAnimation(0, 1);
                        visibleAni.setDuration(500);
                        holder.myitem_title_ko_literal.setAnimation(visibleAni);
                        holder.myitem_title_en_literal.setAnimation(visibleAni);
                        holder.myitem_addinfo_layout.setAnimation(visibleAni);
                        holder.myitem_social_info_layout.setAnimation(visibleAni);
                        holder.myitem_button_layout.setAnimation(visibleAni);

                        holder.myitem_title_ko_literal.setVisibility(View.VISIBLE);
                        holder.myitem_title_en_literal.setVisibility(View.VISIBLE);
                        holder.myitem_addinfo_layout.setVisibility(View.VISIBLE);
                        holder.myitem_social_info_layout.setVisibility(View.VISIBLE);
                        holder.myitem_button_layout.setVisibility(View.VISIBLE);

                        holder.myitem_list_bottom_layout.setVisibility(View.GONE);

                        holder.bReadViewState = true;
                    }

                    mListener.onItemClicked(pos);
                }
            });
            holder.modify_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.modify_btn.getText().toString().equals(mContext.getResources().getString(R.string.common_modify_save_text))) {
                        mListener.onModifyButtonClicked(pos
                                ,holder.myitem_title_ko_edit.getText().toString()
                                ,holder.myitem_title_en_edit.getText().toString()
                                ,holder.myitem_addinfo_edit.getText().toString()
                        );
                    } else {
                        holder.myitem_title_ko.setVisibility(View.GONE);
                        holder.myitem_title_ko_edit.setVisibility(View.VISIBLE);
                        holder.myitem_title_en.setVisibility(View.GONE);
                        holder.myitem_title_en_edit.setVisibility(View.VISIBLE);
                        holder.myitem_addinfo.setVisibility(View.GONE);
                        holder.myitem_addinfo_edit.setVisibility(View.VISIBLE);

                        holder.modify_btn.setText(mContext.getResources().getString(R.string.common_modify_save_text));
                        holder.myitemmodify_cancel_btn_layout.setVisibility(View.VISIBLE);
                    }
                }
            });
            holder.modifycancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.myitem_title_ko.setVisibility(View.VISIBLE);
                    holder.myitem_title_ko_edit.setVisibility(View.GONE);
                    holder.myitem_title_en.setVisibility(View.VISIBLE);
                    holder.myitem_title_en_edit.setVisibility(View.GONE);
                    holder.myitem_addinfo.setVisibility(View.VISIBLE);
                    holder.myitem_addinfo_edit.setVisibility(View.GONE);

                    holder.modify_btn.setText(mContext.getResources().getString(R.string.common_modify_text));
                    holder.myitemmodify_cancel_btn_layout.setVisibility(View.GONE);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView myitem_title_ko_literal;
        TextView myitem_title_ko;
        EditText myitem_title_ko_edit;
        TextView myitem_title_en_literal;
        TextView myitem_title_en;
        EditText myitem_title_en_edit;

        LinearLayout myitem_addinfo_layout;
        TextView myitem_addinfo;
        EditText myitem_addinfo_edit;
        TextView myitem_regdate;

        LinearLayout myitem_social_info_layout;
        TextView item_impression;
        TextView item_like;
        TextView item_bad;

        LinearLayout myitem_button_layout;
        Button modify_btn;
        Button delete_btn;
        LinearLayout myitemmodify_cancel_btn_layout;
        Button modifycancel_btn;

        LinearLayout myitem_list_bottom_layout;
        TextView list_impression;
        TextView list_like;
        TextView list_bad;

        Boolean bReadViewState;

        public ViewHolder(View itemView) {
            super(itemView);

            myitem_title_ko_literal = itemView.findViewById(R.id.myitem_title_ko_literal);
            myitem_title_ko = itemView.findViewById(R.id.myitem_title_ko);
            myitem_title_ko_edit = itemView.findViewById(R.id.myitem_title_ko_edit);
            myitem_title_en_literal = itemView.findViewById(R.id.myitem_title_en_literal);
            myitem_title_en = itemView.findViewById(R.id.myitem_title_en);
            myitem_title_en_edit = itemView.findViewById(R.id.myitem_title_en_edit);

            myitem_addinfo_layout = itemView.findViewById(R.id.myitem_addinfo_layout);
            myitem_addinfo = itemView.findViewById(R.id.myitem_addinfo);
            myitem_addinfo_edit = itemView.findViewById(R.id.myitem_addinfo_edit);
            myitem_regdate = itemView.findViewById(R.id.myitem_regdate);

            myitem_social_info_layout = itemView.findViewById(R.id.myitem_social_info_layout);
            item_impression = itemView.findViewById(R.id.myitem_card_read_impression);
            item_like = itemView.findViewById(R.id.myitem_card_read_like);
            item_bad = itemView.findViewById(R.id.myitem_card_read_bad);

            myitem_button_layout = itemView.findViewById(R.id.myitem_button_layout);
            modify_btn = itemView.findViewById(R.id.myitemmodify_btn);
            delete_btn = itemView.findViewById(R.id.myitemdelete_btn);
            myitemmodify_cancel_btn_layout = itemView.findViewById(R.id.myitemmodify_cancel_btn_layout);
            modifycancel_btn = itemView.findViewById(R.id.myitemmodify_cancel_btn);

            myitem_list_bottom_layout = itemView.findViewById(R.id.myitem_list_bottom_layout);
            list_impression = itemView.findViewById(R.id.myitem_card_list_impression);
            list_like = itemView.findViewById(R.id.myitem_card_list_like);
            list_bad = itemView.findViewById(R.id.myitem_card_list_bad);

            bReadViewState = false;
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public long getItemId(int position) {
        return mDataList.get(position).getIdx();
    }

    public void setOnClickListener(MyItemsRecyclerViewClickListener listener) {
        mListener = listener;
    }

    public Item getItem(int position) {
        return mDataList.get(position);
    }

    public interface MyItemsRecyclerViewClickListener {
        // 아이템 전체 부분의 클릭
        void onItemClicked(int position);

        // Modify 버튼 클릭
        void onModifyButtonClicked(int position, String titleKor, String titleEng, String addInfo);

        // Delete 버튼 클릭
        void onDeleteButtonClicked(int position);
    }

    public void addItem(int position, Item item) {
        mDataList.add(position, item);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mDataList.size());
    }

    public void modifyItem(int position, Item item) {
        mDataList.set(position, item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataList.size());
    }
}
