package com.leyou.order.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 订单配置类
 */
@ConfigurationProperties(prefix = "leyou.worker")
@Data
public class IdWorkerProperties {
    private long workerId;
    private long dataCenterId;
}
