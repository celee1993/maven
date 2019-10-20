package com.leyou.auth.config;


import com.leyou.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 认证中心配置类
 */
@ConfigurationProperties(prefix = "leyou.jwt")
@Data
public class AuthProperties {
    private String secret;//登录校验的密钥
    private String pubKeyPath;//公钥地址
    private String priKeyPath;//私钥地址
    private int expire;//30过期时间,单位分钟
    private String cookieName;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    //属性一旦实例化后(上面的属性值被赋值以后)，就应该读取公钥私钥
    @PostConstruct
    public void init() throws Exception {
        //如果公钥私钥不存在
        File file_pri = new File(priKeyPath);
        File file_pub = new File(pubKeyPath);
        if (!file_pri.exists() || !file_pub.exists()) {
            //公钥私钥不存在 先生成
            RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
        }
        //获取公钥私钥
        privateKey = RsaUtils.getPrivateKey(priKeyPath);
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}
