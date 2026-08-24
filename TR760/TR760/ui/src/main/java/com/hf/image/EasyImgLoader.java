package com.hf.image;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.hf.ui.R;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.engine.CompressFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import id.zelory.compressor.Compressor;
import top.zibin.luban.Luban;
import top.zibin.luban.OnNewCompressListener;

/**
 * @author tx
 * @date 2023/5/29 15:45
 * @target 对Picasso的封装，基于ImageView的大小展示图片，如果ImageView是new出来的没有展示在View中就获取不到宽高，那么执行延迟加载
 */
public class EasyImgLoader {
    private Picasso picasso;

    public EasyImgLoader(){
    }

    //简单加载图片地址
    public void simple(String url, ImageView imageView){
        int w = imageView.getWidth();
        int h = imageView.getHeight();
        //表明imageView还没有载入视图，那么等待一秒
        if(w==0||h==0){
            imageView.postDelayed(new Runnable() {
                //重试
                int retry = 0;
                @Override
                public void run() {
                    //重新获取尺寸
                    if(imageView.getWidth()==0||imageView.getHeight()==0){
                        retry++;
                        //重试不超过3次结束
                        if(retry<3) {
                            run();
                        }else {
                            Log.e("EasyImgLoader","imageView Width or Height equals 0");
                        }
                        return;
                    }
                    picasso = Picasso.get();
                    picasso.load(url)
                            .error(R.drawable.error)
                            .placeholder(R.drawable.placeholder)
                            .resize(imageView.getWidth(),imageView.getHeight())
                            .centerCrop()
                            .into(imageView);
                }
            },1000);
        }else {
            picasso = Picasso.get();
            picasso.load(url)
                    .error(R.drawable.error)
                    .placeholder(R.drawable.placeholder)
                    .resize(w,h)
                    .centerCrop()
                    .into(imageView);
        }
    }

    //简单加载图片文件
    public void simple(File file, ImageView imageView){
        int w = imageView.getWidth();
        int h = imageView.getHeight();
        //表明imageView还没有载入视图，那么等待一秒
        if(w==0||h==0){
            imageView.postDelayed(new Runnable() {
                //重试
                int retry = 0;
                @Override
                public void run() {
                    //重新获取尺寸
                    if(imageView.getWidth()==0||imageView.getHeight()==0){
                        retry++;
                        //重试不超过3次结束
                        if(retry<3) {
                            run();
                        }else {
                            Log.e("EasyImgLoader","imageView Width or Height equals 0");
                        }
                        return;
                    }
                    picasso = Picasso.get();
                    picasso.load(file)
                            .error(R.drawable.error)
                            .placeholder(R.drawable.placeholder)
                            .resize(imageView.getWidth(),imageView.getHeight())
                            .centerCrop()
                            .into(imageView);
                }
            },1000);
        }else {
            picasso = Picasso.get();
            picasso.load(file)
                    .error(R.drawable.error)
                    .placeholder(R.drawable.placeholder)
                    .resize(w,h)
                    .centerCrop()
                    .into(imageView);
        }
    }

    //压缩图片
    public void compressImage(Context context,File imgFile, ICompressResult iCompressResult){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File compressedImageFile = null;
                try {
                    compressedImageFile = new Compressor(context)
                            .compressToFile(imgFile);
                    iCompressResult.reslt(compressedImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    iCompressResult.reslt(null);
                }
            }
        }).start();
    }

    //按VIEW大小压缩图片
    public void compressImage(Context context,File imgFile,ImageView imgView, ICompressResult iCompressResult){
        int w = imgView.getWidth();
        int h = imgView.getHeight();
        int size = w>h?w:h;
        //表明imageView还没有载入视图，那么等待一秒
        if(w==0||h==0){
            imgView.postDelayed(new Runnable() {
                //重试
                int retry = 0;
                @Override
                public void run() {
                    //重新获取尺寸
                    if(imgView.getWidth()==0||imgView.getHeight()==0){
                        retry++;
                        //重试不超过3次结束
                        if(retry<3) {
                            run();
                        }else {
                            Log.e("EasyImgLoader","imageView Width or Height equals 0");
                        }
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File compressedImageFile = null;
                            try {
                                compressedImageFile = new Compressor(context)
                                        .setMaxHeight(size)
                                        .setMaxWidth(size)
                                        .compressToFile(imgFile);
                                iCompressResult.reslt(compressedImageFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                                iCompressResult.reslt(null);
                            }
                        }
                    }).start();
                }
            },1000);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                File compressedImageFile = null;
                try {
                    compressedImageFile = new Compressor(context)
                            .setMaxHeight(size)
                            .setMaxWidth(size)
                            .compressToFile(imgFile);
                    iCompressResult.reslt(compressedImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    iCompressResult.reslt(null);
                }
            }
        }).start();
    }

    //选择照片
    public void selectImage(Activity activity,int max,ISelectImgResult iSelectImgResult){
        int typep = SelectModeConfig.MULTIPLE;
        if(max==1){
            typep = SelectModeConfig.SINGLE;
        }
        PictureSelector.create(activity)
                .openGallery(SelectMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .setImageEngine(PicassoEngine.createPicassoEngine())// 外部传入图片加载引擎，必传项
                .setImageSpanCount(10)// 每行显示个数
                .setSelectionMode(typep)// 多选 or 单选
                .isDisplayCamera(false)// 是否显示拍照按钮
                .isGif(false)// 是否显示gif图片
                .isOriginalSkipCompress(true)// 是否压缩
                .setMaxSelectNum(max)
                .isSelectZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .setCompressEngine(new CompressFileEngine() {
                    @Override
                    public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
                        Luban.with(context).load(source).ignoreBy(512)
                                .setCompressListener(new OnNewCompressListener() {
                                    @Override
                                    public void onStart() {

                                    }

                                    @Override
                                    public void onSuccess(String source, File compressFile) {
                                        if (call != null) {
                                            call.onCallback(source, compressFile.getAbsolutePath());
                                        }
                                    }

                                    @Override
                                    public void onError(String source, Throwable e) {
                                        if (call != null) {
                                            call.onCallback(source, null);
                                        }
                                    }
                                }).launch();
                    }
                })
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result.size() > 0) {
                            List<String> list = new ArrayList<>();
                            for (LocalMedia photo : result) {
                                list.add(photo.getRealPath());
                            }
                            iSelectImgResult.result(list);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    //拍照
    public void takePhoto(Activity activity,ISelectImgResult iSelectImgResult){
        PictureSelector.create(activity)
                .openCamera(SelectMimeType.ofImage())
                .isOriginalSkipCompress(true)// 是否压缩
                .setCompressEngine(new CompressFileEngine() {
                    @Override
                    public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
                        Luban.with(context).load(source).ignoreBy(100)
                                .setCompressListener(new OnNewCompressListener() {
                                    @Override
                                    public void onStart() {

                                    }

                                    @Override
                                    public void onSuccess(String source, File compressFile) {
                                        if (call != null) {
                                            call.onCallback(source, compressFile.getAbsolutePath());
                                        }
                                    }

                                    @Override
                                    public void onError(String source, Throwable e) {
                                        if (call != null) {
                                            call.onCallback(source, null);
                                        }
                                    }
                                }).launch();
                    }
                })
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result.size() > 0) {
                            List<String> list = new ArrayList<>();
                            for (LocalMedia photo : result) {
                                list.add(photo.getRealPath());
                            }
                            iSelectImgResult.result(list);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }
}
