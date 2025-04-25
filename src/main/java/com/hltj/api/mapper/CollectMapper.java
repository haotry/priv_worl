package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Collect;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CollectMapper extends BaseMapper<Collect> {

    @Select("SELECT COUNT(*) FROM hl_collect WHERE content_id = #{contentId} AND content_type = #{contentType} AND jwcode = #{jwcode}")
    Integer checkUserCollectStatus(@Param("contentId") Long contentId, @Param("contentType") Integer contentType, @Param("jwcode") String jwcode);

    @Select("SELECT c.* FROM hl_collect c WHERE c.jwcode = #{jwcode} ORDER BY c.create_time DESC")
    IPage<Collect> findUserCollects(Page<Collect> page, @Param("jwcode") String jwcode);
}