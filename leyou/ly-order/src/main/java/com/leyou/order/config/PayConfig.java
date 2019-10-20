package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayConfig {

    @Bean
    @ConfigurationProperties(prefix = "leyou.pay")
    public PayProperties getConfig() {
        return new PayProperties();
    }

    @Bean
    public WXPay getPay(PayProperties prop) {
        return new WXPay(prop, WXPayConstants.SignType.HMACSHA256);
    }
}
