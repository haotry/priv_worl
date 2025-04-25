package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Moment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface MomentMapper extends BaseMapper<Moment> {

    @Select("SELECT m.* FROM hl_moment m WHERE m.jwcode IN " +
            "(SELECT followed_jwcode FROM hl_follow WHERE follower_jwcode = #{jwcode}) " +
            "ORDER BY m.create_time DESC")
    IPage<Moment> findFollowingMoments(Page<Moment> page, @Param("jwcode") String jwcode);

    @Update("UPDATE hl_moment SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(@Param("id") Long id);

    @Update("UPDATE hl_moment SET like_count = like_count - 1 WHERE id = #{id}")
    void decreaseLikeCount(@Param("id") Long id);

    @Update("UPDATE hl_moment SET comment_count = comment_count + 1 WHERE id = #{id}")
    void increaseCommentCount(@Param("id") Long id);

    @Update("UPDATE hl_moment SET collect_count = collect_count + 1 WHERE id = #{id}")
    void increaseCollectCount(@Param("id") Long id);

    @Update("UPDATE hl_moment SET collect_count = collect_count - 1 WHERE id = #{id}")
    void decreaseCollectCount(@Param("id") Long id);

    @Update("UPDATE hl_moment SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{id}")
    void decreaseCommentCount(@Param("id") Long id);
}