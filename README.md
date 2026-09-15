# 深度学习图像识别应用智能推荐系统算法设计

> 基于Spring Cloud Alibaba微服务架构的图像推荐平台，使用DJL+ResNet50提取图像特征，Milvus向量数据库做相似度搜索，通义千问VL大模型实现RAG架构的AI视觉理解。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 微服务框架 | Spring Cloud Alibaba | 2.7.18 |
| 服务注册/配置 | Nacos | v2.2.0 |
| API网关 | Spring Cloud Gateway | WebFlux |
| 远程调用 | OpenFeign + LoadBalancer | - |
| 深度学习 | DJL (Deep Java Library) | 0.27.0 |
| 预训练模型 | ResNet50 + PyTorch引擎 | - |
| 向量数据库 | Milvus | 2.3.12 |
| 大模型 | 通义千问VL (qwen-vl-plus) | DashScope API |
| 关系数据库 | MySQL | 8.0.33 |
| 缓存 | Redis | 7 |
| 消息队列 | RabbitMQ | 3.12 |
| 前端 | Vue3 + Vite + Element Plus | - |
| JDK | Amazon Corretto | 1.8.0_492 |

## 系统架构

```
                    ┌─────────────┐
                    │   Vue3 前端  │ :5173
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  cr-gateway  │ :8080 (JWT鉴权/路由/限流)
                    └──────┬──────┘
                           │
          ┌────────┬───────┼────────┬────────┐
          │        │       │        │        │
     ┌────▼───┐┌───▼──┐┌───▼────┐┌──▼─────┐┌──▼──────┐
     │cr-user ││cr-image││cr-recommend││cr-notification│
     │ :8081  ││ :8082 ││  :8083  ││  :8084 │
     └────┬───┘└───┬──┘└───┬────┘└──┬─────┘└─────────┘
          │        │       │        │
          │        │  ┌─────▼──────┐ │
          │        │  │  Milvus   │ │  ┌──────────┐
          └────────┼──│  :19530   │ │  │ RabbitMQ │
                   │  └────────────┘ │  │  :5672   │
                   │                 │  └──────────┘
              ┌────▼────┐       ┌───▼───┐
              │  MySQL  │       │ Redis │
              │  :3306  │       │ :6379 │
              └─────────┘       └───────┘
```

## 服务说明

| 服务 | 端口 | 说明 | Redis DB |
|------|------|------|----------|
| cr-gateway | 8080 | API网关，路由转发，JWT鉴权，白名单 | - |
| cr-user | 8081 | 用户服务，登录/注册/JWT | db0 |
| cr-image | 8082 | 图片服务，上传/查询/点赞/收藏/评论 | db1 |
| cr-recommend | 8083 | 推荐服务，DJL特征提取/Milvus搜索/通义千问VL | db2 |
| cr-notification | 8084 | 通知服务，点赞/收藏/评论通知 | db3 |

## 核心功能

### 1. AI图片识别（RAG架构）
```
用户上传图片
    │
    ▼
cr-recommend: ResNet50提取2048维特征
    │
    ▼
Milvus: 向量相似度搜索Top8
    │
    ▼
构建RAG上下文（相似图片的标题/标签/分类）
    │
    ▼
通义千问VL: 多模态视觉理解
    │
    ▼
返回: {分类, 标题, 描述, 标签}
    │
    ▼
前端自动填充（用户可编辑覆盖）
```
通义千问API异常时自动降级为向量匹配方案。

### 2. 混合推荐算法
- **内容推荐50%**：ResNet50提取特征 → Milvus向量余弦相似度搜索
- **协同过滤30%**：基于用户行为矩阵（浏览/点赞/评论/收藏/下载）
- **热门推荐20%**：按浏览量/点赞数/收藏数排序
- **降级策略**：用户行为<3条时自动降级为热门推荐

### 3. 异步特征提取
```
图片上传 → cr-image保存元数据 → RabbitMQ发送消息
→ cr-recommend监听消息 → DJL提取特征 → 写入Milvus
→ 回调cr-image更新feature_extracted状态
```

### 4. 图片分类
12个分类：风景、人物、动物、建筑、美食、科技、艺术、其他、游戏、动漫、历史、书籍

## 快速开始

### 环境要求

- JDK 1.8（推荐 Amazon Corretto 1.8.0_492）
- Maven 3.8+
- MySQL 8.0+
- Docker Desktop（含docker-compose）
- Node.js 18+（前端构建）
- 科学上网工具（用于下载DJL模型和Pexels图片）

### 1. 克隆项目

```bash
git clone https://github.com/rl-yuanyin/content-recommend.git
cd content-recommend
```

### 2. 启动Docker中间件

```bash
docker compose up -d
```

等待所有容器healthy（约1-2分钟）：
- nacos (8848)
- redis (6379)
- rabbitmq (5672, 管理界面15672)
- milvus-etcd (2379)
- milvus-minio (9000, 控制台9001)
- milvus-standalone (19530, 健检查9091)
- milvus-attu (3000, 可视化管理)

验证：
```bash
docker compose ps
```

### 3. 初始化数据库

使用MySQL客户端（如SQLyog、Navicat、MySQL Workbench）执行：

```sql
SOURCE D:/idea-demo/content-recommend/sql/content_recommend_full.sql;
```

或者手动导入 `sql/content_recommend_full.sql` 文件。

### 4. 配置通义千问API Key

1. 前往 [DashScope控制台](https://dashscope.console.aliyun.com/apiKey) 申请API Key
2. 在IDEA中配置cr-recommend运行环境变量：
   ```
   DASHSCOPE_API_KEY=sk-你的key
   ```

### 5. 配置代理（如需科学上网）

在IDEA中给cr-recommend配置VM options：
```
-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890
```

同时配置Docker Desktop代理：
- 设置 → 资源 → 代理 → 手动代理配置
- HTTP/HTTPS: `http://127.0.0.1:7890`

### 6. 启动后端微服务

在IDEA中按顺序启动（重要：按顺序，等前一个启动完成再启动下一个）：

1. CrUserApplication (8081)
2. CrImageApplication (8082)
3. CrRecommendApplication (8083)
4. CrNotificationApplication (8084)
5. CrGatewayApplication (8080)

**IDEA运行配置注意（cr-recommend）：**
- 缩短命令行：JAR manifest（依赖过多导致命令行超长）
- VM options: 代理参数
- 环境变量: DASHSCOPE_API_KEY

### 7. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 8. 初始化Milvus向量

启动所有微服务后，执行Milvus初始化（提取所有图片的特征向量）：

```bash
# 登录获取Token
$token = (Invoke-RestMethod -Uri "http://localhost:8080/api/user/login" `
  -Method POST -Body '{"username":"admin","password":"123456"}' `
  -ContentType "application/json").data

# 初始化Milvus
Invoke-RestMethod -Uri "http://localhost:8080/api/recommend/milvus/init" `
  -Method POST -Headers @{Authorization=$token}
```

首次初始化约需5-10分钟（下载ResNet50模型+提取77张图片特征）。

### 9. 访问系统

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | 前端首页 |
| http://localhost:8080/doc.html | Knife4j API文档 |
| http://localhost:8848/nacos | Nacos控制台 (nacos/nacos) |
| http://localhost:15672 | RabbitMQ管理 (guest/guest) |
| http://localhost:9001 | MinIO控制台 (minioadmin/minioadmin) |
| http://localhost:3000 | Attu Milvus管理界面 |

**登录账号：** admin / 123456

## 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | 123456 | 管理员 |
| test | 123456 | 测试用户 |
| alice | 123456 | 普通用户 |
| bob | 123456 | 普通用户 |
| emma | 123456 | 普通用户 |

## API列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | /api/user/login | 用户登录，返回JWT |
| 注册 | POST | /api/user/register | 用户注册 |
| 图片列表 | GET | /api/image/list | 分页查询图片 |
| 分类列表 | GET | /api/image/category/list | 获取所有分类 |
| 上传图片 | POST | /api/image/upload | 上传图片到本地 |
| 图片详情 | GET | /api/image/detail/{id} | 获取图片详情 |
| 点赞 | POST | /api/image/like/{id} | 点赞图片 |
| 收藏 | POST | /api/image/collect/{id} | 收藏图片 |
| 评论 | POST | /api/image/comment | 发表评论 |
| AI预测 | POST | /api/recommend/predict | 上传图片AI识别 |
| 综合推荐 | GET | /api/recommend/get | 混合推荐 |
| 内容推荐 | GET | /api/recommend/content/{id} | 基于图像特征相似推荐 |
| 协同过滤 | GET | /api/recommend/cf | 协同过滤推荐 |
| Milvus初始化 | POST | /api/recommend/milvus/init | 初始化向量数据 |
| 通知列表 | GET | /api/notification/list | 获取通知 |
| 未读数 | GET | /api/notification/unread/count | 获取未读数 |

## 项目结构

```
content-recommend/
├── cr-common/              # 公共模块（DTO、工具类、异常处理）
├── cr-user/                # 用户服务 (8081)
├── cr-image/               # 图片服务 (8082)
├── cr-recommend/           # 推荐服务 (8083)
│   ├── config/             # DjlConfig, QwenProperties, RedisConfig
│   ├── controller/         # RecommendController
│   ├── service/            # FeatureExtractService, MilvusService, LLMService
│   │   └── impl/           # RecommendServiceImpl, QwenLLMService
│   └── resources/          # application.yml
├── cr-notification/         # 通知服务 (8084)
├── cr-gateway/             # API网关 (8080)
├── frontend/               # Vue3前端
│   ├── src/
│   │   ├── api/            # API定义
│   │   ├── views/          # 页面组件
│   │   ├── router/         # 路由
│   │   ├── store/          # Pinia状态
│   │   ├── utils/          # Axios封装
│   │   └── App.vue
│   └── vite.config.js
├── sql/                    # SQL脚本
│   └── content_recommend_full.sql  # 一键初始化
├── docker-compose.yml      # Docker编排
└── README.md
```

## 常见问题

### Q: Docker拉取Milvus镜像失败？
A: MinIO镜像必须从quay.io拉取（已配置在docker-compose.yml中）。如果仍失败，检查Docker Desktop代理设置。

### Q: cr-recommend启动报"command line too long"？
A: IDEA运行配置 → Modify options → Add VM options → 缩短命令行选JAR manifest。

### Q: Milvus初始化返回data=0？
A: 检查cr-recommend的VM options是否配置了代理参数（-Dhttp.proxyHost等），DJL下载Pexels图片需要代理。

### Q: DJL提取特征报"Failed to read image from input stream"？
A: Pexels默认返回WebP格式，DJL不支持。已通过URL追加&fm=jpg和Accept头限制JPEG解决。如果使用自定义图片源，确保返回JPEG/PNG格式。

### Q: AI预测接口报"未配置通义千问API Key"？
A: 在IDEA运行配置中添加环境变量DASHSCOPE_API_KEY。未配置时自动降级为向量匹配方案。

### Q: Nacos启动报"Client not connected, current status:UNHEALTHY"？
A: Nacos容器启动后需等30-60秒才完全就绪。等Nacos控制台(localhost:8848/nacos)能访问后再启动微服务。

### Q: 端口被占用？
A:
```bash
netstat -ano | findstr :8083
taskkill /PID xxx /F
```

## 技术亮点

1. **RAG架构**：ResNet50特征提取→Milvus向量检索→构建上下文→通义千问VL视觉理解，不是简单的以图搜图，而是大模型视觉理解
2. **深度学习集成**：DJL+ResNet50+PyTorch引擎在Java端完成图像特征提取，无需Python服务
3. **向量数据库**：Milvus存储2048维特征向量，余弦相似度搜索，支持分类过滤
4. **降级策略**：大模型API异常时自动降级为向量匹配方案，保证可用性
5. **异步架构**：RabbitMQ异步处理特征提取和通知，不阻塞用户请求
6. **混合推荐**：内容推荐+协同过滤+热门推荐三路融合，冷启动降级
