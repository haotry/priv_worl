package com.hltj.api.service;

import com.hltj.api.dto.CommentDTO;
import com.hltj.api.dto.PageResult;

import java.util.List;

public interface CommentService {

    PageResult<CommentDTO> getCommentList(Long contentId, Integer page, Integer pageSize, String token);

    Long reply(Long contentId, String content, Long replyId, Long replySubId, String token);

    List<CommentDTO> getReplyLists(Long id, Integer page, Integer pageSize, String token);

    /**
     * 删除评论
     * @param id 评论ID
     * @param token JWT令牌
     * @return 删除结果
     */
    Boolean deleteComment(Long id, String token);

    /**
     * 获取当前用户的所有评论
     * @param page 页码
     * @param pageSize 每页数量
     * @param token JWT令牌
     * @return 评论列表
     */
    PageResult<CommentDTO> getMyComments(Integer page, Integer pageSize, String token);
}