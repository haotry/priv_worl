package com.hltj.api.controller;

import com.hltj.api.common.Result;
import com.hltj.api.dto.LoginRequest;
import com.hltj.api.dto.LoginResponse;
import com.hltj.api.dto.RegisterRequest;
import com.hltj.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hltj")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login/jwcode")
    public Result<LoginResponse> loginByJwcode(@RequestBody LoginRequest request) {
        logger.info("接收到学号登录请求: {}", request.getJwcode());
        try {
            LoginResponse response = userService.loginByJwcode(request);
            logger.info("登录成功: {}", request.getJwcode());
            return Result.success(response);
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login/phone")
    public Result<LoginResponse> loginByPhone(@RequestBody LoginRequest request) {
        logger.info("接收到手机号登录请求: {}", request.getPhone());
        try {
            LoginResponse response = userService.loginByPhone(request);
            logger.info("登录成功: {}", request.getPhone());
            return Result.success(response);
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        logger.info("接收到注册请求: {}", request.getPhone());
        try {
            userService.register(request);
            logger.info("注册成功: {}", request.getPhone());
            return Result.success();
        } catch (Exception e) {
            logger.error("注册失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}