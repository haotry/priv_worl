package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT a.* FROM hl_article a WHERE a.jwcode IN " +
            "(SELECT followed_jwcode FROM hl_follow WHERE follower_jwcode = #{jwcode}) " +
            "ORDER BY a.create_time DESC")
    IPage<Article> findFollowingArticles(Page<Article> page, @Param("jwcode") String jwcode);

    @Update("UPDATE hl_article SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(@Param("id") Long id);

    @Update("UPDATE hl_article SET like_count = like_count - 1 WHERE id = #{id}")
    void decreaseLikeCount(@Param("id") Long id);

    @Update("UPDATE hl_article SET comment_count = comment_count + 1 WHERE id = #{id}")
    void increaseCommentCount(@Param("id") Long id);

    @Update("UPDATE hl_article SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    void decreaseCommentCount(@Param("id") Long id);

    @Update("UPDATE hl_article SET collect_count = collect_count + 1 WHERE id = #{id}")
    void increaseCollectCount(@Param("id") Long id);

    @Update("UPDATE hl_article SET collect_count = collect_count - 1 WHERE id = #{id}")
    void decreaseCollectCount(@Param("id") Long id);
}