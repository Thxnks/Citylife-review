# Testing Guide

The default Maven test lifecycle is designed to run without local MySQL, Redis, or RabbitMQ.

```bash
mvn test
```

Manual integration tests that prepare Redis GEO, cache, ID-worker, HyperLogLog, or Redisson data are marked with `@Disabled`. They can be enabled locally when the required middleware is running.

## Current Automated Coverage

- MQ publish failure triggers order failure compensation.
- Consumer-side MySQL stock failure rejects the message and rolls back Redis state.
- Compensation service marks failed orders and restores Redis stock/user qualification.
- Querying an asynchronous order returns `PROCESSING` or `FAILED` status DTOs.
- Bitmap consecutive-sign counting logic is covered with assertions.

## Manual Middleware Tests

Run the Docker Compose stack first:

```bash
docker compose up -d --build
```

Then remove or override `@Disabled` on the manual integration tests you want to run:

- `CityLifeReviewApplicationTests`
- `RedissonTest`

Keep these tests out of the default lifecycle unless they are migrated to an isolated test environment such as Testcontainers.
