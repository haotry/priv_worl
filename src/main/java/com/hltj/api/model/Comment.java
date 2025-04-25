package com.hltj.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("hl_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId; // ID of the content being commented on (article, video, or moment)

    private String jwcode; // User who made the comment

    private String content;

    private Date createTime;

    private Long replyId; // ID of the comment this is replying to (0 if it's a root comment)

    private Long replySubId; // ID of the sub-comment this is replying to (0 if replying to a root comment)
}