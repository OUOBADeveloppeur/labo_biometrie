package com.hf.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.hf.ui.R;

/**
 * @author tx
 * @date 2023/6/30 11:06
 * @target this class will do...
 */
public class EasyLoading {
    View loadingView;
    ImageView img;
    Dialog dialog;
    Animation rotateAnimation;

    boolean callShowLoading;

    public interface ILoadSrc {
        int srcLayout();

        int srcImgId();
    }

    public EasyLoading(Context context, ILoadSrc iLoadSrc) {
        callShowLoading = false;
        loadingView = LayoutInflater.from(context).inflate(iLoadSrc.srcLayout(), null);
        img = loadingView.findViewById(iLoadSrc.srcImgId());
        dialog = new Dialog(context, R.style.loadDialog);
        dialog.setContentView(loadingView);
        dialog.setCancelable(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                callShowLoading = false;
            }
        });
        rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        Log.e("dialog","create");
    }

    public void show() {
        Log.e("dialog","callshow");
        if (!callShowLoading) {
            dialog.show();
            Log.e("dialog","callshow success");
            callShowLoading = true;
            img.startAnimation(rotateAnimation);
        }
    }

    public void show(boolean cancelable) {
        if (!callShowLoading) {
            dialog.setCancelable(cancelable);
            dialog.show();
            callShowLoading = true;
            img.startAnimation(rotateAnimation);
        }
    }

    public void dismiss() {
        Log.e("dialog","call dismiss");
        callShowLoading = false;
        dialog.dismiss();
        img.clearAnimation();
    }
}
