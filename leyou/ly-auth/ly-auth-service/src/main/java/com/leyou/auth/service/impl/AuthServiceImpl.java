package com.leyou.auth.service.impl;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.AuthProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@EnableConfigurationProperties(AuthProperties.class)
public class AuthServiceImpl implements AuthService {


    @Autowired
    private AuthProperties prop;

    @Autowired
    private UserClient userClient;

    /**
     *  登录进行授权认证发放token
     * @return
     * @param username
     * @param password
     */
    @Override
    public String login(String username, String password) {
        try {
            User user = userClient.findUserByUsernameAndPassword(username, password);
            if (user == null) {
                throw new LyException(ExceptionEnum.INVALID_USERNAME_OR_PASSWORD);
            }
            //转换为UserInfo然后加密
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(username);
            //进行加密验证获取token
            String token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(),prop.getExpire());
            return token;
        } catch (Exception e) {
            log.error("用户名称或者密码错误，用户名称：{}",username,e);
            throw new LyException(ExceptionEnum.CREATE_TOKEN_ERROR);
        }
    }



}
