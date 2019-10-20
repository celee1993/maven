package com.leyou.zuul.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 白名单配置类
 */
@Data
@ConfigurationProperties(prefix = "leyou.filter")
public class FiltersProperties {


    private List<String> allowPaths;
}
