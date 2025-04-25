package com.hltj.api.dto;

import lombok.Data;

@Data
public class VideoDTO {

    private Long id;

    private String cover;

    private String title;

    private String name;

    private String avatar;

    private String content;

    private String video;

    private String createtime;

    private Integer likeCount;

    private Integer liked; // 0: not liked, 1: liked

    private Integer collect; // 0: not collected, 1: collected

    private Integer collectCount;

    private String jwcode;

    private Integer commentCount;
}