package com.hf.network;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;

/**
 * @author tx
 * @date 2023/5/28 15:09
 * @target 将一个请求需要组装的请求体抽象出来，通过实现EasyApi完成请求的独立抽象，使用起来比BaseRequester更简单
 */
public class EasyRequester extends BaseRequester{

    public enum REQUEST_TYPE {POST_FORM,POST_JSON,GET,PUT_FORM,PUT_JSON,PATCH,DELETE,UPLOAD,GET_AS_PC}

    public interface EasyApi<T>{
        REQUEST_TYPE getRequestType();

        String getApiUrl();

        Object getApiParams();

        File getUploadFile();

        ResponseObject<T> getApiResponseObject();
    }

    public void request(EasyApi easyApi,IRequestDone iRequestDone){
        Log.e("request",easyApi.getApiUrl()+"\n->"+easyApi.getRequestType()+"\n->"+ JSON.toJSON(easyApi.getApiParams()));
        REQUEST_TYPE request_type = easyApi.getRequestType();
        ResponseObject responseObject =easyApi.getApiResponseObject();
        responseObject.iRequestDone = iRequestDone;
        switch (request_type){
            case POST_FORM:{
                postForm(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case POST_JSON:{
                postJson(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case GET:{
                get(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case PUT_FORM:{
                putForm(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case PUT_JSON:{
                putJson(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case PATCH:{
                patch(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case DELETE:{
                delete(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
            case UPLOAD:{
                upload(easyApi.getApiUrl(),easyApi.getUploadFile(),responseObject);
                break;
            }
            case GET_AS_PC:{
                getAsPc(easyApi.getApiUrl(),easyApi.getApiParams(),responseObject);
                break;
            }
        }
    }
}
