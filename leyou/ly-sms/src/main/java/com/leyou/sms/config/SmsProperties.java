package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "leyou.sms")
public class SmsProperties {

    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String verifyCodeTemplate;
}
