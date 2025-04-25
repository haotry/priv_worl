package com.hltj.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.common.JwtTokenUtil;
import com.hltj.api.dto.LoginRequest;
import com.hltj.api.dto.LoginResponse;
import com.hltj.api.dto.PageResult;
import com.hltj.api.dto.RegisterRequest;
import com.hltj.api.dto.UserDTO;
import com.hltj.api.mapper.FollowMapper;
import com.hltj.api.mapper.LikeMapper;
import com.hltj.api.mapper.OrderMapper;
import com.hltj.api.mapper.UserMapper;
import com.hltj.api.model.Follow;
import com.hltj.api.model.Order;
import com.hltj.api.model.User;
import com.hltj.api.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginResponse loginByJwcode(LoginRequest request) {
        User user = userMapper.findByJwcode(request.getJwcode());
        if (user == null) {
            throw new BadCredentialsException("Invalid jwcode or password");
        }

        // 测试账号11112222无需密码验证
        if (!"11112222".equals(request.getJwcode())) {
            // 如果密码为null或请求中没有密码，则不进行密码验证
            if (request.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid jwcode or password");
            }
        }

        String token = jwtTokenUtil.generateToken(user.getJwcode());
        LoginResponse response = new LoginResponse();
        UserDTO userDTO = convertToDTO(user);

        response.setToken(token);
        response.setHluser(userDTO);

        return response;
    }

    @Override
    public LoginResponse loginByPhone(LoginRequest request) {
        User user = userMapper.findByPhone(request.getPhone());
        if (user == null) {
            throw new BadCredentialsException("Invalid phone or password");
        }

        // 如果密码为null或请求中没有密码，则不进行密码验证
        if (request.getPassword() != null && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid phone or password");
        }

        String token = jwtTokenUtil.generateToken(user.getJwcode());
        LoginResponse response = new LoginResponse();
        UserDTO userDTO = convertToDTO(user);

        response.setToken(token);
        response.setHluser(userDTO);

        return response;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        User existingUser = userMapper.findByPhone(request.getPhone());
        if (existingUser != null) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = new User();
        user.setTel(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreateTime(new Date());
        user.setCredit(0);
        user.setUserIdentity(0); // Regular user by default

        // Generate a random jwcode
        String jwcode = String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10, 7)));
        user.setJwcode(jwcode);

        // Default name and avatar
        user.setName("User" + jwcode.substring(jwcode.length() - 4));
        user.setAvatar("");

        userMapper.insert(user);
    }

    @Override
    public PageResult<UserDTO> getAllUsers(Integer page, Integer pageSize, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);

        Page<User> pageParam = new Page<>(page, pageSize);
        Page<User> userPage = userMapper.selectPage(pageParam, null);

        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserDTO userDTO = convertToDTO(user);
            userDTO.setIsGuanzhu(followMapper.checkFollowStatus(currentJwcode, user.getJwcode()) > 0 ? 1 : 0);
            userDTOs.add(userDTO);
        }

        return PageResult.of(userPage.getTotal(), userDTOs);
    }

    @Override
    public PageResult<UserDTO> getFollowList(String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getFollowerJwcode, currentJwcode);
        List<Follow> follows = followMapper.selectList(queryWrapper);

        List<UserDTO> userDTOs = new ArrayList<>();
        for (Follow follow : follows) {
            User user = userMapper.findByJwcode(follow.getFollowedJwcode());
            if (user != null) {
                UserDTO userDTO = convertToDTO(user);
                userDTO.setIsGuanzhu(1); // Already following
                userDTOs.add(userDTO);
            }
        }

        return PageResult.of((long) userDTOs.size(), userDTOs);
    }

    @Override
    public UserDTO getUserInfo(String jwcode, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        User user = userMapper.findByJwcode(jwcode);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        UserDTO userDTO = convertToDTO(user);

        // Check follow status
        userDTO.setIsGuanzhu(followMapper.checkFollowStatus(currentJwcode, jwcode) > 0 ? 1 : 0);

        return userDTO;
    }

    @Override
    @Transactional
    public Boolean follow(String jwcode, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        if (currentJwcode.equals(jwcode)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        // Check if already following
        if (followMapper.checkFollowStatus(currentJwcode, jwcode) > 0) {
            return true; // Already following
        }

        Follow follow = new Follow();
        follow.setFollowerJwcode(currentJwcode);
        follow.setFollowedJwcode(jwcode);
        follow.setCreateTime(new Date());

        followMapper.insert(follow);
        return true;
    }

    @Override
    @Transactional
    public Boolean unfollow(String jwcode, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getFollowerJwcode, currentJwcode)
                    .eq(Follow::getFollowedJwcode, jwcode);

        followMapper.delete(queryWrapper);
        return true;
    }

    @Override
    @Transactional
    public Boolean updateAvatar(String jwcode, String avatar, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        if (!currentJwcode.equals(jwcode)) {
            throw new RuntimeException("Cannot update another user's avatar");
        }

        userMapper.updateAvatar(jwcode, avatar);
        return true;
    }

    @Override
    @Transactional
    public Boolean updatePassword(String jwcode, String oldPassword, String newPassword, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        if (!currentJwcode.equals(jwcode)) {
            throw new RuntimeException("Cannot update another user's password");
        }

        User user = userMapper.findByJwcode(jwcode);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        userMapper.updatePassword(jwcode, passwordEncoder.encode(newPassword));
        return true;
    }

    @Override
    @Transactional
    public Boolean updateName(String jwcode, String name, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        if (!currentJwcode.equals(jwcode)) {
            throw new RuntimeException("Cannot update another user's name");
        }

        userMapper.updateName(jwcode, name);
        return true;
    }

    @Override
    public PageResult<Order> getUserOrders(String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getJwcode, currentJwcode)
                   .orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(queryWrapper);

        return PageResult.of((long) orders.size(), orders);
    }

    @Override
    @Transactional
    public Integer addCredit(String jwcode, Integer credit, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        if (!currentJwcode.equals(jwcode)) {
            throw new RuntimeException("Cannot add credit to another user");
        }

        userMapper.addCredit(jwcode, credit);

        User user = userMapper.findByJwcode(jwcode);
        return user.getCredit();
    }

    @Override
    public PageResult<UserDTO> getUserListByRole(Integer userIdentity, Integer page, Integer pageSize, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);
        // 检查当前用户是否是管理员
        User currentUser = userMapper.findByJwcode(currentJwcode);
        if (currentUser == null || currentUser.getUserIdentity() != 0) {
            throw new RuntimeException("No permission");
        }

        Page<User> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 如果指定了角色，则按角色筛选
        if (userIdentity != null) {
            queryWrapper.eq(User::getUserIdentity, userIdentity);
        }

        Page<User> userPage = userMapper.selectPage(pageParam, queryWrapper);

        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : userPage.getRecords()) {
            UserDTO userDTO = convertToDTO(user);
            userDTO.setIsGuanzhu(followMapper.checkFollowStatus(currentJwcode, user.getJwcode()) > 0 ? 1 : 0);
            userDTOs.add(userDTO);
        }

        return PageResult.of(userPage.getTotal(), userDTOs);
    }

    @Override
    @Transactional
    public Boolean updateUserRole(String jwcode, Integer userIdentity, String token) {
        String currentJwcode = jwtTokenUtil.extractUsername(token);

        // 检查当前用户是否是管理员
        User currentUser = userMapper.findByJwcode(currentJwcode);
        if (currentUser == null || currentUser.getUserIdentity() != 0) {
            throw new RuntimeException("No permission");
        }

        // 检查要修改的用户是否存在
        User targetUser = userMapper.findByJwcode(jwcode);
        if (targetUser == null) {
            throw new RuntimeException("User not found");
        }

        // 检查身份值是否合法（0=管理员，1=普通用户，2=讲师）
        if (userIdentity < 0 || userIdentity > 2) {
            throw new RuntimeException("Invalid userIdentity value");
        }

        // 更新用户角色
        targetUser.setUserIdentity(userIdentity);
        userMapper.updateById(targetUser);

        return true;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        // Format date to string
        userDTO.setCreateTime(user.getCreateTime().toString());

        // Get follower and following counts
        userDTO.setFollowerCount(followMapper.countFollowers(user.getJwcode()));
        userDTO.setFollowCount(followMapper.countFollowing(user.getJwcode()));

        // Get likes received
        userDTO.setHuozan(likeMapper.countUserLikes(user.getJwcode()));

        return userDTO;
    }
}