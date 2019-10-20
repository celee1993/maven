package com.leyou.auth.client;


import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient("ly-user-service")
public interface UserClient extends UserApi {
}
