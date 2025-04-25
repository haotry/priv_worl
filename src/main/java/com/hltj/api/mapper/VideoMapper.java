package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Video;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface VideoMapper extends BaseMapper<Video> {

    @Select("SELECT v.* FROM hl_video v WHERE v.jwcode IN " +
            "(SELECT followed_jwcode FROM hl_follow WHERE follower_jwcode = #{jwcode}) " +
            "ORDER BY v.create_time DESC")
    IPage<Video> findFollowingVideos(Page<Video> page, @Param("jwcode") String jwcode);

    @Update("UPDATE hl_video SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(@Param("id") Long id);

    @Update("UPDATE hl_video SET like_count = like_count - 1 WHERE id = #{id}")
    void decreaseLikeCount(@Param("id") Long id);

    @Update("UPDATE hl_video SET comment_count = comment_count + 1 WHERE id = #{id}")
    void increaseCommentCount(@Param("id") Long id);

    @Update("UPDATE hl_video SET collect_count = collect_count + 1 WHERE id = #{id}")
    void increaseCollectCount(@Param("id") Long id);

    @Update("UPDATE hl_video SET collect_count = collect_count - 1 WHERE id = #{id}")
    void decreaseCollectCount(@Param("id") Long id);

    @Update("UPDATE hl_video SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    void decreaseCommentCount(@Param("id") Long id);
}