package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String jwcode;

    private String name;

    private String tel;

    private String avatar;

    private String gender;

    @JsonIgnore
    private String password;

    private Date createTime;

    private Integer credit;

    private Integer userIdentity;
}