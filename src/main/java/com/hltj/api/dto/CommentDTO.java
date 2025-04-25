package com.hltj.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommentDTO {

    private Long id;

    @JsonProperty("content_id")
    private Long contentId;

    private String jwcode;

    private String content;

    private String time;

    private String avatar;

    private String name;

    @JsonProperty("user_identity")
    private Integer userIdentity;

    @JsonProperty("reply_id")
    private Long replyId; // For reply comments

    @JsonProperty("reply_sub_id")
    private Long replySubId; // For sub-reply comments

    // 新增字段，用于我的评论列表
    @JsonProperty("content_type")
    private Integer contentType; // 1=文章, 2=视频, 3=动态

    @JsonProperty("content_title")
    private String contentTitle; // 内容标题

    private String author; // 内容作者
}