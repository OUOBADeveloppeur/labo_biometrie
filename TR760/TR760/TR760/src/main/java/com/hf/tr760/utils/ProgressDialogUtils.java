package com.hf.tr760.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ProgressDialogUtils {
	private static ProgressDialog mProgressDialog;

	public static void showProgressDialog(Context context, CharSequence message, DialogInterface.OnDismissListener dismissListener){

		if(mProgressDialog == null){
			mProgressDialog = ProgressDialog.show(context, "", message);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setOnDismissListener(dismissListener);
		}
			mProgressDialog.show();
	}

	public static void showProgressDialogForce(Context context, CharSequence message){

		if(mProgressDialog == null){
			mProgressDialog = ProgressDialog.show(context, "", message);
			mProgressDialog.setCancelable(false);
		}else{
			mProgressDialog.show();
		}
	}
	
	/**
	 * �ر�ProgressDialog
	 */
	public static void dismissProgressDialog(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
}
