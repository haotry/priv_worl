package com.hltj.api.service;

import com.hltj.api.model.Lesson;
import com.hltj.api.model.LessonMenu;
import com.hltj.api.model.Order;
import com.hltj.api.dto.PageResult;

import java.util.List;

public interface LessonService {

    List<LessonMenu> getLessonBar(String token);

    PageResult<Lesson> getLessonRecommend(Integer page, Integer pageSize, Integer type, String token);

    List<LessonMenu> getLessonMenu(String token);

    PageResult<Lesson> getLessonMenuList(Long menuId, Integer page, Integer pageSize, String token);

    Lesson getLessonDetail(Long id, String jwcode, String token);

    Boolean buyLesson(String jwcode, Long id, Integer credit);

    /**
     * 获取课程列表，支持按分类筛选
     * @param menuId 分类ID，如不传或传0则查询所有
     * @param page 页码
     * @param pageSize 每页条数
     * @param token 授权token
     * @return 分页课程列表
     */
    PageResult<Lesson> getLessonList(Long menuId, Integer page, Integer pageSize, String token);

    /**
     * 获取当前用户购买的课程列表
     * @param page 页码
     * @param pageSize 每页条数
     * @param token 授权token
     * @return 分页的我的课程列表
     */
    PageResult<Order> getMyLessons(Integer page, Integer pageSize, String token);

    /**
     * 添加新课程
     * @param lesson 课程信息
     * @param token 授权token
     * @return 新课程ID
     */
    Long addLesson(Lesson lesson, String token);
}