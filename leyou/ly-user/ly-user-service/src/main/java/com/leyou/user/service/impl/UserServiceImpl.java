package com.leyou.user.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.config.UserProperties;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.user.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableConfigurationProperties(UserProperties.class)
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserProperties prop;

    private static final String KEY_PREFIX = "sms:code:";


    /**
     * 用户注册校验
     *
     * @param data
     * @param type
     * @return
     */
    @Override
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        //判断请求参数类型
        switch (type) {
            //用户名
            case 1:
                user.setUsername(data);
                break;
            //手机
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.DATA_ERROR);
        }
        return userMapper.selectCount(user) == 0;
    }

    /**
     * 用户注册发送验证码的mq通知
     *
     * @param phone
     */
    @Override
    public void sendConfirmCode(String phone) {
        String key = KEY_PREFIX + phone;
        //得到验证码
        String code = getCode();
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        amqpTemplate.convertAndSend(prop.getExchangeName(), prop.getRoutingKey(), msg);
        //同时储存验证码到redis
        stringRedisTemplate.opsForValue().set(key, code,prop.getTimeOut(), TimeUnit.MINUTES);
    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    @Override
    public void register(User user, String code) {
        //判断验证码
        String key = KEY_PREFIX + user.getPhone();
        String code_redis = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code,code_redis)) {
            //验证码不存在
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //密码加密
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        //写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    /**
     * 根据用户名、密码查询用户
     * @param username
     * @param password
     * @return
     */
    @Override
    public User findUser(String username, String password) {
        //先直接根据用户名查询 因为用户名字段添加了索引
        User user = new User();
        user.setUsername(username);
        User user_ = userMapper.selectOne(user);
        //校验用户名
        if (user_ == null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_OR_PASSWORD);
        }
        //校验密码
        if (!StringUtils.equals(user_.getPassword(),CodecUtils.md5Hex(password,user_.getSalt()))) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_OR_PASSWORD);
        }
        //用户名密码正确
        return user_;
    }


    /**
     * 随机产生一个6位的数字字符串
     *
     * @return
     */
    private String getCode() {
        List<Integer> code_num = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int num = new Random().nextInt(10);
            code_num.add(num);
        }
        return StringUtils.join(code_num, "");
    }

}
