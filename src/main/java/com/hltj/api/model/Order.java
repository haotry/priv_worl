package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String jwcode;

    private Long lessonId;

    private String lessonName;

    private String lessonImage;

    private Integer price;

    private Date createTime;
}