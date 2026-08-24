package com.hf.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.hf.ui.DPPX;
import com.hf.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tx
 * @date 2023/6/15 17:29
 * @target 下拉菜单
 */
public class EasySpinner<T extends EasySpinner.SpinnerObject> extends AppCompatTextView {
    RecyclerPop recyclerPop;

    ArrayList<T> spinnerObjectArrayList = new ArrayList<>();

    Adapter<T> adapter;

    T select= null;

    public T getSelect(){
        return select;
    }

    public interface SpinnerObject{
       String getObjectName();
       String getObjectId();
    }

    public interface Adapter<T extends SpinnerObject>{
        void select(T spinnerObject);
        List<T> getData();
        void onLoad();
    }

    public EasySpinner(@NonNull Context context) {
        super(context);
        init(null);
    }

    public EasySpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EasySpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void refresh(){
        recyclerPop.refresh();
    }

    private void init(AttributeSet attrs){
        setBackgroundResource(R.drawable.base_line_input_selector);
        setSingleLine();
        setGravity(Gravity.LEFT|Gravity.CENTER);
        setPadding(DPPX.dip2px(getContext(),10),0,DPPX.dip2px(getContext(),10),0);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
        setTextColor(getResources().getColor(R.color.BASE_BLACK));
        setHintTextColor(getResources().getColor(R.color.BASE_GREY));
    }

    public void setAdataper(Adapter<T> adapter){
        this.adapter = adapter;
        spinnerObjectArrayList.clear();
        List<T> list = adapter.getData();
        spinnerObjectArrayList.addAll(list);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerPop.showPop(EasySpinner.this);
                recyclerPop.refresh();
            }
        });
        recyclerPop =new RecyclerPop(getContext(), new RecyclerPop.IDealList() {
            @Override
            public int getItemCount() {
                return spinnerObjectArrayList.size();
            }

            @Override
            public void setHolder(RecyclerPop.SelectHolder holder, int p) {
                holder.textView.setText(spinnerObjectArrayList.get(p).getObjectName());
                holder.textView.setTextColor(getResources().getColor(R.color.BASE_BLACK));
            }

            @Override
            public void onClick(int p) {
                setText(spinnerObjectArrayList.get(p).getObjectName());
                adapter.select(spinnerObjectArrayList.get(p));
                select = spinnerObjectArrayList.get(p);
            }

            @Override
            public void onLoad() {
                adapter.onLoad();
            }
        });
        recyclerPop.initPopWindow();


        setText(spinnerObjectArrayList.get(0).getObjectName());
        adapter.select(spinnerObjectArrayList.get(0));
        select = spinnerObjectArrayList.get(0);
    }
    public void clear(){
        setText("");
        select = null;
        adapter.select(null);
    }
}
