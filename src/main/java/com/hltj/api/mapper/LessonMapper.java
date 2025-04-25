package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.model.Lesson;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface LessonMapper extends BaseMapper<Lesson> {

    @Select("SELECT * FROM hl_lesson WHERE menu_id = #{menuId} ORDER BY create_time DESC")
    IPage<Lesson> findByMenuId(Page<Lesson> page, @Param("menuId") Long menuId);

    @Select("SELECT * FROM hl_lesson ORDER BY participant_count DESC LIMIT 10")
    List<Lesson> findRecommendedLessons();

    @Update("UPDATE hl_lesson SET participant_count = participant_count + 1 WHERE id = #{id}")
    void increaseJoinCount(@Param("id") Long id);
}