package com.leyou.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.user")
public class UserProperties {
    private String exchangeName;
    private String routingKey;
    private long timeOut;
}
