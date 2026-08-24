package com.hf.network;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author tx
 * @date 2023/5/26 15:36
 * @target 构建BaseRequester完成HTTP请求
 */
public class BaseRequester implements IHttpAction {
    private boolean retryMode = false;
    //client
    BaseClient baseClient;
    //header
    Map<String, String> headerMap=new HashMap<>();

    public void setRetryMode(boolean retryMode){
        this.retryMode = retryMode;
    }

    public interface IRequestDone{
        void done();
    }

    //构建默认的BaseRequester
    public BaseRequester() {
        baseClient = new BaseClient();
    }

    //构建自定义连接和请求时间的BaseRequester
    public BaseRequester(int customerConnectTimes, int customerReadTimes) {
        baseClient = new BaseClient(customerConnectTimes, customerReadTimes);
    }

    //获取header
    public Map<String, String> getHeader() {
        return headerMap;
    }

    //设置header
    public void setHeader(Map<String, String> map) {
        headerMap.putAll(map);
    }

    //设置header
    public void setHeader(Object obj) {
        Map<String, String> map = obj2Map(obj);
        headerMap.putAll(map);
    }

    private Request.Builder getRequestBuilder(){
        Request.Builder builder = new Request.Builder();
        for (String key : headerMap.keySet()) {
            builder.addHeader(key, headerMap.get(key));
        }
        return builder;
    }

    @Override
    public void postForm(String api, Map map, ResponseObject responseObject) {
        Request request = getRequestBuilder().post(map2FormBody(map))
                .addHeader("signature",MD5.md5(MD5.md5(JSON.toJSONString(map))))
                .url(api).build();
        readyToCall(request,responseObject);
    }

    public void postForm(String api, Object obj, ResponseObject responseObject) {
        Map<String, String> map = obj2Map(obj);
        postForm(api, map, responseObject);
    }

    @Override
    public void postJson(String api, Object objec, ResponseObject responseObject) {
        Request request = getRequestBuilder().post(object2JsonBody(objec))
                .addHeader("signature",MD5.md5(MD5.md5(JSON.toJSONString(objec)))).url(api).build();
        readyToCall(request,responseObject);
    }

    @Override
    public void get(String api, Object objec, ResponseObject responseObject) {
        Map<String, String> map = obj2Map(objec);
        get(api, map, responseObject);
    }

    public void get(String api, Map<String, String> map, ResponseObject responseObject) {
        if (map != null && map.size() > 0) {
            api = api + "?";
            for (String key : map.keySet()) {
                api = api + key + "=" + map.get(key) + "&";
            }
            api = api.substring(0, api.length() - 1);
        }
        String signature = api.split("\\?").length==2?api.split("\\?")[1]:"";
        Request request = getRequestBuilder()
                .addHeader("signature",MD5.md5(MD5.md5(signature)))
                .get().url(api).build();
        readyToCall(request,responseObject);
    }

    public void getAsPc(String api, Object objec, ResponseObject responseObject) {
        Map<String, String> map = obj2Map(objec);
        getAsPc(api, map, responseObject);
    }
    public void getAsPc(String api, Map<String,String> map, ResponseObject responseObject){
        if (map != null && map.size() > 0) {
            api = api + "?";
            for (String key : map.keySet()) {
                api = api + key + "=" + map.get(key) + "&";
            }
            api = api.substring(0, api.length() - 1);
        }
        Request request = getRequestBuilder()
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0")
                .get().url(api).build();
        readyToCall(request,responseObject);
    }

    @Override
    public void delete(String api, Object obj, ResponseObject responseObject) {
        Map<String, String> map = obj2Map(obj);
        delete(api, map, responseObject);
    }

    public void delete(String api, Map<String, String> map, ResponseObject responseObject) {
        if (map != null && map.size() > 0) {
            api = api + "?";
            for (String key : map.keySet()) {
                api = api + key + "=" + map.get(key) + "&";
            }
            api = api.substring(0, api.length() - 1);
        }
        Request request = getRequestBuilder()
                .addHeader("signature",MD5.md5(MD5.md5(JSON.toJSONString(map)))).delete().url(api).build();
        readyToCall(request,responseObject);
    }


    @Override
    public void patch(String api, Object objec, ResponseObject responseObject) {
        Request request = getRequestBuilder().patch(object2JsonBody(objec))
                .addHeader("signature",MD5.md5(MD5.md5(JSON.toJSONString(objec)))).url(api).build();
        readyToCall(request,responseObject);
    }

    @Override
    public void putForm(String api, Map map, ResponseObject responseObject) {
        Request request = getRequestBuilder().put(map2FormBody(map))
                .addHeader("signature",MD5.md5(MD5.md5(JSON.toJSONString(map))))
                .url(api).build();
        readyToCall(request,responseObject);
    }

    public void putForm(String api, Object obj, ResponseObject responseObject) {
        Map<String, String> map = obj2Map(obj);
        postForm(api, map, responseObject);
    }

    @Override
    public void putJson(String api, Object objec, ResponseObject responseObject) {
        Request request = getRequestBuilder()
                .put(object2JsonBody(objec))
                .addHeader("signature",MD5.md5(MD5.md5(JSON.toJSONString(objec))))
                .url(api).build();
        readyToCall(request,responseObject);
    }

    //上传文件
    @Override
    public void upload(String api, File file, ResponseObject responseObject) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        Request request = getRequestBuilder().url(api)
                .post(filePart.body())
                .build();
        readyToCall(request,responseObject);
    }

    //下载文件
    @Override
    public void download(Context context,String url,String saveFileName,IFileDownloadResult iFileDownloadResult) {
        String savePath = context.getFilesDir().getAbsolutePath()+"/"+saveFileName;
        FileDownloader.setup(context);
        FileDownloader.getImpl().create(url)
                .setPath(savePath)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        int progress= (int)((((float)soFarBytes)/((float)totalBytes))*100);
                        if(progress!=100) {
                            iFileDownloadResult.result(progress, savePath);
                        }
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        iFileDownloadResult.result(100, savePath);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        iFileDownloadResult.result(-1, savePath);
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();
    }

    //request已经构建好了，开始发起请求
    private <T> void readyToCall(Request request,ResponseObject<T> responseObject) {
        if(retryMode){
            readyToCallRetryMode(request,responseObject);
            return;
        }
        baseClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if(responseObject.iRequestDone!=null){
                    responseObject.iRequestDone.done();
                }
                //请求IO异常
                responseObject.failed(9000, e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(responseObject.iRequestDone!=null){
                    responseObject.iRequestDone.done();
                }
                //请求成功
                if (response.isSuccessful()) {
                    //获取得到的返回
                    String responseData = response.body().string();
                    Log.e("response",request.url()+"\n->"+(responseData.length()>30?responseData.substring(0,25):responseData));
                    //转化HttpData实例，接口必须符合结构规范
                    HttpData httpData = null;
                    try {
                        httpData = JSON.parseObject(responseData, HttpData.class);
                    } catch (Exception e) {
                        if(request.headers().names().contains("User-Agent")) {
                            responseObject.result((T)responseData);
                        }else {
                            responseObject.failed(6000, "转化HttpData实例，接口必须符合结构规范");
                        }
                        return;
                    }
                    //业务逻辑成功
                    if (httpData.ok()) {
                        String dataString = httpData.result;
                        if(dataString==null||dataString.isEmpty()){
                            responseObject.result(null);
                            responseObject.resultList(new ArrayList<>());
                            return;
                        }
                        //鉴别数组
                        if (dataString.startsWith("[") && dataString.endsWith("]")) {
                            List<T> data = JSON.parseArray(httpData.result, responseObject.getT());
                            responseObject.tList = data;
                            responseObject.resultList(data);
                            //鉴别对象
                        } else if (dataString.startsWith("{") && dataString.endsWith("}")) {
                            T data = JSON.parseObject(httpData.result, responseObject.getT());
                            responseObject.t = data;
                            responseObject.result(data);
                            //鉴别结果为非JSON,执行强转
                        } else {
                            responseObject.t = (T) httpData.result;
                            responseObject.result((T) httpData.result);
                        }
                    } else {
                        //业务逻辑失败
                        responseObject.failed(httpData.code, httpData.message);
                    }
                } else {
                    //请求异常
                    Log.e("response",request.url()+"\n->"+response.code()+"-"+response.message());
                    responseObject.failed(response.code(), response.message());
                }
            }
        });
    }

    private <T> void readyToCallRetryMode(Request request,ResponseObject<T> responseObject){
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if(responseObject.iRequestDone!=null){
                    responseObject.iRequestDone.done();
                }
                //请求IO异常
                Log.e("response",request.url()+"\n->"+e.getMessage());
                responseObject.failed(9000, e.getMessage());

                Log.e("response",request.url()+":retry");
                baseClient.getClient().newCall(request).enqueue(this);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(responseObject.iRequestDone!=null){
                    responseObject.iRequestDone.done();
                }
                //请求成功
                if (response.isSuccessful()) {
                    //获取得到的返回
                    String responseData = response.body().string();
                    Log.e("response",request.url()+"\n->"+responseData);
                    //转化HttpData实例，接口必须符合结构规范
                    HttpData httpData = null;
                    try {
                        httpData = JSON.parseObject(responseData, HttpData.class);
                    } catch (Exception e) {
                        if(request.headers().names().contains("User-Agent")) {
                            responseObject.result((T)responseData);
                        }else {
                            responseObject.failed(6000, "转化HttpData实例，接口必须符合结构规范");
                        }
                        return;
                    }
                    //业务逻辑成功
                    if (httpData.ok()) {
                        String dataString = httpData.result;
                        if(dataString==null||dataString.isEmpty()){
                            responseObject.result(null);
                            responseObject.resultList(new ArrayList<>());
                            return;
                        }
                        //鉴别数组
                        if (dataString.startsWith("[") && dataString.endsWith("]")) {
                            List<T> data = JSON.parseArray(httpData.result, responseObject.getT());
                            responseObject.tList = data;
                            responseObject.resultList(data);
                            //鉴别对象
                        } else if (dataString.startsWith("{") && dataString.endsWith("}")) {
                            T data = JSON.parseObject(httpData.result, responseObject.getT());
                            responseObject.t = data;
                            responseObject.result(data);
                            //鉴别结果为非JSON,执行强转
                        } else {
                            responseObject.t = (T) httpData.result;
                            responseObject.result((T) httpData.result);
                        }
                    } else {
                        //业务逻辑失败
                        responseObject.failed(httpData.code, httpData.message);
                    }
                } else {
                    //请求异常
                    Log.e("response",request.url()+"\n->"+response.code()+"-"+response.message());
                    responseObject.failed(response.code(), response.message());

                    Log.e("response",request.url()+":retry");
                    call.enqueue(this);
                }
            }
        };
        baseClient.getClient().newCall(request).enqueue(callback);
    }

    //map构建FormBody
    private RequestBody map2FormBody(Map map) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (map != null) {
            for (Object key : map.keySet()) {
                formBuilder.add(key.toString(), map.get(key).toString());
            }
        }
        RequestBody formBody = formBuilder.build();
        return formBody;
    }

    //object构建JsonBody
    private RequestBody object2JsonBody(Object obj) {
        String json = "";
        if (obj != null) {
            json = JSON.toJSONString(obj);
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        return body;
    }

    //object转map
    private Map<String, String> obj2Map(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        if(obj instanceof Map){
            return (Map<String, String>)obj;
        }
        Map<String, String> map = new HashMap<>();
        Class clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (field.get(obj) instanceof String) {
                    map.put(field.getName(), (String) field.get(obj));
                } else {
                    map.put(field.getName(), field.get(obj) + "");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
