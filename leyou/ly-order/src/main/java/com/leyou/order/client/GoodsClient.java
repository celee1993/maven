package com.leyou.order.client;

import com.leyou.api.GoodsApi;
import com.leyou.domain.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("ly-ltem-service")
public interface GoodsClient extends GoodsApi {

    @GetMapping("/sku/list/ids")
    List<Sku> findSkuListByIds(@RequestParam("ids") List<Long> ids);
}
