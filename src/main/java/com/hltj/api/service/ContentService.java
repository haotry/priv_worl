package com.hltj.api.service;

import com.hltj.api.dto.ContentDTO;
import com.hltj.api.dto.PageResult;
import com.hltj.api.dto.VideoDTO;

public interface ContentService {

    PageResult<ContentDTO> getFollowShareList(Integer page, Integer pageSize, String token);

    PageResult<VideoDTO> getRecommendVideos(Integer page, Integer pageSize, String token);

    PageResult<VideoDTO> getVideoList(Integer page, Integer pageSize, String token, Long id);

    Boolean likeContent(Long id, Integer status, String token);

    PageResult<ContentDTO> getLikeList(Integer page, Integer pageSize, String token);

    Boolean collectContent(Long id, Integer status, String token);

    PageResult<ContentDTO> getCollectList(Integer page, Integer pageSize, String token);

    PageResult<ContentDTO> getArticles(Integer page, Integer pageSize, String token);

    ContentDTO getArticleDetail(String jwcode, Long id, String token);

    ContentDTO getMomentDetail(Long id, String token);

    PageResult<ContentDTO> getUserMomentList(String jwcode, Integer page, Integer pageSize, String token);

    Long publishMoment(String content, String images, String token);

    Long publishArticle(String title, String content,String url, String token);

    Long publishVideo(String title, String content, String video, String url, String token);

    /**
     * 删除文章
     * @param id 文章ID
     * @param token JWT令牌
     * @return 删除结果
     */
    Boolean deleteArticle(Long id, String token);

    /**
     * 删除视频
     * @param id 视频ID
     * @param token JWT令牌
     * @return 删除结果
     */
    Boolean deleteVideo(Long id, String token);
}