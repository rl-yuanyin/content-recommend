# Codex 调试请求：Milvus 初始化提取特征返回0

## 问题描述
调用 `POST /api/recommend/milvus/init` 接口，返回 `{"code":200,"msg":"操作成功","data":0}`，意味着 0 张图片成功提取特征。已执行3次，每次都是 data=0。

## 环境状态
- 7个Docker容器全部healthy（nacos/redis/rabbitmq/milvus-etcd/milvus-minio/milvus-standalone/milvus-attu）
- 5个微服务全部正常运行（cr-user:8081, cr-image:8082, cr-recommend:8083, cr-notification:8084, cr-gateway:8080）
- cr-recommend 已配置 JVM 代理参数：`-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890`
- 已通过 WMI 确认 Java 进程命令行包含 proxyHost 参数
- FlClash 代理端口 7890 在监听
- 数据库中 50 张图片的 URL 已全部更新为 Pexels 真实图片 URL
- 从 PowerShell 通过代理和直连均可 HTTP 200 访问 Pexels 图片 URL
- DJL ResNet50 模型和 PyTorch 引擎已下载到 `C:\Users\26416\.djl.ai` 缓存目录

## 关键代码路径
1. `RecommendController.initMilvus()` → `RecommendServiceImpl.initMilvus()`
2. `initMilvus()` 循环调用 `extractAndStoreFeature(imageId)` 
3. `extractAndStoreFeature()` 内部调用 `FeatureExtractService.extractFeature(url)`
4. `FeatureExtractService.extractFeature()` 第56行：`ImageFactory.getInstance().fromUrl(imageUrl)` 下载图片
5. 下载成功后调用 `predictor.predict(image)` 用 ResNet50 提取2048维特征
6. 特征向量写入 Milvus

## 可能的原因
1. **DJL ImageFactory.fromUrl() 的网络问题**：虽然 PowerShell 能访问 Pexels，但 Java 进程内的 DJL 可能有自己的 URL 连接实现，不走 JVM 代理参数
2. **DJL fromUrl() 默认超时太短**：可能 DJL 内部有默认的连接/读取超时
3. **SSL证书问题**：Pexels 是 HTTPS，Java 可能不信任证书
4. **图片格式问题**：Pexels 返回的 JPEG 可能 DJL 无法直接解析
5. **异常被静默吞掉**：FeatureExtractService 第61-64行的 catch 块只打了 warn 日志，可能日志没注意到

## 需要Codex排查的方向
1. 请在 cr-recommend 的 IDEA 控制台查看是否有 `Failed to extract image feature` 的 WARN 日志，以及具体的异常堆栈
2. 如果是 DJL fromUrl() 的网络问题，考虑改用以下方案之一：
   - 方案A：先用 OkHttp/HttpURLConnection 下载图片到临时文件，再用 `ImageFactory.getInstance().fromFile()` 读取本地文件
   - 方案B：给 DJL 的 fromUrl 设置更长的超时
   - 方案C：用 `java.net.Proxy` 显式设置代理给 URLConnection
3. 如果是 SSL 问题，可能需要配置 TrustManager 或导入证书

## 相关文件
- `cr-recommend/src/main/java/com/cr/recommend/service/FeatureExtractService.java`（特征提取服务）
- `cr-recommend/src/main/java/com/cr/recommend/service/impl/RecommendServiceImpl.java`（initMilvus 和 extractAndStoreFeature 方法）
- `cr-recommend/src/main/java/com/cr/recommend/config/DjlConfig.java`（ResNet50 模型配置）
- `cr-recommend/src/main/java/com/cr/recommend/service/MilvusService.java`（Milvus 向量操作）

## 请求
请 Codex 先查看 cr-recommend 控制台日志确认具体异常，然后修改 FeatureExtractService 使其能成功下载并提取 Pexels 图片的特征。修改后需要重新执行 `/api/recommend/milvus/init`。
