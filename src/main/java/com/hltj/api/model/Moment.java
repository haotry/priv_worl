package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_moment")
public class Moment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String jwcode;

    private String content;

    private String images;

    private Date createTime;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Integer type; // 3 for moment

    @TableField(exist = false)
    private String author; // 临时添加，标记为不存在的字段
}