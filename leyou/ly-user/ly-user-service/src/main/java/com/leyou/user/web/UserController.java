package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册校验
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,@PathVariable(value = "type") Integer type) {
       Boolean flag= userService.checkData(data,type);
        return ResponseEntity.ok(flag);
    }

    /**
     * 用户注册发送手机验证码
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendConfirmCode(@RequestParam("phone") String phone) {
        userService.sendConfirmCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code) {
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 授权中心验证
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<User> findUserByUsernameAndPassword(@RequestParam("username")String username,@RequestParam("password") String password) {
        User user =userService.findUser(username,password);
        return ResponseEntity.ok(user);
    }

}
