package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.* FROM hl_comment c WHERE c.content_id = #{contentId} AND c.reply_id = 0 ORDER BY c.create_time DESC")
    IPage<Comment> findCommentsByContentId(Page<Comment> page, @Param("contentId") Long contentId);

    @Select("SELECT c.* FROM hl_comment c WHERE c.reply_id = #{replyId} ORDER BY c.create_time")
    List<Comment> findRepliesByCommentId(@Param("replyId") Long replyId);
}