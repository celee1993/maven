package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 获取配置文件的自定义的配置值
 */
@Data
@ConfigurationProperties(prefix = "ly.upload")
public class UpLoadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
