-- ============================================================================
-- Content Recommendation Database - Merged Full SQL
-- ============================================================================
-- Combines:
--   1. init.sql                         (tables + users + 50 base images)
--   2. update_image_urls_v3.sql         (Pexels URLs + 4 new categories + 26 new images)
--   3. update_anime_images.sql          (replace 6 anime images with Pollinations AI)
--   4. update_image_51_metadata.sql     (update id=51 metadata)
--
-- Executable in one run on a fresh MySQL 8.0 instance.
-- After running: POST /api/recommend/milvus/init to extract features & build vectors.
-- ============================================================================

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

-- ---------------------------------------------------------------------------
-- Table: sys_user
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- Table: category
-- ---------------------------------------------------------------------------
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片分类表';

-- ---------------------------------------------------------------------------
-- Table: image
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- Table: image_comment
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- Table: image_like
-- ---------------------------------------------------------------------------
CREATE TABLE image_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_image_user (image_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片点赞表';

-- ---------------------------------------------------------------------------
-- Table: image_collect
-- ---------------------------------------------------------------------------
CREATE TABLE image_collect (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    image_id BIGINT NOT NULL COMMENT '图片ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_image_user (image_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片收藏表';

-- ---------------------------------------------------------------------------
-- Table: user_behavior
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- Table: notification
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- Table: recommend_result
-- ---------------------------------------------------------------------------
CREATE TABLE recommend_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    image_ids TEXT COMMENT '推荐图片ID逗号分隔',
    algorithm_type VARCHAR(20) COMMENT 'image_cf/hot/mixed',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐结果表';

-- ===========================================================================
-- Data: Categories (12 total: 8 original + 4 new)
-- ===========================================================================
INSERT INTO category (id, name, sort, status)
VALUES
    (1,  '风景', 1,  1),
    (2,  '人物', 2,  1),
    (3,  '动物', 3,  1),
    (4,  '建筑', 4,  1),
    (5,  '美食', 5,  1),
    (6,  '科技', 6,  1),
    (7,  '艺术', 7,  1),
    (8,  '其他', 8,  1),
    (9,  '游戏', 9,  1),
    (10, '动漫', 10, 1),
    (11, '历史', 11, 1),
    (12, '书籍', 12, 1);

-- ===========================================================================
-- Data: Users (10 total, all passwords are BCrypt hashes of 123456)
-- ===========================================================================
INSERT INTO sys_user (id, username, password, nickname, email, status)
VALUES
    (1,  'admin',  '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S', '管理员',    'admin@example.com',  1),
    (2,  'test',   '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai', '测试用户',  'test@example.com',   1),
    (3,  'alice',  '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S', 'Alice',     'alice@example.com',  1),
    (4,  'bob',    '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai', 'Bob',       'bob@example.com',    1),
    (5,  'carol',  '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S', 'Carol',     'carol@example.com',  1),
    (6,  'david',  '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai', 'David',     'david@example.com',  1),
    (7,  'emma',   '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S', 'Emma',      'emma@example.com',   1),
    (8,  'frank',  '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai', 'Frank',     'frank@example.com',  1),
    (9,  'grace',  '$2a$10$o9lh.D.Zr5UpsZwj9of0O.S.WlZhzJrLc0O1E6BeTHl3ze.wxOo/S', 'Grace',     'grace@example.com',  1),
    (10, 'henry',  '$2a$10$Ktq4vAKagx.Qu76VAIxXM.Eg.6FO7.HZL9x.N2iPSGbu8Bm0eaHai', 'Henry',     'henry@example.com',  1);

-- ===========================================================================
-- Data: Images (77 total) — final merged state
-- ---------------------------------------------------------------------------
-- IDs 1-50: base images from init.sql with corrected metadata & Pexels URLs
--           from update_image_urls_v3.sql (4 URLs replaced: id 10, 16, 19, 25)
-- ID  51:   user upload (明日方舟：终末地宣传图, category_id=9)
--           URL uses Pollinations AI (consistent with anime image approach)
-- IDs 52-77: new images from update_image_urls_v3.sql (Pexels URLs)
-- IDs 58-63: replaced by Pollinations AI anime illustrations
--             (update_anime_images.sql final state)
-- All feature_extracted = 0 (needs Milvus init after)
-- ===========================================================================
INSERT INTO image (
    id, title, description, url, thumbnail_url, category_id,
    author_id, author_name, tags, width, height, file_size,
    view_count, like_count, collect_count, download_count,
    feature_extracted, status
)
VALUES
    -- --- 风景 (category 1) ---
    (1,  '阿尔卑斯山脉风光', '高山与湖泊构成的壮丽自然景观',
        'https://picsum.photos/800/600?random=1', 'https://picsum.photos/400/300?random=1',
        1, 1, 'admin', '风景,山脉,湖泊', 800, 600, 204096, 0, 0, 0, 0, 0, 1),

    (2,  '林间湖泊', '森林与湖水相映的自然风景',
        'https://picsum.photos/800/600?random=2', 'https://picsum.photos/400/300?random=2',
        1, 2, 'test', '风景,森林,湖泊', 800, 600, 208192, 0, 0, 0, 0, 0, 1),

    (3,  '湖畔与长桥', '湖泊、远山和桥梁组成的宁静风景',
        'https://picsum.photos/800/600?random=3', 'https://picsum.photos/400/300?random=3',
        1, 3, 'alice', '风景,湖泊,桥梁', 800, 600, 212288, 0, 0, 0, 0, 0, 1),

    (4,  '山谷湖岸', '开阔山谷中的湖泊与草地',
        'https://picsum.photos/800/600?random=4', 'https://picsum.photos/400/300?random=4',
        1, 4, 'bob', '风景,山谷,湖岸', 800, 600, 216384, 0, 0, 0, 0, 0, 1),

    -- --- 人物 (category 2) ---
    (5,  '风衣人物', '穿着风衣的人物肖像',
        'https://picsum.photos/800/600?random=5', 'https://picsum.photos/400/300?random=5',
        2, 5, 'carol', '人物,服饰,肖像', 800, 600, 220480, 0, 0, 0, 0, 0, 1),

    -- back to 风景
    (6,  '阿尔卑斯雪峰', '雪峰与高山草甸景观',
        'https://picsum.photos/800/600?random=6', 'https://picsum.photos/400/300?random=6',
        1, 6, 'david', '风景,雪山,自然', 800, 600, 224576, 0, 0, 0, 0, 0, 1),

    (7,  '湖畔远山', '湖岸、山谷与远山风景',
        'https://picsum.photos/800/600?random=7', 'https://picsum.photos/400/300?random=7',
        1, 7, 'emma', '风景,湖岸,远山', 800, 600, 228672, 0, 0, 0, 0, 0, 1),

    -- --- 人物 (category 2) ---
    (8,  '商务男士肖像', '衬衫与领带造型的男性肖像',
        'https://picsum.photos/800/600?random=8', 'https://picsum.photos/400/300?random=8',
        2, 8, 'frank', '人物,男性,商务', 800, 600, 232768, 0, 0, 0, 0, 0, 1),

    (9,  '职场人物肖像', '现代职业人物形象',
        'https://picsum.photos/800/600?random=9', 'https://picsum.photos/400/300?random=9',
        2, 9, 'grace', '人物,职场,肖像', 800, 600, 236864, 0, 0, 0, 0, 0, 1),

    -- id=10: URL replaced with Pexels (774909)
    (10, '女性肖像', '自然光下的女性人物肖像',
        'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        2, 10, 'henry', '人物,女性,肖像', 800, 600, 240960, 0, 0, 0, 0, 0, 1),

    (11, '商务男士', '西装与领带造型的男性肖像',
        'https://picsum.photos/800/600?random=11', 'https://picsum.photos/400/300?random=11',
        2, 1, 'admin', '人物,男性,商务', 800, 600, 245056, 0, 0, 0, 0, 0, 1),

    (12, '休闲服饰人物', '穿着休闲服装的人物形象',
        'https://picsum.photos/800/600?random=12', 'https://picsum.photos/400/300?random=12',
        2, 2, 'test', '人物,服饰,生活', 800, 600, 249152, 0, 0, 0, 0, 0, 1),

    (13, '时尚人物', '现代服饰与时尚造型',
        'https://picsum.photos/800/600?random=13', 'https://picsum.photos/400/300?random=13',
        2, 3, 'alice', '人物,时尚,服饰', 800, 600, 253248, 0, 0, 0, 0, 0, 1),

    -- --- 动物 (category 3) ---
    (14, '金毛寻回犬', '草地上的金毛寻回犬',
        'https://picsum.photos/800/600?random=14', 'https://picsum.photos/400/300?random=14',
        3, 4, 'bob', '动物,狗,宠物', 800, 600, 257344, 0, 0, 0, 0, 0, 1),

    (15, '雄狮', '自然状态下的狮子特写',
        'https://picsum.photos/800/600?random=15', 'https://picsum.photos/400/300?random=15',
        3, 5, 'carol', '动物,狮子,野生动物', 800, 600, 261440, 0, 0, 0, 0, 0, 1),

    -- id=16: URL replaced with Pexels (1108099)
    (16, '宠物犬伙伴', '两只可爱的宠物犬',
        'https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        3, 6, 'david', '动物,狗,宠物', 800, 600, 265536, 0, 0, 0, 0, 0, 1),

    (17, '萨摩耶犬', '白色萨摩耶犬的正面特写',
        'https://picsum.photos/800/600?random=17', 'https://picsum.photos/400/300?random=17',
        3, 7, 'emma', '动物,狗,萨摩耶', 800, 600, 269632, 0, 0, 0, 0, 0, 1),

    -- id=18: category changed to 风景
    (18, '湖畔自然风景', '湖岸、石墙和草地构成的自然景观',
        'https://picsum.photos/800/600?random=18', 'https://picsum.photos/400/300?random=18',
        1, 8, 'frank', '风景,湖岸,自然', 800, 600, 273728, 0, 0, 0, 0, 0, 1),

    -- id=19: URL replaced with Pexels (617278)
    (19, '宠物犬肖像', '宠物犬的温暖肖像',
        'https://images.pexels.com/photos/617278/pexels-photo-617278.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/617278/pexels-photo-617278.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        3, 9, 'grace', '动物,狗,宠物', 800, 600, 277824, 0, 0, 0, 0, 0, 1),

    -- --- 建筑 (category 4) ---
    (20, '窗户与空间结构', '建筑窗户与室内光影结构',
        'https://picsum.photos/800/600?random=20', 'https://picsum.photos/400/300?random=20',
        4, 10, 'henry', '建筑,空间,窗户', 800, 600, 281920, 0, 0, 0, 0, 0, 1),

    (21, '建筑与海景', '临海建筑与开阔水域',
        'https://picsum.photos/800/600?random=21', 'https://picsum.photos/400/300?random=21',
        4, 1, 'admin', '建筑,海景,工程', 800, 600, 286016, 0, 0, 0, 0, 0, 1),

    (22, '古典建筑与街景', '具有历史感的建筑与城市空间',
        'https://picsum.photos/800/600?random=22', 'https://picsum.photos/400/300?random=22',
        4, 2, 'test', '建筑,城市,街景', 800, 600, 290112, 0, 0, 0, 0, 0, 1),

    (23, '工程起重机', '大型工程机械设备',
        'https://picsum.photos/800/600?random=23', 'https://picsum.photos/400/300?random=23',
        4, 3, 'alice', '建筑,工程,机械', 800, 600, 294208, 0, 0, 0, 0, 0, 1),

    (24, '湖畔木屋', '临水木屋与自然建筑景观',
        'https://picsum.photos/800/600?random=24', 'https://picsum.photos/400/300?random=24',
        4, 4, 'bob', '建筑,木屋,湖畔', 800, 600, 298304, 0, 0, 0, 0, 0, 1),

    -- id=25: URL replaced with Pexels (325185)
    (25, '现代住宅建筑', '现代建筑立面与居住空间',
        'https://images.pexels.com/photos/325185/pexels-photo-325185.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/325185/pexels-photo-325185.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        4, 5, 'carol', '建筑,住宅,现代', 800, 600, 302400, 0, 0, 0, 0, 0, 1),

    -- --- 美食 (category 5) ---
    (26, '冰淇淋甜点', '精致的冰淇淋甜品',
        'https://picsum.photos/800/600?random=26', 'https://picsum.photos/400/300?random=26',
        5, 6, 'david', '美食,甜点,冰淇淋', 800, 600, 306496, 0, 0, 0, 0, 0, 1),

    (27, '家常馅饼', '蔬菜与肉类制作的家常料理',
        'https://picsum.photos/800/600?random=27', 'https://picsum.photos/400/300?random=27',
        5, 7, 'emma', '美食,料理,馅饼', 800, 600, 310592, 0, 0, 0, 0, 0, 1),

    (28, '芝士汉堡', '夹有芝士和蔬菜的汉堡',
        'https://picsum.photos/800/600?random=28', 'https://picsum.photos/400/300?random=28',
        5, 8, 'frank', '美食,汉堡,快餐', 800, 600, 314688, 0, 0, 0, 0, 0, 1),

    (29, '烤肉料理', '盘中烤肉与配菜',
        'https://picsum.photos/800/600?random=29', 'https://picsum.photos/400/300?random=29',
        5, 9, 'grace', '美食,烤肉,料理', 800, 600, 318784, 0, 0, 0, 0, 0, 1),

    (30, '户外餐厅', '户外餐饮空间与美食场景',
        'https://picsum.photos/800/600?random=30', 'https://picsum.photos/400/300?random=30',
        5, 10, 'henry', '美食,餐厅,户外', 800, 600, 322880, 0, 0, 0, 0, 0, 1),

    (31, '墨西哥卷饼', '包含肉类和蔬菜的卷饼',
        'https://picsum.photos/800/600?random=31', 'https://picsum.photos/400/300?random=31',
        5, 1, 'admin', '美食,卷饼,料理', 800, 600, 326976, 0, 0, 0, 0, 0, 1),

    -- --- 科技 (category 6) ---
    (32, '数字时钟与显示设备', '数字显示与电子设备特写',
        'https://picsum.photos/800/600?random=32', 'https://picsum.photos/400/300?random=32',
        6, 2, 'test', '科技,电子,显示设备', 800, 600, 331072, 0, 0, 0, 0, 0, 1),

    (33, '桌面电脑工作站', '显示器、桌面与电脑设备',
        'https://picsum.photos/800/600?random=33', 'https://picsum.photos/400/300?random=33',
        6, 3, 'alice', '科技,电脑,工作站', 800, 600, 335168, 0, 0, 0, 0, 0, 1),

    (34, '移动设备与网页', '移动终端上的网站与应用界面',
        'https://picsum.photos/800/600?random=34', 'https://picsum.photos/400/300?random=34',
        6, 4, 'bob', '科技,移动设备,互联网', 800, 600, 339264, 0, 0, 0, 0, 0, 1),

    (35, '多屏电脑设备', '电脑显示器与桌面设备',
        'https://picsum.photos/800/600?random=35', 'https://picsum.photos/400/300?random=35',
        6, 5, 'carol', '科技,电脑,显示器', 800, 600, 343360, 0, 0, 0, 0, 0, 1),

    (36, '网络与科学结构', '具有科技感的网络与科学结构',
        'https://picsum.photos/800/600?random=36', 'https://picsum.photos/400/300?random=36',
        6, 6, 'david', '科技,网络,科学', 800, 600, 347456, 0, 0, 0, 0, 0, 1),

    -- id=37: category changed to 建筑
    (37, '宫殿建筑', '具有古典风格的宫殿建筑',
        'https://picsum.photos/800/600?random=37', 'https://picsum.photos/400/300?random=37',
        4, 7, 'emma', '建筑,宫殿,历史', 800, 600, 351552, 0, 0, 0, 0, 0, 1),

    -- --- 艺术 (category 7) ---
    (38, '创意拼贴艺术', '包含绘画工具与拼贴元素的艺术作品',
        'https://picsum.photos/800/600?random=38', 'https://picsum.photos/400/300?random=38',
        7, 8, 'frank', '艺术,拼贴,创意', 800, 600, 355648, 0, 0, 0, 0, 0, 1),

    (39, '花卉艺术摄影', '以雏菊为主题的艺术摄影',
        'https://picsum.photos/800/600?random=39', 'https://picsum.photos/400/300?random=39',
        7, 9, 'grace', '艺术,花卉,摄影', 800, 600, 359744, 0, 0, 0, 0, 0, 1),

    -- id=40: category changed to 动物
    (40, '海洋生物', '海洋生物与水下生态特写',
        'https://picsum.photos/800/600?random=40', 'https://picsum.photos/400/300?random=40',
        3, 10, 'henry', '动物,海洋,生态', 800, 600, 363840, 0, 0, 0, 0, 0, 1),

    (41, '生态艺术摄影', '蜜蜂与花朵构成的生态影像',
        'https://picsum.photos/800/600?random=41', 'https://picsum.photos/400/300?random=41',
        7, 1, 'admin', '艺术,生态,摄影', 800, 600, 367936, 0, 0, 0, 0, 0, 1),

    -- id=42: category changed to 风景
    (42, '海岸灯塔', '海岸、湖面与灯塔景观',
        'https://picsum.photos/800/600?random=42', 'https://picsum.photos/400/300?random=42',
        1, 2, 'test', '风景,海岸,灯塔', 800, 600, 372032, 0, 0, 0, 0, 0, 1),

    (43, '艺术图案', '具有装饰感的艺术图案',
        'https://picsum.photos/800/600?random=43', 'https://picsum.photos/400/300?random=43',
        7, 3, 'alice', '艺术,图案,设计', 800, 600, 376128, 0, 0, 0, 0, 0, 1),

    -- --- 其他 (category 8) ---
    (44, '雨中建筑剪影', '雨伞与建筑构成的生活影像',
        'https://picsum.photos/800/600?random=44', 'https://picsum.photos/400/300?random=44',
        8, 4, 'bob', '其他,建筑,雨景', 800, 600, 380224, 0, 0, 0, 0, 0, 1),

    (45, '棒球运动', '棒球运动员与比赛场景',
        'https://picsum.photos/800/600?random=45', 'https://picsum.photos/400/300?random=45',
        8, 5, 'carol', '其他,体育,棒球', 800, 600, 384320, 0, 0, 0, 0, 0, 1),

    (46, '花园景观', '花盆、围栏与花园空间',
        'https://picsum.photos/800/600?random=46', 'https://picsum.photos/400/300?random=46',
        8, 6, 'david', '其他,花园,生活', 800, 600, 388416, 0, 0, 0, 0, 0, 1),

    (47, '时尚配饰', '帽子、墨镜与服饰配饰',
        'https://picsum.photos/800/600?random=47', 'https://picsum.photos/400/300?random=47',
        8, 7, 'emma', '其他,时尚,配饰', 800, 600, 392512, 0, 0, 0, 0, 0, 1),

    -- id=48: category changed to 人物
    (48, '休闲服饰人物', '穿着休闲服装的人物',
        'https://picsum.photos/800/600?random=48', 'https://picsum.photos/400/300?random=48',
        2, 8, 'frank', '人物,服饰,生活', 800, 600, 396608, 0, 0, 0, 0, 0, 1),

    -- id=49: category changed to 风景
    (49, '湖畔风景', '湖岸与自然风景',
        'https://picsum.photos/800/600?random=49', 'https://picsum.photos/400/300?random=49',
        1, 9, 'grace', '风景,湖泊,自然', 800, 600, 400704, 0, 0, 0, 0, 0, 1),

    (50, '烛光静物', '蜡烛与静物构成的生活场景',
        'https://picsum.photos/800/600?random=50', 'https://picsum.photos/400/300?random=50',
        8, 10, 'henry', '其他,静物,生活', 800, 600, 404800, 0, 0, 0, 0, 0, 1),

    -- --- ID 51: user upload (明日方舟：终末地宣传图) ---
    -- URL uses Pollinations AI (consistent with anime image approach)
    (51, '明日方舟：终末地宣传图', '《明日方舟：终末地》二次元游戏角色与活动信息宣传海报',
        'https://image.pollinations.ai/prompt/Arknights%20Endfield%20anime%20game%20character%20promotional%20poster%2C%20official%20game%20art%20style%2C%20wide%20composition?width=800&height=600&seed=5101&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/Arknights%20Endfield%20anime%20game%20character%20promotional%20poster%2C%20official%20game%20art%20style%2C%20wide%20composition?width=400&height=300&seed=5101&nologo=true&model=flux&fm=jpg',
        9, 1, 'admin', '游戏,明日方舟终末地,二次元,角色,宣传海报', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    -- --- 游戏 (category 9, Pexels URLs) ---
    (52, '游戏手柄特写', '白色游戏手柄与电子设备',
        'https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        9, 1, 'admin', '游戏,手柄,电竞', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (53, '复古游戏摇杆', '经典摇杆控制器特写',
        'https://images.pexels.com/photos/275033/pexels-photo-275033.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/275033/pexels-photo-275033.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        9, 2, 'test', '游戏,摇杆,复古', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (54, '手柄与游戏设备', '游戏控制器和桌面设备',
        'https://images.pexels.com/photos/687811/pexels-photo-687811.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/687811/pexels-photo-687811.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        9, 3, 'alice', '游戏,设备,娱乐', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (55, '游戏控制器', '控制器与游戏操作设备',
        'https://images.pexels.com/photos/3945683/pexels-photo-3945683.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/3945683/pexels-photo-3945683.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        9, 4, 'bob', '游戏,控制器,电竞', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (56, '电竞麦克风与设备', '电竞直播使用的麦克风和输入设备',
        'https://images.pexels.com/photos/7862502/pexels-photo-7862502.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/7862502/pexels-photo-7862502.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        9, 5, 'carol', '游戏,电竞,直播', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (57, '电竞桌面与显示器', '鼠标、显示器与游戏电脑桌面',
        'https://images.pexels.com/photos/7915357/pexels-photo-7915357.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/7915357/pexels-photo-7915357.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        9, 6, 'david', '游戏,电脑,电竞', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    -- --- 动漫 (category 10, Pollinations AI URLs — replaced from Pexels) ---
    (58, '樱花列车少女', '樱花季站台上的二次元少女与驶来的列车',
        'https://image.pollinations.ai/prompt/anime%20girl%20at%20a%20train%20station%20under%20cherry%20blossoms%2C%20clean%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=5801&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/anime%20girl%20at%20a%20train%20station%20under%20cherry%20blossoms%2C%20clean%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=5801&nologo=true&model=flux&fm=jpg',
        10, 7, 'Pollinations AI', '动漫,二次元,少女,樱花,列车', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    (59, '霓虹机甲少年', '未来都市中身着机甲的二次元少年角色',
        'https://image.pollinations.ai/prompt/anime%20mecha%20boy%20in%20a%20neon%20futuristic%20city%2C%20dynamic%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=5902&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/anime%20mecha%20boy%20in%20a%20neon%20futuristic%20city%2C%20dynamic%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=5902&nologo=true&model=flux&fm=jpg',
        10, 8, 'Pollinations AI', '动漫,二次元,机甲,少年,未来都市', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    (60, '魔法图书馆', '奇幻图书馆中阅读魔法书的二次元角色',
        'https://image.pollinations.ai/prompt/anime%20mage%20reading%20a%20glowing%20book%20in%20a%20fantasy%20library%2C%20detailed%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6003&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/anime%20mage%20reading%20a%20glowing%20book%20in%20a%20fantasy%20library%2C%20detailed%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6003&nologo=true&model=flux&fm=jpg',
        10, 9, 'Pollinations AI', '动漫,二次元,魔法,图书馆,奇幻', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    (61, '海边夏日角色', '蓝天海岸背景下的清新二次元人物插画',
        'https://image.pollinations.ai/prompt/anime%20friends%20on%20a%20sunny%20seaside%2C%20bright%20summer%20colors%2C%20clean%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6104&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/anime%20friends%20on%20a%20sunny%20seaside%2C%20bright%20summer%20colors%2C%20clean%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6104&nologo=true&model=flux&fm=jpg',
        10, 10, 'Pollinations AI', '动漫,二次元,夏日,海边,人物', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    (62, '雨夜城市侦探', '雨夜街道中调查线索的二次元侦探角色',
        'https://image.pollinations.ai/prompt/anime%20detective%20in%20a%20rainy%20city%20at%20night%2C%20cinematic%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6205&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/anime%20detective%20in%20a%20rainy%20city%20at%20night%2C%20cinematic%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6205&nologo=true&model=flux&fm=jpg',
        10, 1, 'Pollinations AI', '动漫,二次元,侦探,雨夜,城市', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    (63, '云端幻想城堡', '飞行岛屿与云端城堡构成的二次元幻想场景',
        'https://image.pollinations.ai/prompt/anime%20fantasy%20castle%20on%20floating%20islands%20above%20the%20clouds%2C%20vibrant%20cel%20shading%2C%20wide%20composition?width=800&height=600&seed=6306&nologo=true&model=flux&fm=jpg',
        'https://image.pollinations.ai/prompt/anime%20fantasy%20castle%20on%20floating%20islands%20above%20the%20clouds%2C%20vibrant%20cel%20shading%2C%20wide%20composition?width=400&height=300&seed=6306&nologo=true&model=flux&fm=jpg',
        10, 2, 'Pollinations AI', '动漫,二次元,城堡,云海,幻想', 800, 600, NULL, 0, 0, 0, 0, 0, 1),

    -- --- 历史 (category 11, Pexels URLs) ---
    (64, '历史宫殿建筑', '宫殿与古典建筑群',
        'https://images.pexels.com/photos/208739/pexels-photo-208739.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/208739/pexels-photo-208739.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        11, 3, 'alice', '历史,宫殿,古迹', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (65, '古典宫殿与拱顶', '古典建筑中的拱顶和装饰结构',
        'https://images.pexels.com/photos/460672/pexels-photo-460672.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/460672/pexels-photo-460672.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        11, 4, 'bob', '历史,建筑,宫殿', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (66, '历史遗迹与教堂', '历史教堂、方尖碑与遗迹景观',
        'https://images.pexels.com/photos/2166553/pexels-photo-2166553.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/2166553/pexels-photo-2166553.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        11, 5, 'carol', '历史,遗迹,教堂', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (67, '古典喷泉', '具有历史感的喷泉与纪念碑',
        'https://images.pexels.com/photos/327509/pexels-photo-327509.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/327509/pexels-photo-327509.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        11, 6, 'david', '历史,喷泉,古迹', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (68, '古桥湖景', '湖泊与桥梁构成的历史风景',
        'https://images.pexels.com/photos/358482/pexels-photo-358482.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/358482/pexels-photo-358482.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        11, 7, 'emma', '历史,桥梁,湖景', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (69, '历史航海影像', '船只与历史航海主题影像',
        'https://images.pexels.com/photos/618079/pexels-photo-618079.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/618079/pexels-photo-618079.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        11, 8, 'frank', '历史,航海,船只', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    -- --- 书籍 (category 12, Pexels URLs) ---
    (70, '图书馆书架', '图书馆中的书架与阅读空间',
        'https://images.pexels.com/photos/256541/pexels-photo-256541.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/256541/pexels-photo-256541.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 9, 'grace', '书籍,图书馆,阅读', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (71, '书店阅读空间', '书架、书籍与书店环境',
        'https://images.pexels.com/photos/590493/pexels-photo-590493.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/590493/pexels-photo-590493.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 10, 'henry', '书籍,书店,阅读', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (72, '书架与藏书', '多层书架和整齐排列的图书',
        'https://images.pexels.com/photos/694740/pexels-photo-694740.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/694740/pexels-photo-694740.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 1, 'admin', '书籍,书架,藏书', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (73, '图书馆学习空间', '安静宽敞的图书馆阅览环境',
        'https://images.pexels.com/photos/3747468/pexels-photo-3747468.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/3747468/pexels-photo-3747468.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 2, 'test', '书籍,图书馆,学习', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (74, '满墙书柜', '占据整面墙的书柜与藏书',
        'https://images.pexels.com/photos/4855378/pexels-photo-4855378.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/4855378/pexels-photo-4855378.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 3, 'alice', '书籍,书柜,阅读', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (75, '图书馆阅览区', '图书馆内安静的阅读与学习区域',
        'https://images.pexels.com/photos/1370295/pexels-photo-1370295.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/1370295/pexels-photo-1370295.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 4, 'bob', '书籍,图书馆,学习', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (76, '图书馆藏书', '图书馆书架与丰富的藏书',
        'https://images.pexels.com/photos/3747505/pexels-photo-3747505.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/3747505/pexels-photo-3747505.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 5, 'carol', '书籍,图书馆,藏书', 800, 600, 300000, 0, 0, 0, 0, 0, 1),

    (77, '文具与书籍', '书本、文具与桌面学习用品',
        'https://images.pexels.com/photos/590016/pexels-photo-590016.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&fit=crop&fm=jpg',
        'https://images.pexels.com/photos/590016/pexels-photo-590016.jpeg?auto=compress&cs=tinysrgb&w=400&h=300&fit=crop&fm=jpg',
        12, 6, 'david', '书籍,文具,学习', 800, 600, 300000, 0, 0, 0, 0, 0, 1);

-- ===========================================================================
-- Ensure all images need feature extraction (Milvus init required after)
-- ===========================================================================
UPDATE image SET feature_extracted = 0;
