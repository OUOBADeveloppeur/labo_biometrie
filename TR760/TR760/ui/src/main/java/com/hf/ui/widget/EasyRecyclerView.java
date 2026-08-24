package com.hf.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author tx
 * @date 2023/6/1 8:31
 * @target this class will do...
 */
public class EasyRecyclerView extends RecyclerView {

    int page =1;

    boolean isLoadingMore =false;

    public void reload(List list){
        list.clear();
        page = 1;
        ((EasyAdapter)getAdapter()).hasBeenBind = 0;
        ((EasyAdapter)getAdapter()).onLoad();
    }

    public EasyRecyclerView(@NonNull Context context) {
        super(context);
        init();

    }

    public EasyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EasyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (getAdapter() instanceof EasyAdapter && ((EasyAdapter) getAdapter()).bindAll()) {
                        if(!isLoadingMore) {
                            page = page+1;
                            isLoadingMore = true;
                            ((EasyAdapter) getAdapter()).onLoadMore(page, new ILoadMoreResult() {
                                @Override
                                public void result(boolean r) {
                                    isLoadingMore = false;
                                    if (r) {
                                        getAdapter().notifyDataSetChanged();
                                    } else {
                                        ((EasyAdapter) getAdapter()).noNeedLoad();
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public interface ILoadMoreResult {
        void result(boolean r);
    }

    public void setAdapter(EasyAdapter adapter) {
        super.setAdapter(adapter);
        adapter.onLoad();
    }

    public abstract static class EasyAdapter<T extends ViewHolder> extends RecyclerView.Adapter<T> {

        public abstract void onLoad();

        public abstract void onLoadMore(int page,ILoadMoreResult iLoadMoreResult);

        public abstract void onBind(T holder, int position);

        public abstract boolean needLoadMore();

        public int hasBeenBind = 0;

        public EasyAdapter() {
            hasBeenBind = needLoadMore() ? 0 : Integer.MAX_VALUE;
        }

        public boolean bindAll() {
            return getItemCount() == (hasBeenBind + 1);
        }

        public void noNeedLoad() {
            hasBeenBind = Integer.MAX_VALUE;
        }

        @Override
        public void onBindViewHolder(@NonNull T holder, int position) {
            onBind(holder, position);
            if (position > hasBeenBind) {
                hasBeenBind = position;
            }
        }
    }
}
