# CityLife Review

[English](README.md) | 简体中文

CityLife Review 是一个面向本地生活点评平台的 Spring Boot 后端服务。项目支持手机验证码登录、商铺浏览、附近商铺搜索、商铺类型缓存、探店博客发布、点赞、关注、关注流、每日签到、优惠券，以及高并发秒杀优惠券下单。

项目重点覆盖真实后端开发中的关键问题：Redis 缓存、异步下单、消息可靠性，以及高并发场景下的一致性保障。

## 技术栈

- Java 8
- Spring Boot 2.3
- Spring MVC
- MyBatis-Plus
- MySQL
- Redis
- Redisson
- RabbitMQ
- Lua
- Lombok
- Hutool

## 架构

```text
Client
  |
  v
Controller layer
  |
  v
Service layer
  |-- Redis
  |-- RabbitMQ
  |-- Redisson
  v
Mapper layer
  |
  v
MySQL
```

主要包结构：

```text
com.citylife
|-- config        Spring MVC、MyBatis-Plus、Redisson、RabbitMQ、异常处理
|-- controller    REST API 接口
|-- dto           请求和响应 DTO
|-- entity        MySQL 表实体
|-- enums         业务结果枚举
|-- mapper        MyBatis-Plus Mapper
|-- mq            RabbitMQ 生产者和消费者
|-- service       Service 接口
|-- service.impl  业务实现
`-- utils         Redis 工具、拦截器、常量、ID 生成器
```

## 核心功能

- 基于 Redis Token 存储的手机验证码登录。
- 商铺详情缓存，并支持缓存穿透保护。
- 商铺类型列表缓存和主动缓存失效。
- 基于 Redis GEO 的附近商铺搜索。
- 基于 Redis ZSet 的博客点赞和点赞用户排行。
- 基于 Redis Set 的关注关系和共同关注查询。
- 基于 Redis ZSet 推模式的关注流。
- 基于 Redis Bitmap 的每日签到和连续签到统计。
- 基于 Redis Lua、RabbitMQ、Redisson 和 MySQL 唯一索引保护的秒杀优惠券下单。

## 秒杀下单流程

```text
用户请求购买优惠券
  |
  v
Redis Lua 脚本
  - 校验库存
  - 校验一人一单
  - 预扣减 Redis 库存
  - 记录用户购买资格
  |
  v
发布 VoucherOrderMessage 到 RabbitMQ
  |
  v
消费者异步创建订单
  - Redisson 用户级锁
  - MySQL 重复下单校验
  - MySQL 库存扣减
  - 保存优惠券订单
  |
  v
成功后 ACK，不可恢复失败时拒绝并进入死信队列
```

可靠性保障：

- RabbitMQ 生产者 Confirm 和 Return Callback。
- 消息发布失败时回滚 Redis 预扣减状态。
- 消费者手动 ACK。
- 订单创建失败进入死信队列。
- Redisson 用户级锁控制并发。
- MySQL 在 `tb_voucher_order(user_id, voucher_id)` 上建立唯一索引。

## Redis 使用

| 场景 | 数据结构 | Key 模式 |
| --- | --- | --- |
| 登录验证码 | String | `login:code:{phone}` |
| 登录 Token | Hash | `login:token:{token}` |
| 商铺缓存 | String JSON | `cache:shop:{id}` |
| 商铺类型缓存 | String JSON | `cache:shop-type:list` |
| 附近商铺 | GEO | `shop:geo:{typeId}` |
| 博客点赞 | ZSet | `blog:liked:{blogId}` |
| 关注集合 | Set | `follows:{userId}` |
| 关注流 | ZSet | `feed:{userId}` |
| 每日签到 | Bitmap | `sign:{userId}:yyyyMM` |
| 优惠券库存 | String | `seckill:stock:{voucherId}` |
| 优惠券下单用户集合 | Set | `seckill:order:{voucherId}` |

## 本地启动

### 前置依赖

- JDK 8、11、17 或 21
- Maven 3.6+
- MySQL
- Redis
- RabbitMQ

### 初始化数据库

创建名为 `citylife_review` 的数据库，然后导入：

```text
src/main/resources/db/citylife_review.sql
```

如果订单表已经存在，可以手动添加唯一索引：

```sql
ALTER TABLE tb_voucher_order
ADD UNIQUE INDEX idx_user_voucher(user_id, voucher_id);
```

### 环境变量

项目内置了本地默认配置，也可以通过以下环境变量覆盖：

```text
MYSQL_URL
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
RABBITMQ_VIRTUAL_HOST
CITYLIFE_IMAGE_UPLOAD_DIR
```

RabbitMQ 远程访问通常需要使用非默认的 `guest` 用户。

### 运行

```bash
mvn spring-boot:run
```

默认服务端口：

```text
8081
```

## Docker 部署

前置依赖：

- Docker
- Docker Compose

启动应用和依赖服务：

```bash
docker compose up -d --build
```

Compose 会启动：

- `app`：`http://localhost:8081`
- MySQL：`localhost:3306`
- Redis：`localhost:6379`
- RabbitMQ：`localhost:5672`
- RabbitMQ 管理界面：`http://localhost:15672`

默认容器凭据：

```text
MySQL root password: citylife_root
RabbitMQ username: citylife
RabbitMQ password: citylife_pass
```

MySQL 会从以下文件初始化：

```text
src/main/resources/db/citylife_review.sql
```

停止服务：

```bash
docker compose down
```

删除容器和持久化数据卷：

```bash
docker compose down -v
```

## API 文档

见 [docs/api.md](docs/api.md)。

## 性能测试说明

见 [docs/performance-test.md](docs/performance-test.md)。

真实压测截图应在 MySQL、Redis、RabbitMQ 和应用都运行后，从你自己的机器生成。不要在简历或面试材料中使用编造的数据。

## 简历亮点建议

- 实现基于 Redis 的登录态、缓存穿透保护、GEO 搜索、ZSet 点赞/关注流和 Bitmap 签到。
- 使用 Redis Lua 原子化校验优惠券库存和一人一单约束。
- 构建 RabbitMQ 异步下单流程，包含生产者 Confirm、Return Callback、手动 ACK、死信队列和 Redis 回滚补偿。
- 使用 Redisson 和 MySQL 唯一索引防止高并发下重复下单。
