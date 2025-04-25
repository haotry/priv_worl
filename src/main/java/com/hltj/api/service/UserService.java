package com.hltj.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.dto.LoginRequest;
import com.hltj.api.dto.LoginResponse;
import com.hltj.api.dto.PageResult;
import com.hltj.api.dto.RegisterRequest;
import com.hltj.api.dto.UserDTO;
import com.hltj.api.model.Order;
import com.hltj.api.model.User;

public interface UserService {

    LoginResponse loginByJwcode(LoginRequest request);

    LoginResponse loginByPhone(LoginRequest request);

    void register(RegisterRequest request);

    PageResult<UserDTO> getAllUsers(Integer page, Integer pageSize, String token);

    /**
     * 按角色筛选用户列表
     * @param userIdentity 用户角色标识（0=管理员，1=普通用户，2=讲师，null=全部）
     * @param page 页码
     * @param pageSize 每页条数
     * @param token 授权token
     * @return 分页用户列表
     */
    PageResult<UserDTO> getUserListByRole(Integer userIdentity, Integer page, Integer pageSize, String token);

    /**
     * 更新用户角色
     * @param jwcode 用户学号
     * @param userIdentity 用户角色（0=管理员，1=普通用户，2=讲师）
     * @param token 授权token
     * @return 是否更新成功
     */
    Boolean updateUserRole(String jwcode, Integer userIdentity, String token);

    PageResult<UserDTO> getFollowList(String token);

    UserDTO getUserInfo(String jwcode, String token);

    Boolean follow(String jwcode, String token);

    Boolean unfollow(String jwcode, String token);

    Boolean updateAvatar(String jwcode, String avatar, String token);

    Boolean updatePassword(String jwcode, String oldPassword, String newPassword, String token);

    Boolean updateName(String jwcode, String name, String token);

    PageResult<Order> getUserOrders(String token);

    Integer addCredit(String jwcode, Integer credit, String token);
}