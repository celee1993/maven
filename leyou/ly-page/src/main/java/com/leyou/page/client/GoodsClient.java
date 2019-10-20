package com.leyou.page.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-ltem-service")
public interface GoodsClient extends GoodsApi {
}
