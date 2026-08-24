package com.hf.network;

import android.content.Context;

import java.io.File;
import java.util.Map;

/**
 * @author tx
 * @date 2023/5/26 15:29
 * @target
 * - PUT和POST请求体同时支持表单和JSON格式。
 * - PATCH请求体只支持JSON格式,不支持表单格式。
 * - DELETE请求体一般只支持表单格式,很少使用JSON格式。这是因为每个HTTP方法都有不同的语义及使用场景,对请求体的数据格式和结构有不同的要求:- PUT和POST对应完整资源和复杂事件,需要支持表单和JSON。
 * - PATCH对应更新对象,需要JSON对象格式,表单不够用。
 * - DELETE对应简单ID,表单格式就足够,一般不需要JSON。
 */
public interface IHttpAction {
    void postForm(String api, Map map, ResponseObject responseObject);

    void postJson(String api,Object objec, ResponseObject responseObject);

    void get(String api,Object objec, ResponseObject responseObject);

    void delete(String api,Object objec, ResponseObject responseObject);

    void patch(String api,Object objec, ResponseObject responseObject);

    void putForm(String api,Map map, ResponseObject responseObject);

    void putJson(String api,Object objec, ResponseObject responseObject);

    void upload(String api, File file,ResponseObject responseObject);

    void download(Context context, String url,String saveFileName,IFileDownloadResult iFileDownloadResult);
}
