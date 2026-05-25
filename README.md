# CityLife Review

English | [简体中文](README.zh-CN.md)

CityLife Review is a Spring Boot backend service for a local life-review platform. It supports phone-code login, shop browsing, nearby shop search, shop type caching, blog posting, likes, follows, feed streams, daily sign-in, vouchers, and high-concurrency flash-sale voucher ordering.

The project covers real-world backend engineering concerns: Redis caching strategies, asynchronous order creation with message reliability, high-concurrency data consistency, Elasticsearch full-text search, an AI-powered recommendation agent built with Spring AI, and a RAG (Retrieval-Augmented Generation) pipeline for semantic review search.

## Tech Stack

| Category | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.4.5 |
| AI | Spring AI 1.0.0-M6 (ChatClient, Tool Calling, Chat Memory, Vector Store) |
| Web | Spring MVC, Spring AOP |
| ORM | MyBatis-Plus 3.5.7 |
| Database | MySQL 8.0 |
| Cache & Vector | Redis Stack 7.2 (RedisSearch + RedisJSON modules) |
| Distributed | Redisson 3.40.2 (lock, rate limiter) |
| Message Queue | RabbitMQ (publisher confirm, manual ACK, DLQ) |
| Search Engine | Elasticsearch 7.17 (full-text, geo-distance) |
| API Docs | SpringDoc OpenAPI 2.8.3 (Swagger UI) |
| Scripting | Lua (Redis atomic operations) |
| Utilities | Lombok 1.18.34, Hutool 5.8.34 |
| CI/CD | GitHub Actions |

## Architecture

```text
Client
  |
  v
Controller Layer  ←  SpringDoc OpenAPI (Swagger UI)
  |                   10 controllers including AgentController
  v
Service Layer
  |-- Spring AI (ChatClient + Tool Calling + ChatMemory + PreferenceExtractor)
  |-- RAG Pipeline (MQE + HyDE → multi-query retrieval → LLM rerank + sentiment)
  |-- Redis Stack (cache, GEO, ZSet, Set, Bitmap, Vector Store, RRateLimiter)
  |-- RabbitMQ (async seckill order with publisher confirm + DLQ)
  |-- Elasticsearch (full-text shop search with geo-distance sorting)
  |-- Redisson (distributed lock + rate limiter)
  |
  v
Mapper Layer (MyBatis-Plus)
  |
  v
MySQL 8.0
```

## Package Map

```text
com.citylife
├── agent/               Spring AI recommendation agent
│   ├── RecommendationAgent.java   ChatClient + tool calling + chat memory
│   ├── AgentConfig.java           InMemoryChatMemory bean
│   ├── memory/
│   │   ├── MemoryService.java         Session-user binding + semantic memory (Redis Hash)
│   │   ├── PreferenceExtractor.java   Async preference extraction from dialogue via LLM
│   │   └── PreferenceProfile.java     用户偏好画像 model
│   └── tool/
│       ├── BlogTool.java              @Tool: getShopBlogs — 店铺点评列表
│       ├── ReviewSearchTool.java      @Tool: searchReviews — RAG 语义搜索点评
│       ├── ShopSearchTool.java        @Tool: searchShops — ES 全文搜索店铺
│       ├── UserProfileTool.java       @Tool: getUserProfile — 用户画像 + 语义记忆
│       └── VoucherTool.java           @Tool: getShopVouchers — 店铺优惠券
├── annotation/          @RateLimit — declarative rate limiting
├── aspect/              RateLimiterAspect — Redisson RRateLimiter AOP
├── config/              Spring MVC, MyBatis-Plus, Redisson, RabbitMQ, ES, Spring AI, Swagger, exception handling
├── controller/          REST controllers (10 total, inc. AgentController)
├── dto/                 Request/response DTOs + AgentRequestDTO/AgentResponseDTO/ShopSearchResultDTO
├── document/            ShopDocument — Elasticsearch index model with @GeoPointField
├── entity/              MySQL table entities
├── enums/               Business result enums
├── exception/           Custom exceptions (RateLimitException)
├── mapper/              MyBatis-Plus mappers
├── mq/                  SeckillOrderMessagePublisher + VoucherOrderConsumer
├── rag/                 RAG retrieval pipeline
│   ├── RAGService.java          Core pipeline: MQE+HyDE parallel → multi-query retrieval → LLM rerank
│   ├── QueryExpander.java       MQE multi-query expansion + HyDE hypothetical document embedding
│   ├── ResultReRanker.java      LLM-based relevance reranking + sentiment tagging (positive/negative/neutral)
│   ├── ReviewIndexService.java  Blog-to-vector indexing with TokenTextSplitter
│   ├── RagConfig.java           RedisVectorStore + JedisPooled + ragExecutor thread pool
│   └── RagProperties.java       Configuration switches for MQE/HyDE/rerank/cache
├── service/            Service interfaces (11 total)
├── service.impl/       Service implementations (10 impls + compensation service + shop search)
└── utils/              CacheClient, RedisIdWorker, SimpleRedisLock, ILock, interceptors, UserHolder, constants
```

## Core Features

### AI-Powered Recommendation Agent (`POST /agent/recommend`)

A conversational recommendation agent built with Spring AI, powered by OpenAI-compatible LLMs:

**5 Tools registered via `@Tool` annotation:**
- `searchShops` — Elasticsearch full-text shop search with keyword, type, sort, pagination
- `getShopVouchers` — Fetch voucher/coupon list for a specific shop
- `getShopBlogs` — Fetch user reviews/blog posts for a shop (sorted by likes)
- `searchReviews` — **RAG-powered** semantic search across all review content, with citation indices `[1][2]` and sentiment tags
- `getUserProfile` — Current user's profile + semantic memory preferences

**Preference Memory System:**
- `PreferenceExtractor` — LLM extracts structured preferences (cuisine, budget, atmosphere, habits) from dialogue history
- `MemoryService` — Persists preference profiles in Redis Hash (`agent:memory:semantic:{userId}`)
- Session-to-user binding with 30-min TTL for anonymous sessions
- Preferences are re-injected into `getUserProfile` when confidence > 0.3

**Chat Memory:**
- Per-session `InMemoryChatMemory` via `MessageChatMemoryAdvisor`
- Multi-turn conversation context preserved within a session

### RAG Pipeline for Semantic Review Search

The `searchReviews` tool is backed by an enhanced retrieval pipeline designed for local-life review scenarios — bridging the semantic gap between colloquial user queries and review text:

```text
User query ("哪家火锅好吃又不贵")
  |
  ├─── Parallel ───────────────────────────┐
  |                                         |
  |  MQE (Multi-Query Expansion)            HyDE (Hypothetical Document Embedding)
  |  LLM rewrites query into retrieval-     LLM generates an imagined review
  |  optimized variants from different      matching the intent, then embeds it
  |  angles: "火锅 口味 菜品质量",          to search for semantically similar
  |  "火锅 性价比 人均消费 价格"            real reviews. "Use answers to find
  |                                         answers."
  |
  +─── Multi-query concurrent retrieval ───+
  |     Each variant runs vectorStore.similaritySearch()
  |     Candidate pool = topK × 3 per query
  |
  +─── Merge + dedup by documentId
  |
  +─── LLM Reranker (ResultReRanker)
  |     - Evaluates relevance per candidate (0.0–1.0)
  |     - Tags sentiment: positive / negative / neutral
  |     - Explains with 1-line reason per result
  |     - Fallback to vector-score sorting on LLM failure
  |
  +─── Formatted context with [citationIndex] injected into agent prompt
```

**Engineering safeguards:**
- **Configurable pipeline** (`citylife.rag.*`): MQE, HyDE, rerank, and cache can be toggled independently
- **MQE/HyDE result caching** in Redis with configurable TTL (default 30 min), keyed by SHA-256 of query — saves LLM API costs for repeated/similar queries
- **Timeout protection** at every async stage (default 15s) — slow LLM calls degrade gracefully, never block
- **Dedicated thread pool** (6 daemon threads) for parallel MQE+HyDE+multi-query retrieval
- **Every step has a fallback**: MQE fails → returns empty list; HyDE fails → returns null; rerank fails → falls back to vector-score sort

**Vector Store:**
- Redis Stack `RedisVectorStore` with `citylife-review-index`, prefix `rag:`
- `ReviewIndexService` indexes all Blog content on startup via `@PostConstruct`
- `TokenTextSplitter` chunks long reviews before embedding

### Full-Text Shop Search (`POST /shop/search`)

- **Multi-field weighted search**: name (boost 3×) > area (boost 2×) > address (boost 1×)
- **Result highlighting** with `<em>` tags for name, area, address
- **Sort options**: relevance (default), sales (`sold`), rating (`score`), price (`avgPrice`), geo-distance (when coordinates provided)
- **Index management**: one-click rebuild from MySQL (`POST /shop/search/rebuild`)
- **GeoPoint field** on each shop document for distance-based sorting

### Redis Caching Strategy

Three patterns in `CacheClient`:

| Pattern | Problem Solved | Mechanism |
| --- | --- | --- |
| Null-value guard | Cache penetration | Store null values with short TTL to prevent DB flood |
| Mutex lock | Cache breakdown | `SimpleRedisLock` + `unlock.lua` — serialize DB access on hot-key miss |
| Logical expiry + async rebuild | Hot-key expiry storm | Thread pool rebuilds data in background before physical expiry |

### Flash-Sale (Seckill) Order Flow

The most reliability-critical subsystem:

```text
HTTP request
  |
  v
Redis Lua (seckill.lua) — atomic execution
  - Check stock > 0
  - Check user NOT in seckill:order:{voucherId}
  - Decrement seckill:stock:{voucherId}
  - Add user to seckill:order:{voucherId}
  - Return order ID
  |
  v
Create PROCESSING order in MySQL ("persisted promise" before MQ publish)
  |
  v
Publish VoucherOrderMessage to RabbitMQ
  - Publisher confirm enabled
  - Return callback logs undeliverable messages
  - On publish failure: mark order FAILED, restore Redis stock + user set
  |
  v
Consumer (manual ACK)
  - Redisson user-level distributed lock
  - Verify PROCESSING order exists
  - Deduct MySQL voucher stock
  - Update order status → SUCCESS
  - Manual ACK on success
  - On unrecoverable failure: mark FAILED, rollback Redis, reject to DLQ
```

**Three-layer compensation:**

| Layer | Trigger | Action |
| --- | --- | --- |
| MQ publish failure | Return callback / exception | Mark FAILED, restore Redis stock + user set |
| Consumer failure | Unrecoverable error | Mark FAILED, rollback Redis, reject to DLQ |
| Stale order timeout | `@Scheduled` every 60s | Scan PROCESSING > 5 min, mark FAILED, restore Redis |

**Anti-duplicate defenses:**
- Redisson user-level lock in consumer
- MySQL unique index `idx_user_voucher(user_id, voucher_id)`

See [docs/order-reliability.md](docs/order-reliability.md) for full design.

### Declarative Rate Limiting

```java
@RateLimit(key = "seckill", rate = 10, rateInterval = 1,
           rateIntervalUnit = TimeUnit.SECONDS, perUser = true)
public Result seckillVoucher(Long voucherId) { ... }
```

- Annotation-driven via `@RateLimit` + AOP (`RateLimiterAspect`)
- Backed by Redisson `RRateLimiter` (`RateType.OVERALL`)
- Supports **global** and **per-user** modes — per-user key built from `UserHolder`
- Throws `RateLimitException` on limit exceeded, caught by `WebExceptionAdvice`

## Redis Usage Summary

| Scenario | Data Structure | Key Pattern |
| --- | --- | --- |
| Login verification code | String | `login:code:{phone}` |
| Login token | Hash | `login:token:{token}` |
| Shop detail cache | String (JSON) | `cache:shop:{id}` |
| Shop type list cache | String (JSON) | `cache:shop-type:list` |
| Nearby shops (Redis) | GEO | `shop:geo:{typeId}` |
| Blog likes + top users | ZSet | `blog:liked:{blogId}` |
| Follow set | Set | `follows:{userId}` |
| Feed stream (push mode) | ZSet | `feed:{userId}` |
| Daily sign-in + streak | Bitmap | `sign:{userId}:yyyyMM` |
| Seckill voucher stock | String | `seckill:stock:{voucherId}` |
| Seckill user qualification | Set | `seckill:order:{voucherId}` |
| RAG vector store | Vector (RedisSearch) | `rag:` |
| Rate limiter | RRateLimiter (Redisson) | `rate_limit:{key}` |
| MQE/HyDE result cache | String (JSON) | `rag:mqe:{sha256}`, `rag:hyde:{sha256}` |
| Agent chat memory | InMemory (Spring AI) | — |
| Agent semantic memory | Hash | `agent:memory:semantic:{userId}` |
| Agent session-user binding | String | `agent:memory:session_user:{sessionId}` |

## Local Setup

### Prerequisites

- JDK 17 or 21
- Maven 3.6+
- MySQL 8.0
- Redis Stack 7.2 (RedisSearch module required for vector store)
- RabbitMQ 3.x
- Elasticsearch 7.17 (optional — required for `/shop/search`)
- LLM API key (optional — required for `/agent/recommend` and RAG)

### Initialize Database

```sql
CREATE DATABASE citylife_review;
-- Then import:
-- src/main/resources/db/citylife_review.sql
```

### Environment Variables

All have local defaults; override as needed:

```text
# Database
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

# File upload
CITYLIFE_IMAGE_UPLOAD_DIR=D:/lesson/nginx-1.18.0/html/citylife/imgs/

# Spring AI (LLM)
AGENT_API_KEY=           # LLM API key
AGENT_BASE_URL=https://api.openai.com
AGENT_MODEL=gpt-4o
AGENT_EMBEDDING_MODEL=text-embedding-3-small
```

### Run

```bash
mvn clean package -DskipTests   # Build
mvn test                         # Run tests (Mockito, no middleware needed)
mvn spring-boot:run              # Start (port 8081)
```

Swagger UI: `http://localhost:8081/swagger-ui/index.html`

## Docker Deployment

```bash
docker compose up -d --build    # Start all services
docker compose down             # Stop
docker compose down -v          # Tear down including volumes
```

| Service | Port | Notes |
| --- | --- | --- |
| App | 8081 | Swagger at `/swagger-ui/index.html` |
| MySQL 8.0 | 3306 | root / citylife_root |
| Redis Stack 7.2 | 6379 | Vector search enabled |
| RabbitMQ | 5672 | Management UI at `:15672`, citylife / citylife_pass |
| Elasticsearch 7.17 | 9200 | Single-node |

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) runs `mvn test` on push and PR to `main`.

## Documentation

- [API Documentation](docs/api.md)
- [Order Reliability Design](docs/order-reliability.md)
- [Testing Guide](docs/testing.md)
- [Performance Test Notes](docs/performance-test.md)

## Suggested Resume Highlights

**AI / LLM Integration:**
- Built a conversational recommendation agent with Spring AI: 5 `@Tool`-annotated tools, multi-turn chat memory, and async preference extraction from dialogue that builds structured user profiles stored in Redis.
- Designed a configurable RAG pipeline: MQE multi-query expansion + HyDE hypothetical document embedding running in parallel → concurrent multi-query vector retrieval from Redis Stack → LLM-based relevance reranking with sentiment tagging → cached MQE/HyDE results keyed by SHA-256 to reduce API costs.

**Search:**
- Implemented full-text shop search with Elasticsearch: multi-field weighted relevance scoring (name 3×, area 2×, address 1×), result highlighting, geo-distance sorting, and one-click index rebuild from MySQL.
- Used Redis Stack as a vector store (RedisVectorStore) for semantic review search, with TokenTextSplitter chunking and startup indexing of all review content.

**Caching & Data:**
- Implemented three Redis caching strategies in a reusable `CacheClient`: null-value guard (cache penetration), mutex lock with Lua atomic unlock (cache breakdown), and logical expiry with async thread-pool rebuild (hot-key expiry).
- Used 6 Redis data structures across 10 business scenarios: String, Hash, GEO, ZSet, Set, Bitmap, plus Vector and RRateLimiter.

**High-Concurrency Ordering:**
- Built a flash-sale voucher ordering system with Redis Lua atomic pre-deduction, a "persisted promise" pattern (PROCESSING order before MQ publish), RabbitMQ publisher confirm + manual ACK + DLQ, and three-layer compensation (publish failure / consumer failure / scheduled stale-order scan).
- Used Redisson distributed lock + MySQL unique index as dual-layer duplicate prevention under concurrency.

**Infrastructure:**
- Added declarative rate limiting with a custom `@RateLimit` annotation backed by Redisson `RRateLimiter`, supporting global and per-user modes via AOP.
- Docker Compose one-click deployment with 5 services (app + MySQL 8.0 + Redis Stack + RabbitMQ + Elasticsearch).
