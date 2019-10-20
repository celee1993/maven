package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;


/**
 * 业务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.leyou.item.mapper")
public class LyItem {
    public static void main(String[] args) {
        SpringApplication.run(LyItem.class);
    }
}
