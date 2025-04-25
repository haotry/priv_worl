package com.hltj.api.controller;

import com.hltj.api.common.Result;
import com.hltj.api.dto.PageResult;
import com.hltj.api.model.Lesson;
import com.hltj.api.model.LessonMenu;
import com.hltj.api.model.Order;
import com.hltj.api.service.LessonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hltj/lesson")
public class LessonController {

    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

    @Autowired
    private LessonService lessonService;

    /**
     * 获取课程列表，支持按分类筛选
     * @param menuId 分类ID，如不传或传0则查询所有
     * @param page 页码
     * @param pageSize 每页条数
     * @param token 授权token
     * @return 分页课程列表
     */
    @GetMapping("/list")
    public Result<PageResult<Lesson>> getLessonList(
            @RequestParam(required = false) Long menuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getLessonList(menuId, page, pageSize, token.substring(7)));
    }

    /**
     * 获取当前用户购买的课程列表
     * @param page 页码
     * @param pageSize 每页条数
     * @param token 授权token
     * @return 分页的我的课程列表
     */
    @GetMapping("/my")
    public Result<PageResult<Order>> getMyLessons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getMyLessons(page, pageSize, token.substring(7)));
    }

    @GetMapping("/bar")
    public Result<List<LessonMenu>> getLessonBar(@RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getLessonBar(token.substring(7)));
    }

    @GetMapping("/recommend")
    public Result<PageResult<Lesson>> getLessonRecommend(
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam Integer type,
            @RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getLessonRecommend(page, pageSize, type, token.substring(7)));
    }

    @GetMapping("/menu")
    public Result<List<LessonMenu>> getLessonMenu(@RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getLessonMenu(token.substring(7)));
    }

    @GetMapping("/menu/list")
    public Result<PageResult<Lesson>> getLessonMenuList(
            @RequestParam Long menuId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getLessonMenuList(menuId, page, pageSize, token.substring(7)));
    }

    @GetMapping("/detail")
    public Result<Lesson> getLessonDetail(
            @RequestParam Long id,
            @RequestParam String jwcode,
            @RequestHeader("Authorization") String token) {
        return Result.success(lessonService.getLessonDetail(id, jwcode, token.substring(7)));
    }

    @PostMapping("/buy")
    public Result<Boolean> buyLesson(
            @RequestParam String jwcode,
            @RequestParam Long id,
            @RequestParam Integer credit) {
        return Result.success(lessonService.buyLesson(jwcode, id, credit));
    }

    /**
     * 添加新课程
     * @param lesson 课程信息
     * @param token JWT令牌
     * @return 新课程ID
     */
    @PostMapping("/add")
    public Result<Long> addLesson(
            @RequestBody Lesson lesson,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("接收到添加课程请求: {}", lesson.getTitle());
            Long lessonId = lessonService.addLesson(lesson, token.substring(7));
            logger.info("课程添加成功, ID: {}", lessonId);
            logger.info("课程添加成功, ID: {}", lesson);
            return Result.success(lessonId);
        } catch (Exception e) {
            logger.error("课程添加失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}