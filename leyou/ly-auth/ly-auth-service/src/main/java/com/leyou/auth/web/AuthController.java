package com.leyou.auth.web;

import com.leyou.auth.config.AuthProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@EnableConfigurationProperties(AuthProperties.class)
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthProperties prop;

    /**
     * 登录验证
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam("username") String username, @RequestParam("password") String password) {
        String token = authService.login(username, password);
        //写入cookie
        CookieUtils.newBuilder(response).httpOnly().request(request).build(prop.getCookieName(), token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 页面判断登录状态
     *
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_COOKIE") String token,HttpServletRequest request, HttpServletResponse response) {
        try {
            //解析token
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            //重新写入cookie
            String new_token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            CookieUtils.newBuilder(response).httpOnly().request(request).build(prop.getCookieName(), new_token);
            //登陆成功返回信息
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            //token已过期或者被篡改
            throw new LyException(ExceptionEnum.INVALID_TOKEN_CODE);
        }
    }

}
