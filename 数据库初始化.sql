-- 清空表数据（如果需要）
-- DELETE FROM hl_collect;
-- DELETE FROM hl_like;
-- DELETE FROM hl_comment;
-- DELETE FROM hl_moment;
-- DELETE FROM hl_video;
-- DELETE FROM hl_article;
-- DELETE FROM hl_order;
-- DELETE FROM hl_lesson;
-- DELETE FROM hl_lesson_menu;
-- DELETE FROM hl_follow;
-- DELETE FROM hl_user;

-- 插入用户数据
INSERT INTO hl_user (jwcode, name, tel, avatar, gender, password, create_time, credit, user_identity, deleted) VALUES
('11112222', 'hua', '15962348563', 'https://example.com/avatars/hua.jpg', '女', '$2a$10$7a7UUPQxpYfzEUB4XmRm8u9VxwJYwQjhDpclNQpXZ8G2pBf1m6m2G', '2022-07-01 20:44:00', 666, 0, 0),
('33334444', '教师用户', '15912345678', 'https://example.com/avatars/teacher.jpg', '男', '$2a$10$7a7UUPQxpYfzEUB4XmRm8u9VxwJYwQjhDpclNQpXZ8G2pBf1m6m2G', '2022-06-15 10:30:00', 1000, 1, 0),
('55556666', '张三', '13800138001', 'https://example.com/avatars/zhangsan.jpg', '男', '$2a$10$7a7UUPQxpYfzEUB4XmRm8u9VxwJYwQjhDpclNQpXZ8G2pBf1m6m2G', '2022-08-01 14:20:00', 300, 0, 0),
('77778888', '李四', '13800138002', 'https://example.com/avatars/lisi.jpg', '女', '$2a$10$7a7UUPQxpYfzEUB4XmRm8u9VxwJYwQjhDpclNQpXZ8G2pBf1m6m2G', '2022-08-15 09:10:00', 450, 0, 0),
('99990000', '王教师', '13800138003', 'https://example.com/avatars/wangteacher.jpg', '男', '$2a$10$7a7UUPQxpYfzEUB4XmRm8u9VxwJYwQjhDpclNQpXZ8G2pBf1m6m2G', '2022-05-20 16:40:00', 800, 1, 0);

-- 插入关注关系
INSERT INTO hl_follow (follower_jwcode, followed_jwcode, create_time) VALUES
('11112222', '33334444', '2022-09-01 10:00:00'),
('11112222', '99990000', '2022-09-02 11:30:00'),
('55556666', '33334444', '2022-09-03 14:15:00'),
('77778888', '33334444', '2022-09-04 16:45:00'),
('77778888', '99990000', '2022-09-05 09:20:00'),
('55556666', '11112222', '2022-09-06 13:10:00');

-- 插入课程菜单 (移除sort列)
INSERT INTO hl_lesson_menu (name) VALUES
('编程基础'),
('Web开发'),
('移动开发'),
('数据科学'),
('人工智能');

-- 插入课程
INSERT INTO hl_lesson (menu_id, title, image, price, participant_count, create_time, content, deleted, author) VALUES
(1, 'Java编程基础', 'https://example.com/images/java.jpg', 100, 50, '2023-01-01 00:00:00', 'Java是全球排名第一的编程语言，本课程将带你入门Java编程的世界。', 0, '教师用户'),
(1, 'Python基础教程', 'https://example.com/images/python.jpg', 80, 120, '2023-01-05 00:00:00', 'Python是最简单易学的编程语言之一，适合初学者入门。', 0, '教师用户'),
(2, 'HTML/CSS入门', 'https://example.com/images/html.jpg', 60, 200, '2023-01-10 00:00:00', '学习网页开发的基础：HTML和CSS。', 0, '教师用户'),
(2, 'JavaScript高级教程', 'https://example.com/images/javascript.jpg', 120, 80, '2023-01-15 00:00:00', '深入学习JavaScript，掌握前端开发的核心技能。', 0, '教师用户'),
(3, 'Android开发基础', 'https://example.com/images/android.jpg', 150, 60, '2023-01-20 00:00:00', '学习Android开发，打造自己的移动应用。', 0, '王教师'),
(4, '数据分析与可视化', 'https://example.com/images/data.jpg', 200, 40, '2023-01-25 00:00:00', '学习如何分析数据并创建直观的可视化图表。', 0, '王教师'),
(5, '机器学习入门', 'https://example.com/images/ml.jpg', 250, 30, '2023-01-30 00:00:00', '了解机器学习的基本概念和算法。', 0, '王教师');

-- 插入订单
INSERT INTO hl_order (jwcode, lesson_id, lesson_name, lesson_image, price, create_time) VALUES
('11112222', 1, 'Java编程基础', 'https://example.com/images/java.jpg', 100, '2023-02-01 10:30:00'),
('11112222', 3, 'HTML/CSS入门', 'https://example.com/images/html.jpg', 60, '2023-02-05 14:45:00'),
('55556666', 2, 'Python基础教程', 'https://example.com/images/python.jpg', 80, '2023-02-10 09:15:00'),
('77778888', 4, 'JavaScript高级教程', 'https://example.com/images/javascript.jpg', 120, '2023-02-15 16:20:00'),
('55556666', 5, 'Android开发基础', 'https://example.com/images/android.jpg', 150, '2023-02-20 11:30:00');

-- 插入文章
INSERT INTO hl_article (jwcode, title, content, image, create_time, like_count, comment_count, collect_count, author) VALUES
('33334444', 'Java编程思想', '本文介绍Java编程的核心概念和思想方法...', 'https://example.com/articles/java.jpg', '2023-03-01 09:00:00', 15, 5, 3, '教师用户'),
('33334444', 'Web开发最佳实践', '分享Web开发中的一些经验和最佳实践...', 'https://example.com/articles/web.jpg', '2023-03-05 10:30:00', 20, 8, 5, '教师用户'),
('99990000', '人工智能发展趋势', '探讨人工智能的最新发展趋势和未来方向...', 'https://example.com/articles/ai.jpg', '2023-03-10 14:00:00', 25, 10, 7, '王教师'),
('99990000', '移动应用设计原则', '介绍移动应用设计的基本原则和用户体验考虑因素...', 'https://example.com/articles/mobile.jpg', '2023-03-15 15:45:00', 18, 6, 4, '王教师');

-- 插入视频
INSERT INTO hl_video (jwcode, title, content, video, cover, create_time, like_count, comment_count, collect_count, author) VALUES
('33334444', 'Java编程教学', 'Java编程基础知识讲解', 'https://example.com/videos/java.mp4', 'https://example.com/covers/java.jpg', '2023-03-20 10:00:00', 30, 12, 8, '教师用户'),
('33334444', 'HTML速成课', 'HTML快速入门教程', 'https://example.com/videos/html.mp4', 'https://example.com/covers/html.jpg', '2023-03-25 11:30:00', 25, 10, 6, '教师用户'),
('99990000', 'Python数据分析', 'Python数据分析实战教程', 'https://example.com/videos/python.mp4', 'https://example.com/covers/python.jpg', '2023-03-30 14:15:00', 35, 15, 10, '王教师'),
('99990000', '机器学习实战', '机器学习算法讲解与实战', 'https://example.com/videos/ml.mp4', 'https://example.com/covers/ml.jpg', '2023-04-01 16:00:00', 40, 18, 12, '王教师');

-- 插入动态
INSERT INTO hl_moment (jwcode, content, images, create_time, like_count, comment_count, collect_count, type, author) VALUES
('11112222', '今天开始学习Java，感觉很有趣！', 'https://example.com/moments/java1.jpg,https://example.com/moments/java2.jpg', '2023-04-05 09:30:00', 10, 3, 1, 3, 'hua'),
('55556666', '分享一个Python小技巧，希望对大家有帮助', 'https://example.com/moments/python1.jpg', '2023-04-10 11:45:00', 15, 5, 2, 3, '张三'),
('77778888', '前端开发的日常，HTML/CSS真是既简单又复杂', 'https://example.com/moments/web1.jpg,https://example.com/moments/web2.jpg,https://example.com/moments/web3.jpg', '2023-04-15 14:20:00', 12, 4, 1, 3, '李四'),
('11112222', '完成了第一个Java项目，终于理解了面向对象编程', 'https://example.com/moments/java_project.jpg', '2023-04-20 16:30:00', 18, 6, 3, 3, 'hua');

-- 插入评论
INSERT INTO hl_comment (content_id, jwcode, content, reply_id, reply_sub_id, create_time) VALUES
(1, '55556666', '这篇文章写得非常好，学到了很多！', 0, 0, '2023-04-25 10:00:00'),
(1, '77778888', '赞同，文章很有深度', 0, 0, '2023-04-25 10:30:00'),
(1, '11112222', '有些概念讲解得不够清晰', 0, 0, '2023-04-25 11:00:00'),
(1, '33334444', '谢谢支持，后续会更新更多内容', 1, 0, '2023-04-25 11:30:00'),
(1, '33334444', '会注意改进，谢谢反馈', 3, 0, '2023-04-25 12:00:00'),
(2, '11112222', '这些最佳实践很实用', 0, 0, '2023-04-26 09:00:00'),
(3, '55556666', '对AI的未来发展分析很到位', 0, 0, '2023-04-26 10:00:00');

-- 插入点赞记录
INSERT INTO hl_like (content_id, content_type, jwcode, create_time) VALUES
(1, 1, '11112222', '2023-04-27 09:00:00'),  -- 文章1被11112222点赞
(1, 1, '55556666', '2023-04-27 09:30:00'),  -- 文章1被55556666点赞
(2, 1, '11112222', '2023-04-27 10:00:00'),  -- 文章2被11112222点赞
(1, 2, '77778888', '2023-04-27 10:30:00'),  -- 视频1被77778888点赞
(2, 2, '55556666', '2023-04-27 11:00:00'),  -- 视频2被55556666点赞
(1, 3, '33334444', '2023-04-27 11:30:00'),  -- 动态1被33334444点赞
(2, 3, '99990000', '2023-04-27 12:00:00');  -- 动态2被99990000点赞

-- 插入收藏记录
INSERT INTO hl_collect (content_id, content_type, jwcode, create_time) VALUES
(1, 1, '77778888', '2023-04-28 09:00:00'),  -- 文章1被77778888收藏
(3, 1, '11112222', '2023-04-28 09:30:00'),  -- 文章3被11112222收藏
(1, 2, '55556666', '2023-04-28 10:00:00'),  -- 视频1被55556666收藏
(3, 2, '77778888', '2023-04-28 10:30:00'),  -- 视频3被77778888收藏
(1, 3, '99990000', '2023-04-28 11:00:00'),  -- 动态1被99990000收藏
(3, 3, '33334444', '2023-04-28 11:30:00');  -- 动态3被33334444收藏