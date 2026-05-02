# CityLife Review

CityLife Review is a Spring Boot backend service for a local life-review platform. It supports phone-code login, shop browsing, nearby shop search, shop type caching, blog posting, likes, follows, feed streams, daily sign-in, vouchers, and high-concurrency flash-sale voucher ordering.

The project focuses on practical backend concerns: Redis caching, asynchronous order creation, message reliability, and high-concurrency consistency.

## Tech Stack

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

## Architecture

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

Main package layout:

```text
com.citylife
|-- config        Spring MVC, MyBatis-Plus, Redisson, RabbitMQ, exception handling
|-- controller    REST API endpoints
|-- dto           Request and response DTOs
|-- entity        MySQL table entities
|-- enums         Business result enums
|-- mapper        MyBatis-Plus mappers
|-- mq            RabbitMQ publisher and consumers
|-- service       Service interfaces
|-- service.impl  Business implementations
`-- utils         Redis tools, interceptors, constants, ID worker
```

## Core Features

- Phone verification-code login with Redis token storage.
- Shop detail caching with cache penetration protection.
- Shop type list cache with explicit cache invalidation.
- Redis GEO nearby shop search.
- Blog likes and top-liked users with Redis ZSet.
- Follow relationship and common-follow query with Redis Set.
- Feed stream based on Redis ZSet push mode.
- Daily sign-in and consecutive sign-in count with Redis Bitmap.
- Flash-sale voucher ordering with Redis Lua, RabbitMQ, Redisson, and MySQL unique index protection.

## Flash-Sale Order Flow

```text
User requests voucher purchase
  |
  v
Redis Lua script
  - Check stock
  - Check one-user-one-order
  - Pre-deduct Redis stock
  - Record user qualification
  |
  v
Publish VoucherOrderMessage to RabbitMQ
  |
  v
Consumer creates order asynchronously
  - Redisson user-level lock
  - MySQL duplicate-order check
  - MySQL stock deduction
  - Save voucher order
  |
  v
ACK on success, reject to DLQ on unrecoverable failure
```

Reliability safeguards:

- RabbitMQ publisher confirm and return callback.
- Redis rollback when message publishing fails.
- Consumer manual ACK.
- Dead-letter queue for failed order creation.
- Redisson lock for user-level concurrency control.
- MySQL unique index on `tb_voucher_order(user_id, voucher_id)`.

## Redis Usage

| Scenario | Data Structure | Key Pattern |
| --- | --- | --- |
| Login code | String | `login:code:{phone}` |
| Login token | Hash | `login:token:{token}` |
| Shop cache | String JSON | `cache:shop:{id}` |
| Shop type cache | String JSON | `cache:shop-type:list` |
| Nearby shop | GEO | `shop:geo:{typeId}` |
| Blog likes | ZSet | `blog:liked:{blogId}` |
| Follow set | Set | `follows:{userId}` |
| Feed stream | ZSet | `feed:{userId}` |
| Daily sign-in | Bitmap | `sign:{userId}:yyyyMM` |
| Voucher stock | String | `seckill:stock:{voucherId}` |
| Voucher user set | Set | `seckill:order:{voucherId}` |

## Local Setup

### Prerequisites

- JDK 8, 11, 17, or 21
- Maven 3.6+
- MySQL
- Redis
- RabbitMQ

### Initialize Database

Create a database named `citylife_review`, then import:

```text
src/main/resources/db/citylife_review.sql
```

If the order table already exists, add the unique index manually:

```sql
ALTER TABLE tb_voucher_order
ADD UNIQUE INDEX idx_user_voucher(user_id, voucher_id);
```

### Environment Variables

The project has local defaults, but these variables can override them:

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

RabbitMQ remote access usually requires a user other than the default `guest`.

### Run

```bash
mvn spring-boot:run
```

Default server port:

```text
8081
```

## Docker Deployment

Prerequisites:

- Docker
- Docker Compose

Start the application and its dependencies:

```bash
docker compose up -d --build
```

The compose stack starts:

- `app` on `http://localhost:8081`
- MySQL on `localhost:3306`
- Redis on `localhost:6379`
- RabbitMQ on `localhost:5672`
- RabbitMQ Management UI on `http://localhost:15672`

Default container credentials:

```text
MySQL root password: citylife_root
RabbitMQ username: citylife
RabbitMQ password: citylife_pass
```

MySQL is initialized from:

```text
src/main/resources/db/citylife_review.sql
```

Stop the stack:

```bash
docker compose down
```

Remove containers and persistent data volumes:

```bash
docker compose down -v
```

## API Documentation

See [docs/api.md](docs/api.md).

## Performance Test Notes

See [docs/performance-test.md](docs/performance-test.md).

Real benchmark screenshots should be generated from your own machine after MySQL, Redis, RabbitMQ, and the application are running. Do not use fabricated numbers in resumes or interviews.

## Suggested Resume Highlights

- Implemented Redis-based login state, cache penetration protection, GEO search, ZSet likes/feed, and Bitmap sign-in.
- Used Redis Lua to atomically validate voucher stock and one-user-one-order constraints.
- Built RabbitMQ asynchronous order creation with publisher confirm, return callback, manual ACK, DLQ, and Redis rollback compensation.
- Used Redisson and a MySQL unique index to prevent duplicate voucher orders under concurrency.
