package com.hf.ui.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.hf.ui.DPPX;
import com.hf.ui.R;

/**
 * @author tx
 * @date 2023/5/30 13:53
 * @target this class will do...
 */
public class EasyEditText extends AppCompatEditText {

    public EasyEditText(@NonNull Context context) {
        super(context);
        init(null);
    }

    public EasyEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EasyEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        String nameSpace = "http://schemas.android.com/apk/res/android";
        if(attrs!=null&&attrs.getAttributeBooleanValue(nameSpace,"singleLine",false)==false){
            setBackgroundResource(R.drawable.base_line_input_selector);
            setPadding(DPPX.dip2px(getContext(),10),DPPX.dip2px(getContext(),3),DPPX.dip2px(getContext(),10),DPPX.dip2px(getContext(),3));
            setGravity(Gravity.LEFT | Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setTextCursorDrawable(R.drawable.input_cursor);
            }
        }else {
            boolean setPaddingLeft=false,setPaddingRight=false,setPaddingTop=false,setPaddingBottom = false;
            if(attrs!=null&&attrs.getAttributeValue(nameSpace,"paddingLeft")!=null){
                setPaddingLeft = true;
            }
            if(attrs!=null&&attrs.getAttributeValue(nameSpace,"paddingRight")!=null){
                setPaddingRight = true;
            }
            if(attrs!=null&&attrs.getAttributeValue(nameSpace,"paddingTop")!=null){
                setPaddingTop = true;
            }
            if(attrs!=null&&attrs.getAttributeValue(nameSpace,"paddingBottom")!=null){
                setPaddingBottom = true;
            }

            setBackgroundResource(R.drawable.base_line_input_selector);
            setGravity(Gravity.LEFT | Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setTextCursorDrawable(R.drawable.input_cursor);
            }
            setPadding(setPaddingLeft?getPaddingLeft():DPPX.dip2px(getContext(),10),
                    setPaddingTop?getPaddingTop():DPPX.dip2px(getContext(),3),
                    setPaddingRight?getPaddingRight():DPPX.dip2px(getContext(),10),
                    setPaddingBottom?getPaddingBottom():DPPX.dip2px(getContext(),3));
        }
    }

}
