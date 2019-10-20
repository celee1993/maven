package com.leyou.user.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * @author: celee
 * @create: 2019-04-30 15:56
 **/
public class CodecUtils {


    /**
     * 自定义MOD5加密
     * @param data
     * @param salt
     * @return
     */
    public static String md5Hex(String data,String salt) {
        if (StringUtils.isBlank(salt)) {
            salt = data.hashCode() + "";
        }
        return DigestUtils.md5Hex(salt + DigestUtils.md5Hex(data));
    }

    /**
     * sha加密
     * @param data
     * @param salt
     * @return
     */
    public static String shaHex(String data, String salt) {
        if (StringUtils.isBlank(salt)) {
            salt = data.hashCode() + "";
        }
        return DigestUtils.sha512Hex(salt + DigestUtils.sha512Hex(data));
    }

    /**
     * 生成随机salt
     * @return
     */
    public static String generateSalt(){
        return StringUtils.replace(UUID.randomUUID().toString(), "-", "");
    }
}