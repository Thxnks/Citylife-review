# CityLife Review

[English](README.md) | 简体中文

CityLife Review 是一个面向本地生活点评平台的 Spring Boot 后端服务。支持手机验证码登录、商铺浏览、附近商铺搜索、商铺类型缓存、探店博客发布、点赞、关注、Feed 流、每日签到、优惠券，以及高并发秒杀优惠券下单。

项目覆盖真实后端开发中的关键问题：Redis 缓存策略、异步下单与消息可靠性、高并发数据一致性、Elasticsearch 全文搜索、基于 Spring AI 的智能推荐 Agent，以及 RAG（检索增强生成）管线实现语义化点评搜索。

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言 | Java 17 |
| 框架 | Spring Boot 3.4.5 |
| AI | Spring AI 1.0.0-M6（ChatClient、工具调用、对话记忆、向量库） |
| Web | Spring MVC、Spring AOP |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0 |
| 缓存 & 向量 | Redis Stack 7.2（RedisSearch + RedisJSON 模块） |
| 分布式 | Redisson 3.40.2（分布式锁、限流器） |
| 消息队列 | RabbitMQ（生产者 Confirm、手动 ACK、死信队列） |
| 搜索引擎 | Elasticsearch 7.17（全文搜索、地理位置排序） |
| API 文档 | SpringDoc OpenAPI 2.8.3（Swagger UI） |
| 脚本 | Lua（Redis 原子操作） |
| 工具库 | Lombok 1.18.34、Hutool 5.8.34 |
| CI/CD | GitHub Actions |

## 架构

```text
Client
  |
  v
Controller 层  ←  SpringDoc OpenAPI（Swagger UI）
  |                   10 个 Controller，含 AgentController
  v
Service 层
  |-- Spring AI（ChatClient + 工具调用 + 对话记忆 + 偏好提取器）
  |-- RAG 管线（MQE + HyDE → 多路检索 → LLM 重排序 + 情感标注）
  |-- Redis Stack（缓存、GEO、ZSet、Set、Bitmap、向量库、RRateLimiter）
  |-- RabbitMQ（异步秒杀下单，含生产者 Confirm + 死信队列）
  |-- Elasticsearch（商铺全文搜索 + 地理位置排序）
  |-- Redisson（分布式锁 + 限流器）
  |
  v
Mapper 层（MyBatis-Plus）
  |
  v
MySQL 8.0
```

## 包结构

```text
com.citylife
├── agent/               Spring AI 智能推荐 Agent
│   ├── RecommendationAgent.java   ChatClient + 工具调用 + 对话记忆
│   ├── AgentConfig.java           InMemoryChatMemory Bean
│   ├── memory/
│   │   ├── MemoryService.java         会话-用户绑定 + 语义记忆（Redis Hash）
│   │   ├── PreferenceExtractor.java   从对话中异步 LLM 提取用户偏好
│   │   └── PreferenceProfile.java     用户偏好画像模型（菜系/预算/氛围/习惯）
│   └── tool/
│       ├── BlogTool.java              @Tool: 获取店铺点评列表
│       ├── ReviewSearchTool.java      @Tool: RAG 语义搜索点评（含引用编号 + 情感标注）
│       ├── ShopSearchTool.java        @Tool: ES 全文搜索店铺（关键词/类型/排序/分页）
│       ├── UserProfileTool.java       @Tool: 获取用户画像 + 语义记忆偏好
│       └── VoucherTool.java           @Tool: 获取店铺优惠券列表
├── annotation/          @RateLimit — 声明式限流注解
├── aspect/              RateLimiterAspect — Redisson RRateLimiter AOP 切面
├── config/              Spring MVC、MyBatis-Plus、Redisson、RabbitMQ、ES、Spring AI、Swagger、异常处理
├── controller/          REST 接口（10 个，含 AgentController）
├── dto/                 请求/响应 DTO + AgentRequestDTO/AgentResponseDTO/ShopSearchResultDTO
├── document/            ShopDocument — ES 索引文档模型（含 @GeoPointField）
├── entity/              MySQL 表实体
├── enums/               业务结果枚举
├── exception/           自定义异常（RateLimitException）
├── mapper/              MyBatis-Plus Mapper
├── mq/                  SeckillOrderMessagePublisher + VoucherOrderConsumer
├── rag/                 RAG 检索管线
│   ├── RAGService.java          核心管线：MQE+HyDE 并行 → 多路检索并发 → LLM 重排序
│   ├── QueryExpander.java       MQE 多查询扩展 + HyDE 假设文档嵌入（含特定场景 Prompt 示例）
│   ├── ResultReRanker.java      LLM 语义重排序 + 情感标注（好评/差评/中性）+ 降级策略
│   ├── ReviewIndexService.java  Blog 向量化索引（TokenTextSplitter 分块 + 启动时自动建索引）
│   ├── RagConfig.java           RedisVectorStore + JedisPooled + ragExecutor 线程池
│   └── RagProperties.java       功能开关配置（MQE/HyDE/重排序/缓存/超时）
├── service/            Service 接口（11 个）
├── service.impl/       Service 实现（10 个实现 + 补偿服务 + 店铺搜索服务）
└── utils/              CacheClient、RedisIdWorker、SimpleRedisLock、ILock、拦截器、UserHolder、常量
```

## 核心功能

### AI 智能推荐 Agent（`POST /agent/recommend`）

基于 Spring AI 的对话式推荐助手，兼容 OpenAI 兼容 API：

**5 个 @Tool 工具：**
- `searchShops` — Elasticsearch 店铺全文搜索，支持关键词、类型筛选、排序、分页
- `getShopVouchers` — 查询指定店铺的优惠券/秒杀券列表
- `getShopBlogs` — 获取店铺的用户点评（按点赞数排序）
- `searchReviews` — **RAG 驱动**的语义点评搜索，返回引用编号 `[1][2]` 和情感标注（好评/差评/中性）
- `getUserProfile` — 当前用户画像 + 从对话中提取的语义记忆偏好

**偏好记忆系统：**
- `PreferenceExtractor` — LLM 从对话历史中提取结构化偏好：菜系偏好、预算水平、氛围偏好、用餐习惯、置信度
- `MemoryService` — 偏好画像持久化到 Redis Hash（`agent:memory:semantic:{userId}`）
- 会话-用户绑定（30 分钟 TTL），未登录会话也能关联偏好
- `getUserProfile` 在置信度 > 0.3 时自动注入语义记忆

**对话记忆：**
- 基于 `InMemoryChatMemory` + `MessageChatMemoryAdvisor` 的多轮对话上下文
- 每次推荐先查用户画像 → 搜店铺 → 搜点评 → 查优惠券 → 生成推荐理由

### RAG 语义点评搜索管线

`searchReviews` 工具背后的增强检索管线，针对本地生活点评场景设计——桥接用户口语查询与点评文本之间的语义鸿沟：

```text
用户查询（"哪家火锅好吃又不贵"）
  |
  ├─── 并行 ─────────────────────────────────┐
  |                                           |
  |  MQE（多查询扩展）                          HyDE（假设文档嵌入）
  |  LLM 将口语改写为多角度检索变体：             LLM 想象一段理想点评文本，
  |  "火锅 口味 菜品质量 推荐菜"                 再拿它去搜语义相似的真实点评
  |  "火锅 性价比 人均消费 价格"                 "用答案去找答案"
  |
  +─── 多路并发检索 ──────────────────────────+
  |     每个变体分别调用 vectorStore.similaritySearch()
  |     候选池 = topK × 3 / 查询数
  |
  +─── 按 documentId 合并去重
  |
  +─── LLM 重排序器（ResultReRanker）
  |     - 逐条评估与查询的实质相关度（0.0-1.0）
  |     - 标注情感倾向：positive / negative / neutral
  |     - 给出一句话判定理由
  |     - LLM 失败时自动降级为向量分数排序
  |
  +─── 格式化上下文（含 [1][2] 引用编号），注入 Agent 提示词
```

**工程化保障：**
- **可配置管线**（`citylife.rag.*`）：MQE、HyDE、重排序、缓存可独立开关
- **MQE/HyDE 结果缓存**：Redis 缓存，SHA-256 查询去重，可配置 TTL（默认 30 分钟），节省 LLM API 开销
- **全链路超时保护**：每个异步阶段默认 15 秒超时，LLM 慢不拖垮管线
- **专用线程池**：6 个守护线程覆盖 MQE + HyDE + 最多 4 路并发检索
- **每步可降级**：MQE 失败返回空列表 → 只用原始查询；HyDE 失败返回 null → 跳过；重排序失败 → 回退向量分数排序

**向量库：**
- Redis Stack `RedisVectorStore`，索引名 `citylife-review-index`，前缀 `rag:`
- `ReviewIndexService` 通过 `@PostConstruct` 在启动时自动将所有 Blog 内容向量化索引
- `TokenTextSplitter` 对长点评做分块处理

### 商铺全文搜索（`POST /shop/search`）

- **多字段加权搜索**：名称（权重 3×）> 区域（权重 2×）> 地址（权重 1×）
- **搜索结果高亮**：名称、区域、地址以 `<em>` 标签标记
- **排序方式**：相关性（默认）、销量（`sold`）、评分（`score`）、价格（`avgPrice`）、地理位置距离
- **索引管理**：一键从 MySQL 重建索引（`POST /shop/search/rebuild`）
- **地理位置字段**：每个店铺文档含 `@GeoPointField`，支持距离排序

### Redis 缓存策略

`CacheClient` 封装三种模式：

| 模式 | 解决问题 | 实现机制 |
| --- | --- | --- |
| 空值守卫 | 缓存穿透 | 缓存空值 + 短 TTL，防止恶意查询穿透到 DB |
| 互斥锁 | 缓存击穿 | `SimpleRedisLock` + `unlock.lua` 原子解锁 |
| 逻辑过期 + 异步重建 | 热点 key 过期 | 线程池后台重建，旧数据继续服务 |

### 秒杀下单流程

项目中可靠性要求最高的子系统：

```text
HTTP 请求
  |
  v
Redis Lua（seckill.lua）— 原子执行
  - 校验库存 > 0
  - 校验用户不在 seckill:order:{voucherId} 集合中
  - 扣减 seckill:stock:{voucherId}
  - 将用户加入 seckill:order:{voucherId}
  - 返回订单 ID
  |
  v
写入 PROCESSING 状态订单到 MySQL（MQ 发布前的"持久化承诺"）
  |
  v
发布 VoucherOrderMessage 到 RabbitMQ
  - 生产者 Confirm 开启
  - Return Callback 记录不可达消息
  - 发布失败：标记订单 FAILED，恢复 Redis 库存 + 用户资格
  |
  v
消费者（手动 ACK）
  - Redisson 用户级分布式锁
  - 校验 PROCESSING 订单存在
  - MySQL 库存扣减
  - 更新订单状态 → SUCCESS
  - 成功手动 ACK
  - 不可恢复失败：标记 FAILED，回滚 Redis，拒绝进入 DLQ
```

**三层补偿机制：**

| 层级 | 触发条件 | 补偿动作 |
| --- | --- | --- |
| MQ 发布失败 | Return Callback / 异常 | 标记 FAILED，恢复 Redis 库存 + 用户资格 |
| 消费失败 | 不可恢复错误 | 标记 FAILED，回滚 Redis，拒绝到 DLQ |
| 定时兜底 | `@Scheduled` 每 60 秒 | 扫描超过 5 分钟的 PROCESSING 订单，标记 FAILED，恢复 Redis |

**防重复下单：**
- 消费者内 Redisson 用户级分布式锁
- MySQL 唯一索引 `idx_user_voucher(user_id, voucher_id)`

详见 [docs/order-reliability.md](docs/order-reliability.md)。

### 声明式限流

```java
@RateLimit(key = "seckill", rate = 10, rateInterval = 1,
           rateIntervalUnit = TimeUnit.SECONDS, perUser = true)
public Result seckillVoucher(Long voucherId) { ... }
```

- `@RateLimit` 注解 + AOP 切面（`RateLimiterAspect`），零侵入
- 底层 Redisson `RRateLimiter`（`RateType.OVERALL`）
- 支持**全局**和**按用户**两种限流模式 — 按用户模式从 `UserHolder` 获取用户 ID 构建 key
- 超限抛出 `RateLimitException`，由 `WebExceptionAdvice` 统一处理

## Redis 使用总览

| 场景 | 数据结构 | Key 模式 |
| --- | --- | --- |
| 登录验证码 | String | `login:code:{phone}` |
| 登录 Token | Hash | `login:token:{token}` |
| 商铺详情缓存 | String (JSON) | `cache:shop:{id}` |
| 商铺类型列表缓存 | String (JSON) | `cache:shop-type:list` |
| 附近商铺（Redis） | GEO | `shop:geo:{typeId}` |
| 博客点赞 + 排行 | ZSet | `blog:liked:{blogId}` |
| 关注集合 | Set | `follows:{userId}` |
| Feed 流（推模式） | ZSet | `feed:{userId}` |
| 每日签到 + 连续天数 | Bitmap | `sign:{userId}:yyyyMM` |
| 秒杀库存 | String | `seckill:stock:{voucherId}` |
| 秒杀用户资格 | Set | `seckill:order:{voucherId}` |
| RAG 向量库 | Vector（RedisSearch） | `rag:` |
| 限流器 | RRateLimiter（Redisson） | `rate_limit:{key}` |
| MQE/HyDE 结果缓存 | String (JSON) | `rag:mqe:{sha256}`、`rag:hyde:{sha256}` |
| Agent 对话记忆 | InMemory（Spring AI） | — |
| Agent 语义记忆 | Hash | `agent:memory:semantic:{userId}` |
| Agent 会话-用户绑定 | String | `agent:memory:session_user:{sessionId}` |

## 本地启动

### 前置依赖

- JDK 17 或 21
- Maven 3.6+
- MySQL 8.0
- Redis Stack 7.2（向量检索需要 RedisSearch 模块）
- RabbitMQ 3.x
- Elasticsearch 7.17（可选，`/shop/search` 需要）
- LLM API Key（可选，`/agent/recommend` 和 RAG 需要）

### 初始化数据库

```sql
CREATE DATABASE citylife_review;
-- 然后导入：
-- src/main/resources/db/citylife_review.sql
```

### 环境变量

全部有本地默认值，可按需覆盖：

```text
# 数据库
MYSQL_URL=jdbc:mysql://127.0.0.1:3306/citylife_review?useSSL=false&serverTimezone=UTC
MYSQL_USERNAME=root
MYSQL_PASSWORD=

# Redis
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=

# RabbitMQ
RABBITMQ_HOST=127.0.0.1
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_VIRTUAL_HOST=/

# Elasticsearch
ELASTICSEARCH_URIS=http://127.0.0.1:9200

# 文件上传
CITYLIFE_IMAGE_UPLOAD_DIR=D:/lesson/nginx-1.18.0/html/citylife/imgs/

# Spring AI（LLM）
AGENT_API_KEY=           # LLM API Key
AGENT_BASE_URL=https://api.openai.com
AGENT_MODEL=gpt-4o
AGENT_EMBEDDING_MODEL=text-embedding-3-small
```

### 运行

```bash
mvn clean package -DskipTests   # 构建
mvn test                         # 运行测试（Mockito，无需中间件）
mvn spring-boot:run              # 启动（端口 8081）
```

Swagger UI：`http://localhost:8081/swagger-ui/index.html`

## Docker 部署

```bash
docker compose up -d --build    # 启动所有服务
docker compose down             # 停止
docker compose down -v          # 停止并删除数据卷
```

| 服务 | 端口 | 备注 |
| --- | --- | --- |
| App | 8081 | Swagger 地址 `/swagger-ui/index.html` |
| MySQL 8.0 | 3306 | root / citylife_root |
| Redis Stack 7.2 | 6379 | 已开启向量检索 |
| RabbitMQ | 5672 | 管理界面 `:15672`，citylife / citylife_pass |
| Elasticsearch 7.17 | 9200 | 单节点 |

## CI/CD

GitHub Actions（`.github/workflows/ci.yml`）在 push 和 PR 到 `main` 分支时自动执行 `mvn test`。

## 文档

- [API 接口文档](docs/api.md)
- [秒杀订单可靠性设计](docs/order-reliability.md)
- [测试指南](docs/testing.md)
- [性能测试说明](docs/performance-test.md)

## 简历亮点建议

**AI / LLM 集成：**
- 基于 Spring AI 实现对话式推荐 Agent：5 个 `@Tool` 注解工具、多轮对话记忆、从对话中异步 LLM 提取用户偏好（菜系/预算/氛围/习惯/置信度）并持久化到 Redis Hash。
- 设计可配置的 RAG 增强检索管线：MQE 多查询扩展 + HyDE 假设文档嵌入并行 → 多路并发 Redis Stack 向量检索 → LLM 语义重排序 + 情感标注（好评/差评/中性）→ MQE/HyDE 结果 SHA-256 缓存，节省 API 开销。MQE 针对本地生活场景设计了特定 Prompt 示例（"贵不贵"→"人均消费价格 性价比"等）。

**搜索：**
- 基于 Elasticsearch 实现商铺全文搜索：多字段加权相关性评分（名称 3×、区域 2×、地址 1×）、搜索结果高亮、地理位置距离排序、一键从 MySQL 重建索引。
- 使用 Redis Stack 作为向量数据库（RedisVectorStore），实现点评内容的语义搜索，TokenTextSplitter 分块 + 启动自动建索引。

**缓存与数据：**
- 封装 `CacheClient` 实现三种 Redis 缓存策略：空值守卫（防穿透）、互斥锁 + Lua 原子解锁（防击穿）、逻辑过期 + 线程池异步重建（防热点过期雪崩）。
- 综合使用 6 种 Redis 数据结构覆盖 10 个业务场景：String、Hash、GEO、ZSet、Set、Bitmap，外加 Vector 和 RRateLimiter。

**高并发下单：**
- 构建秒杀优惠券下单系统：Redis Lua 原子预扣、"持久化承诺"模式（PROCESSING 订单先行落库再发 MQ）、RabbitMQ 生产者 Confirm + 手动 ACK + 死信队列、三层补偿（发布失败/消费失败/定时扫描兜底）。
- Redisson 分布式锁 + MySQL 唯一索引双层防护防止高并发重复下单。

**基础设施：**
- 自定义 `@RateLimit` 注解 + AOP 切面，基于 Redisson `RRateLimiter` 实现声明式限流，支持全局和按用户两种模式。
- Docker Compose 一键部署 5 个服务（App + MySQL 8.0 + Redis Stack + RabbitMQ + Elasticsearch）。
