package com.leyou.user.service;

import com.leyou.user.pojo.User;

public interface UserService {
    Boolean checkData(String data, Integer type);

    void sendConfirmCode(String phone);

    void register(User user, String code);

    User findUser(String username, String password);
}
