package com.hltj.api.controller;

import com.hltj.api.common.Result;
import com.hltj.api.dto.CommentDTO;
import com.hltj.api.dto.PageResult;
import com.hltj.api.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hltj")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/comment/list")
    public Result<PageResult<CommentDTO>> getCommentList(
            @RequestParam Long id,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(commentService.getCommentList(id, page, pageSize, token.substring(7)));
    }

    @PostMapping("/reply")
    public Result<Long> reply(
            @RequestParam(required = false) Long replyId,
            @RequestParam Long id,
            @RequestParam String content,
            @RequestParam(required = false) Long replySubId,
            @RequestHeader("Authorization") String token) {
        return Result.success(commentService.reply(id, content, replyId, replySubId, token.substring(7)));
    }

    @GetMapping("/reply/lists")
    public Result<List<CommentDTO>> getReplyLists(
            @RequestParam Long id,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(commentService.getReplyLists(id, page, pageSize, token.substring(7)));
    }

    /**
     * 删除评论
     *
     * @param id    评论ID
     * @param token JWT令牌
     * @return 删除结果
     */
    @PostMapping("/comment/delete")
    public Result<Boolean> deleteComment(
            @RequestParam Long id,
            @RequestHeader("Authorization") String token) {
        return Result.success(commentService.deleteComment(id, token.substring(7)));
    }

    /**
     * 获取当前用户的所有评论
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param token    JWT令牌
     * @return 评论列表
     */
    @GetMapping("/comment/my")
    public Result<PageResult<CommentDTO>> getMyComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        return Result.success(commentService.getMyComments(page, pageSize, token.substring(7)));
    }
}