package com.hf.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.ui.R;

public class RecyclerPop extends BasePopupWindow {

    EasyRecyclerView recyclerView;

    public interface IDealList{
        int getItemCount();
        void setHolder(SelectHolder holder, int p);
        void onClick(int p);
        void onLoad();
    }


    public static class SelectHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public SelectHolder( View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    IDealList iDealList;

    public RecyclerPop(Context context, IDealList iDealList) {
        super(context);
        this.iDealList = iDealList;
    }

    public void select(int p){
        iDealList.onClick(p);
    }

    @Override
    public int getPopupWindowResourceId() {
        return R.layout.pop_recycler_item_select;
    }

    @Override
    public void popupWindowSet(final View view) {
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new EasyRecyclerView.EasyAdapter<SelectHolder>() {
            @Override
            public SelectHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
                View v = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.item_pop_recyler_item,viewGroup,false);
                return new SelectHolder(v);
            }

            @Override
            public void onLoad() {
                iDealList.onLoad();
            }

            @Override
            public void onLoadMore(int page, EasyRecyclerView.ILoadMoreResult iLoadMoreResult) {

            }

            @Override
            public void onBind(SelectHolder holder, int position) {
                iDealList.setHolder(holder,position);
                holder.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        iDealList.onClick(position);
                        dismiss();
                    }
                });
            }

            @Override
            public boolean needLoadMore() {
                return false;
            }

            @Override
            public int getItemCount() {
                return iDealList.getItemCount();
            }
        });
    }

    public void refresh(){
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
