package com.hf.image;

import java.util.List;

/**
 * @author tx
 * @date 2023/5/31 16:16
 * @target 选择图片回调
 */
public interface ISelectImgResult {
    void result(List<String> pathList);
}
