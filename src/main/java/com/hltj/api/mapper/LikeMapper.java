package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Like;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LikeMapper extends BaseMapper<Like> {

    @Select("SELECT COUNT(*) FROM hl_like WHERE content_id = #{contentId} AND content_type = #{contentType} AND jwcode = #{jwcode}")
    Integer checkUserLikeStatus(@Param("contentId") Long contentId, @Param("contentType") Integer contentType, @Param("jwcode") String jwcode);

    @Select("SELECT COUNT(*) FROM hl_like WHERE jwcode = #{jwcode}")
    Integer countUserLikes(@Param("jwcode") String jwcode);

    @Select("SELECT l.* FROM hl_like l ORDER BY l.create_time DESC")
    IPage<Like> findUserLikes(Page<Like> page, @Param("jwcode") String jwcode);
}