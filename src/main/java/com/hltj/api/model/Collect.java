package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_collect")
public class Collect {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId; // ID of the content being collected

    private Integer contentType; // 1: article, 2: video, 3: moment

    private String jwcode; // User who collected

    private Date createTime;
}