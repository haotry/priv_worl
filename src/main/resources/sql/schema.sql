-- Create database
CREATE DATABASE IF NOT EXISTS hltj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hltj;

-- User table
CREATE TABLE IF NOT EXISTS hl_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jwcode VARCHAR(20) NOT NULL COMMENT 'User ID code',
    name VARCHAR(50) NOT NULL COMMENT 'Username',
    tel VARCHAR(20) COMMENT 'Phone number',
    avatar VARCHAR(255) COMMENT 'Avatar URL',
    gender VARCHAR(10) COMMENT 'Gender',
    password VARCHAR(255) NOT NULL COMMENT 'Password (encrypted)',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    credit INT DEFAULT 0 COMMENT 'User credit points',
    user_identity INT DEFAULT 0 COMMENT 'User identity (0: regular user, 1: teacher)',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    UNIQUE KEY idx_jwcode (jwcode),
    UNIQUE KEY idx_tel (tel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

-- Article table
CREATE TABLE IF NOT EXISTS hl_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jwcode VARCHAR(20) NOT NULL COMMENT 'Author jwcode',
    title VARCHAR(255) NOT NULL COMMENT 'Article title',
    content TEXT NOT NULL COMMENT 'Article content',
    image VARCHAR(255) COMMENT 'Article image URL',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    like_count INT DEFAULT 0 COMMENT 'Like count',
    comment_count INT DEFAULT 0 COMMENT 'Comment count',
    collect_count INT DEFAULT 0 COMMENT 'Collection count',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    KEY idx_jwcode (jwcode),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Article table';

-- Video table
CREATE TABLE IF NOT EXISTS hl_video (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jwcode VARCHAR(20) NOT NULL COMMENT 'Author jwcode',
    title VARCHAR(255) NOT NULL COMMENT 'Video title',
    content TEXT COMMENT 'Video description',
    cover VARCHAR(255) COMMENT 'Video cover image URL',
    video VARCHAR(255) NOT NULL COMMENT 'Video URL',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    like_count INT DEFAULT 0 COMMENT 'Like count',
    comment_count INT DEFAULT 0 COMMENT 'Comment count',
    collect_count INT DEFAULT 0 COMMENT 'Collection count',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    KEY idx_jwcode (jwcode),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Video table';

-- Moment table
CREATE TABLE IF NOT EXISTS hl_moment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jwcode VARCHAR(20) NOT NULL COMMENT 'Author jwcode',
    content TEXT NOT NULL COMMENT 'Moment content',
    images TEXT COMMENT 'Image URLs (JSON array)',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    like_count INT DEFAULT 0 COMMENT 'Like count',
    comment_count INT DEFAULT 0 COMMENT 'Comment count',
    collect_count INT DEFAULT 0 COMMENT 'Collection count',
    type INT DEFAULT 3 COMMENT 'Content type (3: moment)',
    author VARCHAR(50) COMMENT 'Author name',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    KEY idx_jwcode (jwcode),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Moment table';

-- Comment table
CREATE TABLE IF NOT EXISTS hl_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content_id BIGINT NOT NULL COMMENT 'ID of the content being commented on',
    jwcode VARCHAR(20) NOT NULL COMMENT 'Commenter jwcode',
    content TEXT NOT NULL COMMENT 'Comment content',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    reply_id BIGINT DEFAULT 0 COMMENT 'ID of the comment this is replying to (0 if root comment)',
    reply_sub_id BIGINT DEFAULT 0 COMMENT 'ID of the sub-comment this is replying to (0 if replying to root)',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    KEY idx_content_id (content_id),
    KEY idx_jwcode (jwcode),
    KEY idx_reply_id (reply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Comment table';

-- Follow table
CREATE TABLE IF NOT EXISTS hl_follow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    follower_jwcode VARCHAR(20) NOT NULL COMMENT 'Follower jwcode',
    followed_jwcode VARCHAR(20) NOT NULL COMMENT 'Followed jwcode',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    UNIQUE KEY idx_follower_followed (follower_jwcode, followed_jwcode),
    KEY idx_follower (follower_jwcode),
    KEY idx_followed (followed_jwcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Follow table';

-- Like table
CREATE TABLE IF NOT EXISTS hl_like (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content_id BIGINT NOT NULL COMMENT 'ID of the content being liked',
    content_type INT NOT NULL COMMENT 'Type of content (1: article, 2: video, 3: moment)',
    jwcode VARCHAR(20) NOT NULL COMMENT 'User jwcode',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    UNIQUE KEY idx_content_user (content_id, content_type, jwcode),
    KEY idx_jwcode (jwcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Like table';

-- Collect table
CREATE TABLE IF NOT EXISTS hl_collect (
    id BIGINT NOT NULL AUTO_INCREMENT,
    content_id BIGINT NOT NULL COMMENT 'ID of the content being collected',
    content_type INT NOT NULL COMMENT 'Type of content (1: article, 2: video, 3: moment)',
    jwcode VARCHAR(20) NOT NULL COMMENT 'User jwcode',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    UNIQUE KEY idx_content_user (content_id, content_type, jwcode),
    KEY idx_jwcode (jwcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Collect table';

-- Lesson Menu table
CREATE TABLE IF NOT EXISTS hl_lesson_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT 'Menu name',
    image VARCHAR(255) COMMENT 'Menu image',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lesson menu table';

-- Lesson table
CREATE TABLE IF NOT EXISTS hl_lesson (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL COMMENT 'Lesson title',
    cover VARCHAR(255) COMMENT 'Lesson cover image',
    author VARCHAR(50) NOT NULL COMMENT 'Author name',
    avatar VARCHAR(255) COMMENT 'Author avatar',
    content TEXT COMMENT 'Lesson content',
    intro TEXT COMMENT 'Author introduction',
    purchase_notes TEXT COMMENT 'Purchase notes',
    video VARCHAR(255) COMMENT 'Lesson video URL',
    image VARCHAR(255) COMMENT 'Lesson image URL',
    price INT NOT NULL DEFAULT 0 COMMENT 'Price (in credits)',
    participant_count INT NOT NULL DEFAULT 0 COMMENT 'Number of participants',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    menu_id BIGINT COMMENT 'Category ID',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    KEY idx_menu_id (menu_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lesson table';

-- Order table
CREATE TABLE IF NOT EXISTS hl_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jwcode VARCHAR(20) NOT NULL COMMENT 'User jwcode',
    lesson_id BIGINT NOT NULL COMMENT 'Lesson ID',
    lesson_name VARCHAR(255) NOT NULL COMMENT 'Lesson name',
    lesson_image VARCHAR(255) COMMENT 'Lesson image',
    price INT NOT NULL COMMENT 'Price paid (in credits)',
    create_time DATETIME NOT NULL COMMENT 'Creation time',
    deleted TINYINT DEFAULT 0 COMMENT 'Logical delete flag',
    PRIMARY KEY (id),
    UNIQUE KEY idx_jwcode_lesson (jwcode, lesson_id),
    KEY idx_jwcode (jwcode),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order table';

-- Insert sample data
INSERT INTO hl_user (jwcode, name, tel, avatar, gender, password, create_time, credit, user_identity) VALUES
('11112222', 'hua', '15962348563', '', '女', '$2a$10$7a7UUPQxpYfzEUB4XmRm8u9VxwJYwQjhDpclNQpXZ8G2pBf1m6m2G', '2022-07-01 20:44:00', 666, 0);