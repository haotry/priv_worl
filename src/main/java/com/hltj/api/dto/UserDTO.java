package com.hltj.api.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String jwcode;

    private String name;

    private String tel;

    private String avatar;

    private String gender;

    private String password;

    private String createTime;

    private Integer credit;

    private Integer userIdentity;

    private Integer isGuanzhu; // 1: followed, 2: not followed

    private Integer huozan; // Number of likes received

    private Integer followCount; // Number of users this user follows

    private Integer followerCount; // Number of users following this user
}