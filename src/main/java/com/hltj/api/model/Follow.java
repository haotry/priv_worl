package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_follow")
public class Follow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String followerJwcode; // User who follows

    private String followedJwcode; // User being followed

    private Date createTime;
}