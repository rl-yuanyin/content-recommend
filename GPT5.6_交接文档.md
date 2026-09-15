# GPT5.6 交接文档：深度学习图像识别应用智能推荐系统

## 一、项目位置
D:\idea-demo\content-recommend

## 二、项目概述
毕业设计课题：深度学习图像识别应用智能推荐系统算法设计
微服务架构的图片推荐平台，使用 DJL+ResNet50 提取图像特征，Milvus 向量数据库做相似度搜索，实现图片上传 AI 自动识别分类、基于内容的智能推荐、协同过滤推荐和混合推荐。

## 三、技术栈
- 后端：Spring Boot 2.7.18 + Spring Cloud Alibaba (Nacos/Gateway/OpenFeign/Sentinel)
- 深度学习：DJL (Deep Java Library) 0.27.0 + ResNet50 + PyTorch 引擎
- 向量数据库：Milvus 2.3.12 (Docker 部署, 端口 19530)
- 数据库：MySQL 8.0.33 (content_recommend)
- 缓存：Redis 7 (Docker, 端口 6379)
- 消息队列：RabbitMQ 3.12 (Docker, 端口 5672)
- 前端：Vue3 + Vite + Element Plus + Pinia + Vue Router
- JDK：1.8 (Amazon Corretto 1.8.0_492)
- 构建工具：Maven

## 四、服务架构
| 服务 | 端口 | 说明 |
|------|------|------|
| cr-gateway | 8080 | API 网关 (Spring Cloud Gateway, WebFlux) |
| cr-user | 8081 | 用户服务 (登录/注册/JWT) |
| cr-image | 8082 | 图片服务 (上传/查询/点赞/收藏/评论) |
| cr-recommend | 8083 | 推荐服务 (DJL特征提取/Milvus向量搜索/推荐算法) |
| cr-notification | 8084 | 通知服务 (点赞/收藏/评论通知) |
| frontend | 5173 | Vue3 前端 (Vite dev server) |

Docker 容器（7个）：
- cr-nacos (8848) - 服务注册中心
- cr-redis (6379) - 缓存
- cr-rabbitmq (5672, 15672) - 消息队列
- milvus-etcd (2379) - Milvus 依赖
- milvus-minio (9000, 9001) - Milvus 对象存储
- milvus-standalone (19530, 9091) - Milvus 向量数据库
- milvus-attu (3000) - Milvus 可视化管理

## 五、启动步骤（严格按顺序）

### 5.1 启动顺序
1. 开 FlClash（代理，端口 7890）
2. 开 Docker Desktop，等 7 个容器全部 healthy
3. 开 IDEA → Reload Maven → 按顺序启动：
   cr-user → cr-image → cr-recommend → cr-notification → cr-gateway
4. 终端执行：
   cd D:\idea-demo\content-recommend\frontend
   npm run dev

### 5.2 cr-recommend 的 IDEA 运行配置
- VM options 必须配置：
  -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890
- 缩短命令行：JAR manifest（依赖过多导致命令行超长）
- 原因：DJL 下载 Pexels 图片需要代理，不配置会导致特征提取全部失败

### 5.3 Docker Desktop 代理配置
- 设置 → 资源 → 代理 → 手动代理配置
- HTTP: http://127.0.0.1:7890
- HTTPS: http://127.0.0.1:7890

### 5.4 登录测试
- 浏览器打开 http://localhost:5173
- 用户名：admin，密码：123456

## 六、已完成的功能

### 6.1 核心功能
- [x] 用户登录/注册 (JWT)
- [x] 图片上传（本地存储到 D:/idea-demo/content-recommend/upload/images/）
- [x] 图片瀑布流展示（首页）
- [x] 分类筛选（12个分类：风景/人物/动物/建筑/美食/科技/艺术/其他/游戏/动漫/历史/书籍）
- [x] 搜索功能
- [x] 图片详情页（点赞/收藏/下载/评论）
- [x] 相似图片推荐（基于 Milvus 向量搜索，返回12张，排除当前图片）
- [x] 通知中心（点赞/收藏通知，未读计数）
- [x] AI 图片识别（上传图片自动填充标题/描述/分类/标签）

### 6.2 推荐算法
- 基于内容推荐：ResNet50 提取 2048 维特征 → Milvus 向量相似度搜索
- 协同过滤推荐：基于用户行为矩阵（浏览/点赞/评论/收藏/下载）
- 热门推荐：按浏览量/点赞数/收藏数排序
- 混合推荐：内容50% + 协同过滤30% + 热门20%（用户行为<3条时降级为热门）

### 6.3 数据库现状
- 77 张图片（50 原始 Pexels + 26 新增分类 + 1 用户上传）
- 12 个分类
- Milvus image_features 集合：77 条向量（全部提取成功）

## 七、关键文件清单

### 后端
- cr-recommend/src/main/java/com/cr/recommend/controller/RecommendController.java — 推荐接口
- cr-recommend/src/main/java/com/cr/recommend/service/impl/RecommendServiceImpl.java — 推荐核心逻辑
- cr-recommend/src/main/java/com/cr/recommend/service/FeatureExtractService.java — DJL 特征提取
- cr-recommend/src/main/java/com/cr/recommend/service/MilvusService.java — Milvus 向量操作
- cr-recommend/src/main/java/com/cr/recommend/config/DjlConfig.java — ResNet50 模型配置
- cr-recommend/src/main/resources/application.yml — 推荐服务配置
- cr-image/src/main/java/com/cr/image/controller/ImageController.java — 图片接口
- cr-image/src/main/resources/application.yml — 图片服务配置（multipart 20MB）
- cr-image/src/main/java/com/cr/image/config/WebMvcConfig.java — 静态资源映射
- cr-gateway/src/main/resources/application.yml — 网关路由+白名单+codec 25MB
- docker-compose.yml — Docker 容器编排

### 前端
- frontend/src/views/Home.vue — 首页瀑布流
- frontend/src/views/ImageDetail.vue — 图片详情页+相似推荐
- frontend/src/views/Upload.vue — 上传页+AI预测填充
- frontend/src/views/Notification.vue — 通知中心
- frontend/src/views/Login.vue — 登录页
- frontend/src/api/index.js — API 定义
- frontend/src/utils/request.js — Axios 封装
- frontend/src/router/index.js — 路由
- frontend/src/store/user.js — Pinia 状态
- frontend/vite.config.js — Vite 配置（代理到 8080）

### SQL
- sql/init.sql — 建表脚本
- sql/update_image_urls_v2.sql — 50 张图片 Pexels URL
- sql/update_image_urls_v3.sql — Codex 生成的 77 张图片+12 分类

## 八、当前问题（需要 GPT5.6 解决）

### 问题1：AI 图片识别准确率低（优先级：高）
现象：上传证件照被分类为"建筑"，标题填充不合理。
当前方案：用 ResNet50 提取特征 → Milvus 搜索相似图片 → 用最相似图片的元数据推断。本质是"以图搜图"，不是真正的图像理解。
要求：
1. 引入 RAG + 大模型多模态视觉理解
2. 推荐方案：接入阿里云通义千问 VL（qwen-vl-plus/qwen-vl-max）多模态模型
   - 上传图片后调用大模型 API 做图像理解
   - 大模型生成：图片标题、描述、分类、标签
   - 比当前 ResNet50 特征匹配准确得多
3. 实现方式：
   - 在 cr-recommend 新增 LLMService，封装通义千问 VL API 调用
   - predictImage 方法改为：先调用大模型理解图片 → 返回分类/标题/描述/标签
   - 大模型 API key 从 application.yml 读取
   - 保留现有 ResNet50 特征提取和 Milvus 向量搜索（用于相似推荐，不丢弃）
4. 配置：通义千问 API key 需要用户自行申请，配置在 application.yml 中
5. 前端无需大改，predict 接口返回格式不变

### 问题2：动漫分类图片不符（优先级：中）
现象：Pexels 是真实照片站点，没有二次元/动漫图片，动漫分类下的图片内容与分类不符。
要求：
1. 动漫分类图片改为真实的动漫/二次元图片
2. 图片来源建议：
   - Bilibili 番剧封面（通过 API 获取）
   - 或用其他可访问的二次元图片来源
   - 注意：图片来源必须能被 DJL 下载（JPEG/PNG 格式，不能是 WebP）
   - URL 需要追加 &fm=jpg 参数（如果来源支持）
3. 生成 sql/update_anime_images.sql 替换动漫分类下的图片

### 问题3：id=51 的用户上传图片元数据不完整（优先级：低）
现象：id=51 的图片标题是 "my-image"，没有标签，导致 AI 预测时匹配到它会产生垃圾结果。
已在代码中修复（predictImage 跳过元数据不完整的图片），但建议在数据库中补全该图片的元数据。

## 九、常见问题与注意事项

### 9.1 Docker 镜像拉取
- MinIO 镜像必须从 quay.io 拉取：quay.io/minio/minio:RELEASE.2023-03-20T20-16-18Z
  （MinIO 已停止发布到 Docker Hub）
- Attu 版本用 v2.3.10（v2.3.12 不存在）
- 如果拉取失败：检查 Docker Desktop 代理配置 + FlClash 是否开启

### 9.2 DJL 图片下载
- Pexels URL 必须追加 &fm=jpg（强制 JPEG，DJL 不支持 WebP）
- Accept 头必须是 image/jpeg,image/jpg,image/png,image/*;q=0.8（不能包含 image/webp）
- JVM 必须配置代理参数（FlClash 端口 7890）

### 9.3 端口冲突
- 如果启动报 "Port XXXX already in use"：
  netstat -ano | findstr :8083
  taskkill /PID xxx /F

### 9.4 IDEA 命令行过长
- cr-recommend 依赖很多（DJL+Milvus+PyTorch），命令行会超长
- 运行配置 → Modify options → Add VM options → 缩短命令行选 JAR manifest

### 9.5 C 盘残留目录
- C:\Users\26416\AppData\Roaming\TRAE SOLO CN\ModularData\ai-agent\work-mode-projects\6aa3e3ca0b2c0a523509aa35\content-recommend 是旧目录，已废弃
- 所有操作必须在 D:\idea-demo\content-recommend 下进行

### 9.6 Nacos 启动顺序
- Nacos 容器启动后需要等 30-60 秒才能完全就绪
- 如果微服务启动报 "Client not connected, current status:UNHEALTHY"，等 Nacos 就绪后重启

### 9.7 MySQL
- 数据库：content_recommend
- 密码：123456
- 端口：3306（不是 Docker 容器，是本地安装的 MySQL）
- 工具：SQLyog Community

### 9.8 Redis 数据库分配
- cr-user: db0
- cr-image: db1
- cr-recommend: db2
- cr-notification: db3

### 9.9 RabbitMQ
- 交换机：content.exchange
- 路由键：notification.like, notification.comment, notification.collect, image.feature.extract
- 管理界面：http://localhost:15672（guest/guest）

### 9.10 API 路径前缀
- /api/user/** → cr-user
- /api/image/** → cr-image
- /api/recommend/** → cr-recommend
- /api/notification/** → cr-notification
- /uploads/** → cr-image（本地图片静态资源）

## 十、本次交接前的变更摘要

### 10.1 Codex 完成的变更
1. docker-compose.yml: minio 镜像改为 quay.io，attu 版本改为 v2.3.10
2. cr-recommend/pom.xml: 新增 spring-cloud-starter-loadbalancer
3. FeatureExtractService.java: HttpURLConnection 显式代理下载+超时+浏览器 UA
4. sql/update_image_urls_v3.sql: 77 张图片+12 分类（含游戏/动漫/历史/书籍）
5. RecommendController.java: 新增 /predict 和 /content/{imageId} 接口
6. RecommendServiceImpl.java: predictImage 和 getSimilarImages 方法实现
7. ImagePredictionDTO.java: 预测结果 DTO
8. Upload.vue: AI 自动填充标题/描述/分类/标签
9. ImageDetail.vue: 相似图片推荐展示

### 10.2 TRAE 完成的变更
1. FeatureExtractService.java: Accept 头去掉 image/webp，Pexels URL 追加 &fm=jpg
   原因：DJL 的 ImageIO 不支持 WebP，Pexels 默认返回 WebP 导致解码失败
2. RecommendController.java: predict 接口参数从 String 改为 MultipartFile
3. RecommendService.java: 接口签名同步修改
4. RecommendServiceImpl.java: predictImage 改为从 MultipartFile 读取 InputStream
5. FeatureExtractService.java: 新增 extractFeatureFromInputStream 方法
6. application.yml (cr-recommend): 新增 multipart 20MB/25MB 限制
7. api/index.js: predict 路径从 /image/predict 改为 /recommend/predict
8. RecommendServiceImpl.java: predictImage 优先选择有元数据的相似图片，跳过 "my-image" 等空数据
9. 修复中文全角引号导致的编译错误

### 10.3 当前状态
- 7 个 Docker 容器全部 healthy
- 5 个微服务全部正常运行
- Milvus 77 条向量全部提取成功
- AI 预测接口功能正常但准确率低（需要升级为 RAG+大模型方案）
- 相似推荐接口正常返回 12 张
