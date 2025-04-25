package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hl_lesson_menu")
public class LessonMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String image;
}