package com.hltj.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hltj.api.common.JwtTokenUtil;
import com.hltj.api.dto.PageResult;
import com.hltj.api.mapper.LessonMapper;
import com.hltj.api.mapper.LessonMenuMapper;
import com.hltj.api.mapper.OrderMapper;
import com.hltj.api.mapper.UserMapper;
import com.hltj.api.model.Lesson;
import com.hltj.api.model.LessonMenu;
import com.hltj.api.model.Order;
import com.hltj.api.model.User;
import com.hltj.api.service.LessonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonMapper lessonMapper;

    @Autowired
    private LessonMenuMapper lessonMenuMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public List<LessonMenu> getLessonBar(String token) {
        // Just return all lesson menus
        return lessonMenuMapper.selectList(null);
    }

    @Override
    public PageResult<Lesson> getLessonRecommend(Integer page, Integer pageSize, Integer type, String token) {
        Page<Lesson> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<Lesson> wrapper = new LambdaQueryWrapper<>();

        // Filter by type if provided
        if (type != null && type > 0) {
            wrapper.eq(Lesson::getMenuId, type);
        }

        // Sort by participant count
        wrapper.orderByDesc(Lesson::getParticipantCount);

        Page<Lesson> lessons = lessonMapper.selectPage(pageParam, wrapper);

        return PageResult.of(lessons.getTotal(), lessons.getRecords());
    }

    @Override
    public List<LessonMenu> getLessonMenu(String token) {
        // Just return all lesson menus
        return lessonMenuMapper.selectList(null);
    }

    @Override
    public PageResult<Lesson> getLessonMenuList(Long menuId, Integer page, Integer pageSize, String token) {
        Page<Lesson> pageParam = new Page<>(page, pageSize);

        // Get lessons by menu ID
        IPage<Lesson> lessons = lessonMapper.findByMenuId(pageParam, menuId);

        return PageResult.of(lessons.getTotal(), lessons.getRecords());
    }

    @Override
    public Lesson getLessonDetail(Long id, String jwcode, String token) {
        Lesson lesson = lessonMapper.selectById(id);
        if (lesson == null) {
            return null;
        }

        return lesson;
    }

    @Override
    @Transactional
    public Boolean buyLesson(String jwcode, Long id, Integer credit) {
        // Check if user already bought this lesson
        Integer orderCount = orderMapper.checkUserOrderStatus(jwcode, id);
        if (orderCount > 0) {
            return true; // Already bought
        }

        // Get lesson
        Lesson lesson = lessonMapper.selectById(id);
        if (lesson == null) {
            return false;
        }

        // Check if user has enough credit
        User user = userMapper.findByJwcode(jwcode);
        if (user == null || user.getCredit() < lesson.getPrice()) {
            return false;
        }

        // Deduct credit
        userMapper.subtractCredit(jwcode, lesson.getPrice());

        // Create order
        Order order = new Order();
        order.setJwcode(jwcode);
        order.setLessonId(id);
        order.setLessonName(lesson.getTitle());
        order.setLessonImage(lesson.getImage());
        order.setPrice(lesson.getPrice());
        order.setCreateTime(new Date());

        orderMapper.insert(order);

        // Increase participant count
        lessonMapper.increaseJoinCount(id);

        return true;
    }

    @Override
    public PageResult<Lesson> getLessonList(Long menuId, Integer page, Integer pageSize, String token) {
        // 获取当前用户
        String jwcode = jwtTokenUtil.extractUsername(token);
        User user = userMapper.findByJwcode(jwcode);

        // 检查用户权限，教师和管理员可以查看所有课程
        boolean isTeacherOrAdmin = user != null && (user.getUserIdentity() == 0 || user.getUserIdentity() == 2);

        Page<Lesson> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Lesson> wrapper = new LambdaQueryWrapper<>();

        // 如果指定了分类ID且不为0，则按分类筛选
        if (menuId != null && menuId > 0) {
            wrapper.eq(Lesson::getMenuId, menuId);
        }

        // 如果是普通用户，只能看到上架的课程
//        if (!isTeacherOrAdmin) {
//            wrapper.eq(Lesson::getDeleted, 0); // 未删除的课程
//            // 假设status字段表示课程状态: 1=已上架, 0=待审核, -1=已拒绝
//            wrapper.eq(Lesson::getStatus, 1); // 已上架的课程
//        }

        // 排序：按参与人数降序
        wrapper.orderByDesc(Lesson::getParticipantCount);

        Page<Lesson> lessons = lessonMapper.selectPage(pageParam, wrapper);

        return PageResult.of(lessons.getTotal(), lessons.getRecords());
    }

    @Override
    public PageResult<Order> getMyLessons(Integer page, Integer pageSize, String token) {
        // 获取当前用户
        String jwcode = jwtTokenUtil.extractUsername(token);

        Page<Order> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 查询当前用户的订单
        wrapper.eq(Order::getJwcode, jwcode)
               .orderByDesc(Order::getCreateTime);

        Page<Order> orders = orderMapper.selectPage(pageParam, wrapper);

        return PageResult.of(orders.getTotal(), orders.getRecords());
    }

    @Override
    @Transactional
    public Long addLesson(Lesson lesson, String token) {
        // 获取当前用户
        String jwcode = jwtTokenUtil.extractUsername(token);
        User user = userMapper.findByJwcode(jwcode);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户是否有权发布课程（教师或管理员）
        if (user.getUserIdentity() != 0 && user.getUserIdentity() != 2) {
            throw new RuntimeException("无权发布课程，只有教师或管理员可以发布课程");
        }

        // 确保课程菜单存在
        if (lesson.getMenuId() != null && lesson.getMenuId() > 0) {
            LessonMenu menu = lessonMenuMapper.selectById(lesson.getMenuId());
            if (menu == null) {
                throw new RuntimeException("所选课程分类不存在");
            }
        }

        // 补充课程信息
        lesson.setParticipantCount(0);  // 初始参与人数为0
        lesson.setCreateTime(new Date());  // 设置创建时间

        // 保存课程
        lessonMapper.insert(lesson);

        return lesson.getId();
    }
}