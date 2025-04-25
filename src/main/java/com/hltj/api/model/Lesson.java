package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_lesson")
public class Lesson {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String cover;

    private String author;

    private String avatar;

    private String content;

    private String intro;

//    private String purchaseNotes;
    private String purchase_notes;

    private String video;

    private String image;

    private Integer price;

    private Integer participantCount; // Number of participants

    private Date createTime;

    private Long menuId; // Category ID
}