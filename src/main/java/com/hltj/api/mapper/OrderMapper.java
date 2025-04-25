package com.hltj.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hltj.api.model.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COUNT(*) FROM hl_order WHERE jwcode = #{jwcode} AND lesson_id = #{lessonId}")
    Integer checkUserOrderStatus(@Param("jwcode") String jwcode, @Param("lessonId") Long lessonId);
}