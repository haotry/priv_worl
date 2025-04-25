package com.hltj.api.dto;

import lombok.Data;

@Data
public class ContentDTO {

    private Long id;

    private Integer type; // 1: article, 2: video, 3: moment

    private String time;

    private String title;

    private String content;

    private UserContentDTO user;

    @Data
    public static class UserContentDTO {

        private String name;

        private String avatar;

        private String jwcode;

        private String video;

        private Integer likeCount;

        private Integer liked; // 0: not liked, 1: liked

        private Integer commentCount;

        private Integer favoriteCount;

        private Integer favorited; // 0: not favorited, 1: favorited

        private String image;

        private String[] images;

        private Integer userIdentity; // 0: regular user, 1: teacher
    }
}