package com.hf.network;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author tx
 * @date 2023/6/30 10:34
 * @target this class will do...
 */
public class MD5 {
    public static String md5(String text) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes());
            result = toHexString(digest);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static String toHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        String hexStr;
        for (byte b : digest) {
            hexStr = Integer.toHexString(b & 0xFF);//& 0xFF处理负数
            if (hexStr.length() == 1) {//长度等于1，前面进行补0，保证最后的字符串长度为32
                hexStr = "0" + hexStr;
            }
            sb.append(hexStr);
        }

        return sb.toString();
    }
}
