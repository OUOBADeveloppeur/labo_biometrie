package com.hf.network;

/**
 * @author tx
 * @date 2023/5/31 16:29
 * @target 文件下载回调
 */
public interface IFileDownloadResult {
    void result(int progress,String path);
}
