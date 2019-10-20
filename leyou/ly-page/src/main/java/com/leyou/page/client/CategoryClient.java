package com.leyou.page.client;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ly-ltem-service")
public interface CategoryClient extends CategoryApi {
}
