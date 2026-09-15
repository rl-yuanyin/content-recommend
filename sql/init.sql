-- Image recommendation database initialization.
-- MySQL 8.0, utf8mb4.

CREATE DATABASE IF NOT EXISTS content_recommend
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE content_recommend;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Recreate all tables so this script can be executed repeatedly.
DROP TABLE IF EXISTS image_like;
DROP TABLE IF EXISTS image_collect;
DROP TABLE IF EXISTS image_comment;
DROP TABLE IF EXISTS user_behavior;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS recommend_result;
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS sys_user;

-- Remove legacy article tables when upgrading an existing database.
DROP TABLE IF EXISTS article_tag;
DROP TABLE IF EXISTS article_like;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS `comment`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片分类表';

CREATE TABLE image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT '图片标题',
    description VARCHAR(500) COMMENT '描述',
    url VARCHAR(500) NOT NULL COMMENT '图片URL',
    thumbnail_url VARCHAR(500) COMMENT '缩略图URL',
    category_id BIGINT COMMENT '分类ID',
    author_id BIGINT NOT NULL COMMENT '上传者ID',
    author_name VARCHAR(50) COMMENT '上传者名称',
    tags VARCHAR(200) COMMENT '标签，逗号分隔',
    width INT COMMENT '宽度像素',
    height INT COMMENT '高度像素',
    file_size BIGINT COMMENT '文件大小字节',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    collect_count INT DEFAULT 0 COMMENT '收藏数',
    download_count INT DEFAULT 0 COMMENT '下载数',
    feature_extracted TINYINT DEFAULT 0 COMMENT '特征是否已提取 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态 0下架 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_author (author_id),
    INDEX idx_feature (feature_extracted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片元数据表';

CREATE TABLE image_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_image (image_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片评论表';

CREATE TABLE image_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_image_user (image_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片点赞表';

CREATE TABLE image_collect (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_image_user (image_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片收藏表';

CREATE TABLE user_behavior (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    image_id BIGINT NOT NULL COMMENT '图片ID',
    behavior_type TINYINT NOT NULL COMMENT '1浏览 2点赞 3评论 5收藏 6下载',
    duration INT DEFAULT 0 COMMENT '停留时长秒',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_image (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户图片行为表';

CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type TINYINT NOT NULL COMMENT '1点赞 2评论 3收藏 4系统',
    title VARCHAR(100) COMMENT '标题',
    content VARCHAR(500) COMMENT '内容',
    related_id BIGINT COMMENT '关联图片ID',
    is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

CREATE TABLE recommend_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    image_ids TEXT COMMENT '推荐图片ID逗号分隔',
    algorithm_type VARCHAR(20) COMMENT 'image_cf/hot/mixed',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐结果表';

-- Image categories.
INSERT INTO category (id, name, sort, status)
VALUES
    (1, '风景', 1, 1),
    (2, '人物', 2, 1),
    (3, '动物', 3, 1),
    (4, '建筑', 4, 1),
    (5, '美食', 5, 1),
    (6, '科技', 6, 1),
    (7, '艺术', 7, 1),
    (8, '其他', 8, 1);

-- Ten users. All passwords are BCrypt hashes of 123456.
INSERT INTO sys_user (id, username, password, nickname, email, status)
VALUES
    (
        1,
        'admin',
        '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S',
        '管理员',
        'admin@example.com',
        1
    ),
    (
        2,
        'test',
        '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai',
        '测试用户',
        'test@example.com',
        1
    ),
    (
        3,
        'alice',
        '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S',
        'Alice',
        'alice@example.com',
        1
    ),
    (
        4,
        'bob',
        '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai',
        'Bob',
        'bob@example.com',
        1
    ),
    (
        5,
        'carol',
        '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S',
        'Carol',
        'carol@example.com',
        1
    ),
    (
        6,
        'david',
        '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai',
        'David',
        'david@example.com',
        1
    ),
    (
        7,
        'emma',
        '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S',
        'Emma',
        'emma@example.com',
        1
    ),
    (
        8,
        'frank',
        '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai',
        'Frank',
        'frank@example.com',
        1
    ),
    (
        9,
        'grace',
        '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S',
        'Grace',
        'grace@example.com',
        1
    ),
    (
        10,
        'henry',
        '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai',
        'Henry',
        'henry@example.com',
        1
    );

-- Fifty test images from Picsum Photos.
INSERT INTO image (
    id,
    title,
    description,
    url,
    thumbnail_url,
    category_id,
    author_id,
    author_name,
    tags,
    width,
    height,
    file_size,
    feature_extracted,
    status
)
SELECT
    number_value,
    CONCAT('测试图片 ', LPAD(number_value, 2, '0')),
    CONCAT('用于图像识别与智能推荐算法测试的第 ', number_value, ' 张图片'),
    CONCAT('https://picsum.photos/800/600?random=', number_value),
    CONCAT('https://picsum.photos/400/300?random=', number_value),
    CASE
        WHEN number_value BETWEEN 1 AND 7 THEN 1
        WHEN number_value BETWEEN 8 AND 13 THEN 2
        WHEN number_value BETWEEN 14 AND 19 THEN 3
        WHEN number_value BETWEEN 20 AND 25 THEN 4
        WHEN number_value BETWEEN 26 AND 31 THEN 5
        WHEN number_value BETWEEN 32 AND 37 THEN 6
        WHEN number_value BETWEEN 38 AND 43 THEN 7
        ELSE 8
    END,
    MOD(number_value - 1, 10) + 1,
    ELT(
        MOD(number_value - 1, 10) + 1,
        'admin',
        'test',
        'alice',
        'bob',
        'carol',
        'david',
        'emma',
        'frank',
        'grace',
        'henry'
    ),
    CASE
        WHEN number_value BETWEEN 1 AND 7
            THEN CONCAT('风景,自然,旅行,图片', number_value)
        WHEN number_value BETWEEN 8 AND 13
            THEN CONCAT('人物,肖像,生活,图片', number_value)
        WHEN number_value BETWEEN 14 AND 19
            THEN CONCAT('动物,自然,宠物,图片', number_value)
        WHEN number_value BETWEEN 20 AND 25
            THEN CONCAT('建筑,城市,空间,图片', number_value)
        WHEN number_value BETWEEN 26 AND 31
            THEN CONCAT('美食,餐饮,生活,图片', number_value)
        WHEN number_value BETWEEN 32 AND 37
            THEN CONCAT('科技,数码,未来,图片', number_value)
        WHEN number_value BETWEEN 38 AND 43
            THEN CONCAT('艺术,设计,创意,图片', number_value)
        ELSE CONCAT('其他,综合,图片', number_value)
    END,
    800,
    600,
    200000 + number_value * 4096,
    0,
    1
FROM (
    SELECT 1 AS number_value UNION ALL
    SELECT 2 UNION ALL
    SELECT 3 UNION ALL
    SELECT 4 UNION ALL
    SELECT 5 UNION ALL
    SELECT 6 UNION ALL
    SELECT 7 UNION ALL
    SELECT 8 UNION ALL
    SELECT 9 UNION ALL
    SELECT 10 UNION ALL
    SELECT 11 UNION ALL
    SELECT 12 UNION ALL
    SELECT 13 UNION ALL
    SELECT 14 UNION ALL
    SELECT 15 UNION ALL
    SELECT 16 UNION ALL
    SELECT 17 UNION ALL
    SELECT 18 UNION ALL
    SELECT 19 UNION ALL
    SELECT 20 UNION ALL
    SELECT 21 UNION ALL
    SELECT 22 UNION ALL
    SELECT 23 UNION ALL
    SELECT 24 UNION ALL
    SELECT 25 UNION ALL
    SELECT 26 UNION ALL
    SELECT 27 UNION ALL
    SELECT 28 UNION ALL
    SELECT 29 UNION ALL
    SELECT 30 UNION ALL
    SELECT 31 UNION ALL
    SELECT 32 UNION ALL
    SELECT 33 UNION ALL
    SELECT 34 UNION ALL
    SELECT 35 UNION ALL
    SELECT 36 UNION ALL
    SELECT 37 UNION ALL
    SELECT 38 UNION ALL
    SELECT 39 UNION ALL
    SELECT 40 UNION ALL
    SELECT 41 UNION ALL
    SELECT 42 UNION ALL
    SELECT 43 UNION ALL
    SELECT 44 UNION ALL
    SELECT 45 UNION ALL
    SELECT 46 UNION ALL
    SELECT 47 UNION ALL
    SELECT 48 UNION ALL
    SELECT 49 UNION ALL
    SELECT 50
) AS number_sequence;
