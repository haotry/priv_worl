package com.hltj.api.controller;

import com.hltj.api.common.Result;
import com.hltj.api.dto.ContentDTO;
import com.hltj.api.dto.PageResult;
import com.hltj.api.dto.VideoDTO;
import com.hltj.api.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hltj")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/followShare")
    public Result<PageResult<ContentDTO>> getFollowShareList(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getFollowShareList(page, pageSize, token.substring(7)));
    }

    @GetMapping("/recommend/knowledge")
    public Result<PageResult<VideoDTO>> getRecommendVideos(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getRecommendVideos(page, pageSize, token.substring(7)));
    }

    @GetMapping("/recommend/videoList")
    public Result<PageResult<VideoDTO>> getVideoList(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam Long id,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getVideoList(page, pageSize, token.substring(7), id));
    }

    @PostMapping("/zan")
    public Result<Boolean> likeContent(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.likeContent(id, status, token.substring(7)));
    }

    @GetMapping("/user/zan/list")
    public Result<PageResult<ContentDTO>> getLikeList(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getLikeList(page, pageSize, token.substring(7)));
    }

    @PostMapping("/collect")
    public Result<Boolean> collectContent(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.collectContent(id, status, token.substring(7)));
    }

    @GetMapping("/user/collect/list")
    public Result<PageResult<ContentDTO>> getCollectList(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getCollectList(page, pageSize, token.substring(7)));
    }

    @GetMapping("/articles")
    public Result<PageResult<ContentDTO>> getArticles(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getArticles(page, pageSize, token.substring(7)));
    }

    @GetMapping("/article/detail")
    public Result<ContentDTO> getArticleDetail(
            @RequestParam String jwcode,
            @RequestParam Long id,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getArticleDetail(jwcode, id, token.substring(7)));
    }

    @GetMapping("/moment/detail")
    public Result<ContentDTO> getMomentDetail(
            @RequestParam Long id,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getMomentDetail(id, token.substring(7)));
    }

    @GetMapping("/user/moment/list")
    public Result<PageResult<ContentDTO>> getUserMomentList(
            @RequestParam String jwcode,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.getUserMomentList(jwcode, page, pageSize, token.substring(7)));
    }

    @PostMapping("/user/moment")
    public Result<Long> publishMoment(
            @RequestParam String content,
            @RequestParam String images,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.publishMoment(content, images, token.substring(7)));
    }

    @PostMapping("/user/article")
    public Result<Long> publishArticle(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam("url") String url,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.publishArticle(title, content,url, token.substring(7)));
    }

    @PostMapping("/user/video")
    public Result<Long> publishVideo(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String video,
            @RequestParam String url,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.publishVideo(title, content, video, url, token.substring(7)));
    }

    /**
     * 删除文章
     *
     * @param id    文章ID
     * @param token JWT令牌
     * @return 删除结果
     */
    @PostMapping("/user/article/delete")
    public Result<Boolean> deleteArticle(
            @RequestParam Long id,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.deleteArticle(id, token.substring(7)));
    }

    /**
     * 删除视频
     *
     * @param id    视频ID
     * @param token JWT令牌
     * @return 删除结果
     */
    @PostMapping("/user/video/delete")
    public Result<Boolean> deleteVideo(
            @RequestParam Long id,
            @RequestHeader("Authorization") String token) {
        return Result.success(contentService.deleteVideo(id, token.substring(7)));
    }
}