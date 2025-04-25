package com.hltj.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.common.JwtTokenUtil;
import com.hltj.api.dto.CommentDTO;
import com.hltj.api.dto.PageResult;
import com.hltj.api.mapper.ArticleMapper;
import com.hltj.api.mapper.CommentMapper;
import com.hltj.api.mapper.MomentMapper;
import com.hltj.api.mapper.UserMapper;
import com.hltj.api.mapper.VideoMapper;
import com.hltj.api.model.Comment;
import com.hltj.api.model.User;
import com.hltj.api.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private MomentMapper momentMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public PageResult<CommentDTO> getCommentList(Long contentId, Integer page, Integer pageSize, String token) {
        Page<Comment> commentPage = new Page<>(page, pageSize);
        IPage<Comment> commentsResult = commentMapper.findCommentsByContentId(commentPage, contentId);

        List<CommentDTO> commentDTOs = commentsResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(commentsResult.getTotal(), commentDTOs);
    }

    @Override
    @Transactional
    public Long reply(Long contentId, String content, Long replyId, Long replySubId, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        Comment comment = new Comment();
        comment.setContentId(contentId);
        comment.setJwcode(jwcode);
        comment.setContent(content);
        comment.setCreateTime(new Date());

        // If replyId is provided, set it (replying to a comment)
        if (replyId != null) {
            comment.setReplyId(replyId);
        } else {
            comment.setReplyId(0L); // Root comment
        }

        // If replySubId is provided, set it (replying to a reply)
        if (replySubId != null) {
            comment.setReplySubId(replySubId);
        } else {
            comment.setReplySubId(0L); // Replying to root comment
        }

        commentMapper.insert(comment);

        // Increase the comment count of the content
        increaseCommentCount(contentId);

        return comment.getId();
    }

    @Override
    public List<CommentDTO> getReplyLists(Long id, Integer page, Integer pageSize, String token) {
        List<Comment> replies = commentMapper.findRepliesByCommentId(id);

        return replies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean deleteComment(Long id, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // 查询评论
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return false;
        }

        // 验证权限（只有评论作者才能删除）
        if (!comment.getJwcode().equals(jwcode)) {
            return false;
        }

        // 获取内容ID
        Long contentId = comment.getContentId();

        // 删除评论及其回复
        // 1. 删除当前评论
        commentMapper.deleteById(id);

        // 2. 删除对该评论的所有回复
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getReplyId, id);
        commentMapper.delete(wrapper);

        // 减少内容的评论计数
        decreaseCommentCount(contentId);

        return true;
    }

    @Override
    public PageResult<CommentDTO> getMyComments(Integer page, Integer pageSize, String token) {
        String jwcode = jwtTokenUtil.extractUsername(token);

        // 分页查询当前用户的所有评论
        Page<Comment> commentPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getJwcode, jwcode)
               .orderByDesc(Comment::getCreateTime);

        Page<Comment> comments = commentMapper.selectPage(commentPage, wrapper);

        // 转换为DTO
        List<CommentDTO> commentDTOs = comments.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 增强评论信息 - 添加内容标题和作者
        commentDTOs.forEach(this::enrichCommentWithContentInfo);

        return PageResult.of(comments.getTotal(), commentDTOs);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // Format the time
        dto.setTime(comment.getCreateTime().toString());

        // Get user details
        User user = userMapper.findByJwcode(comment.getJwcode());
        if (user != null) {
            dto.setName(user.getName());
            dto.setAvatar(user.getAvatar());
            dto.setUserIdentity(user.getUserIdentity());
        }

        return dto;
    }

    private void enrichCommentWithContentInfo(CommentDTO dto) {
        Long contentId = dto.getContentId();

        System.out.println("正在处理评论ID: " + dto.getId() + ", contentId: " + contentId);

        // 尝试查询文章
        if (articleMapper.selectById(contentId) != null) {
            System.out.println("找到了文章信息");
            dto.setContentType(1); // 设置为文章类型
            dto.setContentTitle(articleMapper.selectById(contentId).getTitle());
            // 获取作者信息
            String authorJwcode = articleMapper.selectById(contentId).getJwcode();
            User author = userMapper.findByJwcode(authorJwcode);
            if (author != null) {
                dto.setAuthor(author.getName());
                System.out.println("设置文章作者: " + author.getName());
            } else {
                System.out.println("未找到文章作者信息, jwcode: " + authorJwcode);
            }
            return;
        }

        // 尝试查询视频
        if (videoMapper.selectById(contentId) != null) {
            System.out.println("找到了视频信息");
            dto.setContentType(2); // 设置为视频类型
            dto.setContentTitle(videoMapper.selectById(contentId).getTitle());
            // 获取作者信息
            String authorJwcode = videoMapper.selectById(contentId).getJwcode();
            User author = userMapper.findByJwcode(authorJwcode);
            if (author != null) {
                dto.setAuthor(author.getName());
                System.out.println("设置视频作者: " + author.getName());
            } else {
                System.out.println("未找到视频作者信息, jwcode: " + authorJwcode);
            }
            return;
        }

        // 尝试查询动态
        if (momentMapper.selectById(contentId) != null) {
            System.out.println("找到了动态信息");
            dto.setContentType(3); // 设置为动态类型
            dto.setContentTitle("动态"); // 动态没有标题，设置为固定值
            // 获取作者信息
            String authorJwcode = momentMapper.selectById(contentId).getJwcode();
            User author = userMapper.findByJwcode(authorJwcode);
            if (author != null) {
                dto.setAuthor(author.getName());
                System.out.println("设置动态作者: " + author.getName());
            } else {
                System.out.println("未找到动态作者信息, jwcode: " + authorJwcode);
            }
        } else {
            System.out.println("未找到内容ID为 " + contentId + " 的任何内容");
            // 设置默认值，以便前端能够显示
            dto.setContentType(0); // 0表示未知类型
            dto.setContentTitle("未知内容");
            dto.setAuthor("未知作者");
        }
    }

    private void increaseCommentCount(Long contentId) {
        // Check which type of content this is and increment the comment count
        // This is a simplified approach - in a real app you'd need to determine content type

        // Try to increment comment count for each content type
        try {
            articleMapper.increaseCommentCount(contentId);
        } catch (Exception ignored) {}

        try {
            videoMapper.increaseCommentCount(contentId);
        } catch (Exception ignored) {}

        try {
            momentMapper.increaseCommentCount(contentId);
        } catch (Exception ignored) {}
    }

    private void decreaseCommentCount(Long contentId) {
        // 尝试减少各类内容的评论计数
        try {
            articleMapper.decreaseCommentCount(contentId);
        } catch (Exception ignored) {}

        try {
            videoMapper.decreaseCommentCount(contentId);
        } catch (Exception ignored) {}

        try {
            momentMapper.decreaseCommentCount(contentId);
        } catch (Exception ignored) {}
    }
}