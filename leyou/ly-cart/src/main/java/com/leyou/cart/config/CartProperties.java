package com.leyou.cart.config;


import com.leyou.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * 购物车配置类
 */
@ConfigurationProperties(prefix = "leyou.jwt")
@Data
public class CartProperties {
    private String pubKeyPath;//公钥地址
    private String cookieName;

    private PublicKey publicKey;

    //属性一旦实例化后(上面的属性值被赋值以后)，就应该读取公钥私钥
    @PostConstruct
    public void init() throws Exception {
        //获取公钥私钥
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}
