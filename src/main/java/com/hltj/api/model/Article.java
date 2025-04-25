package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String jwcode;

    private String title;

    private String content;

    private String image;

    private Date createTime;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    @TableField(exist = false)
    private String author;
}