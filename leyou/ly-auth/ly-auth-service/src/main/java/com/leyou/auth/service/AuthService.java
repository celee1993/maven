package com.leyou.auth.service;

import com.leyou.common.entity.UserInfo;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    String login(String username, String password);


}
