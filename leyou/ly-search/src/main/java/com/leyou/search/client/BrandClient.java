package com.leyou.search.client;

import com.leyou.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-ltem-service")
public interface BrandClient extends BrandApi {
}
