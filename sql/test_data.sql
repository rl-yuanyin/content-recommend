-- Additional test data for recommendation algorithm verification.
-- Run after sql/init.sql.

USE content_recommend;

SET NAMES utf8mb4;

-- Users 3-10. Both BCrypt hashes below match the plaintext password 123456.
INSERT INTO sys_user (id, username, password, nickname, email, status)
VALUES
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
    )
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    email = VALUES(email),
    status = VALUES(status);

-- Articles 6-20: five per category and multiple authors.
INSERT INTO article (
    id,
    title,
    summary,
    content,
    category_id,
    author_id,
    author_name,
    view_count,
    like_count,
    comment_count,
    status
)
VALUES
    (
        6,
        'HashMap底层结构与扩容机制',
        '从数组、链表和红黑树分析HashMap的实现。',
        'HashMap使用数组、链表和红黑树存储键值对。哈希冲突时采用链地址法，链表长度达到阈值后转为红黑树。扩容时容量翻倍并重新分配节点。',
        1,
        3,
        'alice',
        0,
        0,
        0,
        1
    ),
    (
        7,
        'JVM垃圾回收算法详解',
        '介绍标记清除、复制、标记整理和分代收集。',
        'JVM垃圾回收需要解决对象存活判断和内存回收问题。常见算法包括标记清除、复制算法、标记整理，以及基于分代的垃圾收集器。',
        1,
        5,
        'carol',
        0,
        0,
        0,
        1
    ),
    (
        8,
        'Java并发编程：线程池核心参数',
        '理解核心线程数、最大线程数、队列和拒绝策略。',
        'Java线程池通过核心线程数、最大线程数、空闲时间、任务队列和拒绝策略控制任务执行。合理配置线程池可以提升系统吞吐量并避免资源耗尽。',
        1,
        9,
        'grace',
        0,
        0,
        0,
        1
    ),
    (
        9,
        'Spring事务传播机制实践',
        '掌握REQUIRED、REQUIRES_NEW和NESTED的区别。',
        'Spring事务传播机制决定事务方法调用时如何加入或创建事务。REQUIRED是默认方式，REQUIRES_NEW会挂起当前事务并创建新事务。',
        1,
        10,
        'henry',
        0,
        0,
        0,
        1
    ),
    (
        10,
        'Java Stream流式处理指南',
        '使用Stream完成过滤、映射、排序和聚合。',
        'Java Stream提供声明式集合处理能力。通过filter、map、sorted、reduce和collect可以组合出清晰的数据处理流水线。',
        1,
        1,
        'admin',
        0,
        0,
        0,
        1
    ),
    (
        11,
        'Python Pandas数据清洗实战',
        '处理缺失值、重复值和异常值。',
        'Pandas可以快速读取表格数据，并通过dropna、fillna、drop_duplicates和条件筛选完成数据清洗，为后续分析准备高质量数据。',
        2,
        4,
        'bob',
        0,
        0,
        0,
        1
    ),
    (
        12,
        'Python异步编程：asyncio入门',
        '使用async和await构建高并发IO程序。',
        'asyncio使用事件循环调度协程。async定义协程，await等待异步操作，适合网络请求、文件IO和数据库访问等并发任务。',
        2,
        6,
        'david',
        0,
        0,
        0,
        1
    ),
    (
        13,
        'Django REST Framework接口开发',
        '使用序列化器和视图集构建REST API。',
        'Django REST Framework提供Serializer、ViewSet、Router和认证权限组件，可以快速构建结构清晰的RESTful接口。',
        2,
        8,
        'frank',
        0,
        0,
        0,
        1
    ),
    (
        14,
        'Python机器学习数据预处理',
        '掌握特征缩放、编码和训练集划分。',
        '机器学习模型依赖高质量特征。常用预处理包括标准化、归一化、独热编码和训练测试集划分，避免数据泄漏。',
        2,
        9,
        'grace',
        0,
        0,
        0,
        1
    ),
    (
        15,
        'Python Flask项目工程化实践',
        '组织蓝图、配置、数据库和异常处理。',
        'Flask适合构建轻量API服务。工程化项目通常使用应用工厂、Blueprint、配置类、SQLAlchemy和统一异常处理组织代码。',
        2,
        2,
        'test',
        0,
        0,
        0,
        1
    ),
    (
        16,
        'Vue 3响应式系统原理',
        '理解ref、reactive和依赖追踪机制。',
        'Vue 3使用Proxy实现响应式系统。读取属性时收集依赖，修改属性时触发更新，并通过调度器批量执行组件更新。',
        3,
        7,
        'emma',
        0,
        0,
        0,
        1
    ),
    (
        17,
        'React Hooks开发指南',
        '使用useState、useEffect和自定义Hook。',
        'React Hooks让函数组件可以管理状态和副作用。useState保存状态，useEffect处理副作用，自定义Hook用于复用有状态逻辑。',
        3,
        8,
        'frank',
        0,
        0,
        0,
        1
    ),
    (
        18,
        'TypeScript类型系统进阶',
        '讲解泛型、联合类型和类型收窄。',
        'TypeScript通过静态类型提升前端代码可靠性。泛型用于抽象类型关系，联合类型和类型守卫帮助编译器在分支中收窄类型。',
        3,
        10,
        'henry',
        0,
        0,
        0,
        1
    ),
    (
        19,
        'CSS Grid响应式布局技巧',
        '使用网格模板构建复杂页面布局。',
        'CSS Grid通过行和列定义二维布局，grid-template-columns、grid-template-rows、gap和媒体查询可以构建响应式页面。',
        3,
        3,
        'alice',
        0,
        0,
        0,
        1
    ),
    (
        20,
        '前端性能优化核心方法',
        '从资源加载、渲染和缓存角度提升性能。',
        '前端性能优化包括压缩资源、代码分割、懒加载、图片优化、缓存策略、减少重排重绘和使用浏览器性能分析工具。',
        3,
        4,
        'bob',
        0,
        0,
        0,
        1
    )
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    summary = VALUES(summary),
    content = VALUES(content),
    category_id = VALUES(category_id),
    author_id = VALUES(author_id),
    author_name = VALUES(author_name),
    status = VALUES(status);

-- Keep this script idempotent by replacing behavior data for users 1-10.
DELETE FROM user_behavior
WHERE user_id BETWEEN 1 AND 10;

-- 120 behavior records: each user has 5 views, 3 likes, and 4 comments.
INSERT INTO user_behavior (user_id, article_id, behavior_type, duration)
VALUES
    -- Java-focused users.
    (1, 1, 1, 120), (1, 6, 1, 150), (1, 7, 1, 95), (1, 8, 1, 180), (1, 10, 1, 110),
    (1, 2, 2, 0), (1, 6, 2, 0), (1, 9, 2, 0),
    (1, 1, 3, 0), (1, 6, 3, 0), (1, 7, 3, 0), (1, 10, 3, 0),

    (2, 2, 1, 100), (2, 6, 1, 130), (2, 7, 1, 145), (2, 9, 1, 160), (2, 10, 1, 90),
    (2, 1, 2, 0), (2, 7, 2, 0), (2, 8, 2, 0),
    (2, 2, 3, 0), (2, 7, 3, 0), (2, 8, 3, 0), (2, 9, 3, 0),

    (3, 1, 1, 115), (3, 2, 1, 125), (3, 6, 1, 170), (3, 8, 1, 135), (3, 10, 1, 105),
    (3, 6, 2, 0), (3, 7, 2, 0), (3, 10, 2, 0),
    (3, 1, 3, 0), (3, 2, 3, 0), (3, 6, 3, 0), (3, 8, 3, 0),

    -- Python-focused users.
    (4, 3, 1, 140), (4, 11, 1, 165), (4, 12, 1, 130), (4, 13, 1, 145), (4, 14, 1, 155),
    (4, 3, 2, 0), (4, 11, 2, 0), (4, 15, 2, 0),
    (4, 3, 3, 0), (4, 11, 3, 0), (4, 12, 3, 0), (4, 14, 3, 0),

    (5, 3, 1, 135), (5, 11, 1, 150), (5, 12, 1, 160), (5, 14, 1, 120), (5, 15, 1, 145),
    (5, 12, 2, 0), (5, 13, 2, 0), (5, 14, 2, 0),
    (5, 3, 3, 0), (5, 11, 3, 0), (5, 13, 3, 0), (5, 15, 3, 0),

    (6, 3, 1, 125), (6, 11, 1, 135), (6, 12, 1, 145), (6, 13, 1, 155), (6, 15, 1, 165),
    (6, 3, 2, 0), (6, 12, 2, 0), (6, 15, 2, 0),
    (6, 3, 3, 0), (6, 11, 3, 0), (6, 12, 3, 0), (6, 14, 3, 0),

    -- Frontend-focused users.
    (7, 4, 1, 135), (7, 5, 1, 150), (7, 16, 1, 170), (7, 17, 1, 140), (7, 18, 1, 155),
    (7, 4, 2, 0), (7, 16, 2, 0), (7, 20, 2, 0),
    (7, 4, 3, 0), (7, 5, 3, 0), (7, 16, 3, 0), (7, 18, 3, 0),

    (8, 4, 1, 145), (8, 5, 1, 125), (8, 16, 1, 160), (8, 18, 1, 150), (8, 20, 1, 175),
    (8, 5, 2, 0), (8, 17, 2, 0), (8, 19, 2, 0),
    (8, 4, 3, 0), (8, 5, 3, 0), (8, 17, 3, 0), (8, 19, 3, 0),

    -- Mixed-interest users for testing blended recommendations.
    (9, 1, 1, 110), (9, 7, 1, 130), (9, 3, 1, 145), (9, 12, 1, 150), (9, 16, 1, 140),
    (9, 6, 2, 0), (9, 11, 2, 0), (9, 17, 2, 0),
    (9, 1, 3, 0), (9, 12, 3, 0), (9, 16, 3, 0), (9, 18, 3, 0),

    (10, 2, 1, 120), (10, 8, 1, 135), (10, 13, 1, 145), (10, 4, 1, 155), (10, 18, 1, 165),
    (10, 9, 2, 0), (10, 14, 2, 0), (10, 19, 2, 0),
    (10, 2, 3, 0), (10, 13, 3, 0), (10, 4, 3, 0), (10, 19, 3, 0);

-- Synchronize article counters with the inserted behavior data.
UPDATE article article_row
SET
    view_count = (
        SELECT COUNT(*)
        FROM user_behavior behavior_row
        WHERE behavior_row.article_id = article_row.id
          AND behavior_row.behavior_type = 1
    ),
    like_count = (
        SELECT COUNT(*)
        FROM user_behavior behavior_row
        WHERE behavior_row.article_id = article_row.id
          AND behavior_row.behavior_type = 2
    ),
    comment_count = (
        SELECT COUNT(*)
        FROM user_behavior behavior_row
        WHERE behavior_row.article_id = article_row.id
          AND behavior_row.behavior_type = 3
    )
WHERE article_row.id BETWEEN 1 AND 20;
