package com.hltj.api.controller;

import com.hltj.api.common.Result;
import com.hltj.api.dto.PageResult;
import com.hltj.api.dto.UserDTO;
import com.hltj.api.model.Order;
import com.hltj.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hltj")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/allusers")
    public Result<PageResult<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.getAllUsers(page, pageSize, token.substring(7)));
    }

    /**
     * 获取用户列表，支持按角色筛选
     * @param userIdentity 用户角色（0=管理员，1=普通用户，2=讲师，不传则查询所有）
     * @param page 页码
     * @param pageSize 每页条数
     * @param token 授权token
     * @return 分页用户列表
     */
    @GetMapping("/user/list")
    public Result<PageResult<UserDTO>> getUserList(
            @RequestParam(required = false) Integer userIdentity,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.getUserListByRole(userIdentity, page, pageSize, token.substring(7)));
    }

    /**
     * 修改用户角色
     * @param jwcode 用户学号
     * @param userIdentity 用户角色（0=管理员，1=普通用户，2=讲师）
     * @param token 授权token
     * @return 修改结果
     */
    @PostMapping("/user/role")
    public Result<Boolean> updateUserRole(
            @RequestBody Map<String, Object> params,
            @RequestHeader("Authorization") String token) {
        String jwcode = params.get("jwcode").toString();
        Integer userIdentity = Integer.parseInt(params.get("userIdentity").toString());
        return Result.success(userService.updateUserRole(jwcode, userIdentity, token.substring(7)));
    }

    @GetMapping("/followList")
    public Result<PageResult<UserDTO>> getFollowList(@RequestHeader("Authorization") String token) {
        return Result.success(userService.getFollowList(token.substring(7)));
    }

    @GetMapping("/user/info")
    public Result<UserDTO> getUserInfo(
            @RequestParam String jwcode,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.getUserInfo(jwcode, token.substring(7)));
    }

    @PostMapping("/user/follow")
    public Result<Boolean> followUser(
            @RequestParam String jwcode,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.follow(jwcode, token.substring(7)));
    }

    @PostMapping("/user/unfollow")
    public Result<Boolean> unfollowUser(
            @RequestParam String jwcode,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.unfollow(jwcode, token.substring(7)));
    }

    @PostMapping("/user/avatar")
    public Result<Boolean> updateAvatar(
            @RequestParam String jwcode,
            @RequestParam String avatar,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.updateAvatar(jwcode, avatar, token.substring(7)));
    }

    @PostMapping("/user/password")
    public Result<Boolean> updatePassword(
            @RequestParam String jwcode,
            @RequestParam String oldpas,
            @RequestParam String newpas,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.updatePassword(jwcode, oldpas, newpas, token.substring(7)));
    }

    @PostMapping("/user/name")
    public Result<Boolean> updateName(
            @RequestParam String jwcode,
            @RequestParam String name,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.updateName(jwcode, name, token.substring(7)));
    }

    @GetMapping("/user/order")
    public Result<PageResult<Order>> getUserOrders(@RequestHeader("Authorization") String token) {
        return Result.success(userService.getUserOrders(token.substring(7)));
    }

    @PostMapping("/user/credit")
    public Result<Integer> addCredit(
            @RequestParam String jwcode,
            @RequestParam Integer credit,
            @RequestHeader("Authorization") String token) {
        return Result.success(userService.addCredit(jwcode, credit, token.substring(7)));
    }
}