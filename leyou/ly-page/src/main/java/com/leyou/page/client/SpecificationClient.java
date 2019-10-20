package com.leyou.page.client;


import com.leyou.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-ltem-service")
public interface SpecificationClient extends SpecificationApi {
}
